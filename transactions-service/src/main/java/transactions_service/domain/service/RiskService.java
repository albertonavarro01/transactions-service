package transactions_service.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import transactions_service.domain.model.RiskRule;
import transactions_service.domain.repository.RiskRuleRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RiskService {

    private final RiskRuleRepository riskRepo;

    public Mono<Boolean> isAllowed(String currency, String type, BigDecimal amount) {
        return Mono.fromCallable(() ->
                        riskRepo.findFirstByCurrency(currency)
                                .map(RiskRule::getMaxDebitPerTx)
                                .orElse(new BigDecimal("0")))
                .subscribeOn(Schedulers.boundedElastic())
                .map(max -> {
                    if ("DEBIT".equalsIgnoreCase(type)) {
                        return amount.compareTo(max) <= 0;
                    }
                    return true;
                });
    }
}
