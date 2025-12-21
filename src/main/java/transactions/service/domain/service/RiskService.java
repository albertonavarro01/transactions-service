package transactions.service.domain.service;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import transactions.service.domain.model.RiskRule;
import transactions.service.domain.repository.RiskRuleRepository;

/**
 * Servicio de validación de reglas de riesgo.
 */
@Service
@RequiredArgsConstructor
public final class RiskService {

  /**
   * Repositorio de reglas de riesgo.
   */
  private final RiskRuleRepository riskRepo;

  /**
   * Evalúa si una transacción está permitida según reglas de riesgo.
   *
   * @param currency código de moneda
   * @param type     tipo de transacción
   * @param amount   monto solicitado
   * @return {@code true} si la operación está permitida
   */
  public Mono<Boolean> isAllowed(
      final String currency,
      final String type,
      final BigDecimal amount) {

    Mono<BigDecimal> maxMono = riskRepo
        .findFirstByCurrency(currency)
        .map(RiskRule::getMaxDebitPerTx)
        .defaultIfEmpty(BigDecimal.ZERO);

    return maxMono.map(max -> {
      if ("DEBIT".equalsIgnoreCase(type)) {
        return amount.compareTo(max) <= 0;
      }
      return true;
    });
  }
}
