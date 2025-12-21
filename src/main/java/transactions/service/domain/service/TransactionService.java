package transactions.service.domain.service;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import transactions.service.domain.dto.CreateTxRequest;
import transactions.service.domain.model.Account;
import transactions.service.domain.model.Transaction;
import transactions.service.domain.repository.AccountRepository;
import transactions.service.domain.repository.TransactionRepository;
import transactions.service.infrastructure.exception.BusinessException;

/**
 * Servicio de negocio para operaciones de transacciones.
 */
@Service
@RequiredArgsConstructor
public final class TransactionService {

  /**
   * Repositorio de cuentas.
   */
  private final AccountRepository accountRepo;

  /**
   * Repositorio de transacciones.
   */
  private final TransactionRepository txRepo;

  /**
   * Servicio de validación de reglas de riesgo.
   */
  private final RiskService riskService;

  /**
   * Sink reactivo para emitir transacciones en tiempo real.
   */
  private final Sinks.Many<Transaction> txSink;

  /**
   * Crea una nueva transacción aplicando validaciones de negocio.
   *
   * @param req solicitud de creación de transacción
   * @return transacción creada
   */
  public Mono<Transaction> create(final CreateTxRequest req) {
    return accountRepo.findByNumber(req.getAccountNumber())
        .switchIfEmpty(
            Mono.error(
                new BusinessException("account_not_found")))
        .flatMap(acc -> validateAndApply(acc, req))
        .onErrorMap(IllegalStateException.class,
            e -> new BusinessException(e.getMessage()));
  }

  /**
   * Valida reglas de riesgo y aplica la transacción a la cuenta.
   *
   * @param acc cuenta origen
   * @param req solicitud de transacción
   * @return transacción persistida
   */
  private Mono<Transaction> validateAndApply(
      final Account acc,
      final CreateTxRequest req) {

    final String type = req.getType().toUpperCase();
    final BigDecimal amount = req.getAmount();

    return riskService.isAllowed(acc.getCurrency(), type, amount)
        .flatMap(allowed -> {
          if (!allowed) {
            return Mono.error(
                new BusinessException("risk_rejected"));
          }

          if ("DEBIT".equals(type)
              && acc.getBalance().compareTo(amount) < 0) {
            return Mono.error(
                new BusinessException("insufficient_funds"));
          }

          return Mono.just(acc)
              .publishOn(Schedulers.parallel())
              .map(a -> {
                final BigDecimal newBal =
                    "DEBIT".equals(type)
                        ? a.getBalance()
                        .subtract(amount)
                        : a.getBalance()
                        .add(amount);
                a.setBalance(newBal);
                return a;
              })
              .flatMap(accountRepo::save)
              .flatMap(saved -> txRepo.save(
                  Transaction.builder()
                      .accountId(saved.getId())
                      .type(type)
                      .amount(amount)
                      .timestamp(Instant.now())
                      .status("OK")
                      .build()))
              .doOnNext(tx ->
                  txSink.tryEmitNext(tx));
        });
  }

  /**
   * Obtiene las transacciones de una cuenta.
   *
   * @param accountNumber número de cuenta
   * @return flujo de transacciones ordenadas por fecha
   */
  public Flux<Transaction> byAccount(
      final String accountNumber) {

    return accountRepo.findByNumber(accountNumber)
        .switchIfEmpty(Mono.error(
            new BusinessException("account_not_found")))
        .flatMapMany(acc ->
            txRepo.findByAccountIdOrderByTimestampDesc(
                acc.getId()));
  }

  /**
   * Flujo SSE con las transacciones en tiempo real.
   *
   * @return eventos de servidor con transacciones
   */
  public Flux<ServerSentEvent<Transaction>> stream() {
    return txSink.asFlux()
        .map(tx -> ServerSentEvent.builder(tx)
            .event("transaction")
            .build());
  }
}
