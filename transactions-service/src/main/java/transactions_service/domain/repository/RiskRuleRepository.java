package transactions_service.domain.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import transactions_service.domain.model.RiskRule;

import java.util.Optional;

public interface RiskRuleRepository extends ReactiveMongoRepository<RiskRule, Long> {

    Optional<RiskRule> findFirstByCurrency(String currency);
}
