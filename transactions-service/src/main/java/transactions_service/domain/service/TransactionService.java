package transactions_service.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import transactions_service.domain.dto.CreateTxRequest;
import transactions_service.domain.model.Account;
import transactions_service.domain.model.Transaction;
import transactions_service.domain.repository.AccountRepository;
import transactions_service.domain.repository.TransactionRepository;
import transactions_service.infrastructure.exception.BusinessException;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepo;
    private final TransactionRepository txRepo;
    private final RiskService riskService;
    private final Sinks.Many<Transaction> txSink;

    public Mono<Transaction> create(CreateTxRequest req) {
        return accountRepo.findByNumber(req.getAccountNumber())
                .switchIfEmpty(Mono.error(new BusinessException("account_not_found")))
                .flatMap(acc -> validateAndApply(acc, req))
                .onErrorMap(IllegalStateException.class,
                        e -> new BusinessException(e.getMessage()));
    }

    private Mono<Transaction> validateAndApply(Account acc, CreateTxRequest req) {
        String type = req.getType().toUpperCase();
        BigDecimal amount = req.getAmount();

        return riskService.isAllowed(acc.getCurrency(), type, amount)
                .flatMap(allowed -> {
                    if (!allowed) {
                        return Mono.error(new BusinessException("risk_rejected"));
                    }

                    if ("DEBIT".equals(type) && acc.getBalance().compareTo(amount) < 0) {
                        return Mono.error(new BusinessException("insufficient_funds"));
                    }

                    return Mono.just(acc)
                            .publishOn(Schedulers.parallel())
                            .map(a -> {
                                BigDecimal newBal = "DEBIT".equals(type)
                                        ? a.getBalance().subtract(amount)
                                        : a.getBalance().add(amount);
                                a.setBalance(newBal);
                                return a;
                            })
                            .flatMap(accountRepo::save)
                            .flatMap(saved -> txRepo.save(Transaction.builder()
                                    .accountId(saved.getId())
                                    .type(type)
                                    .amount(amount)
                                    .timestamp(Instant.now())
                                    .status("OK")
                                    .build()))
                            .doOnNext(tx -> txSink.tryEmitNext(tx));
                });
    }

    public Flux<Transaction> byAccount(String accountNumber) {
        return accountRepo.findByNumber(accountNumber)
                .switchIfEmpty(Mono.error(new BusinessException("account_not_found")))
                .flatMapMany(acc ->
                        txRepo.findByAccountIdOrderByTimestampDesc(acc.getId()));
    }

    public Flux<ServerSentEvent<Transaction>> stream() {
        return txSink.asFlux()
                .map(tx -> ServerSentEvent.builder(tx)
                        .event("transaction")
                        .build());
    }
}
