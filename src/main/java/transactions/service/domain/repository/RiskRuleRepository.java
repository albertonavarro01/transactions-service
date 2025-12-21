package transactions.service.domain.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;
import transactions.service.domain.model.RiskRule;

/**
 * Repositorio para la entidad RiskRule.
 */
public interface RiskRuleRepository
    extends ReactiveMongoRepository<RiskRule, String> {

  /**
   * Devuelve la primera regla de riesgo para una moneda dada.
   *
   * @param currency código de moneda (ej. "PEN")
   * @return Mono con la regla si existe
   */
  Mono<RiskRule> findFirstByCurrency(String currency);
}
