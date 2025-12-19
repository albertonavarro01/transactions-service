package transactions_service.infrastructure.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import transactions_service.domain.model.Account;
import transactions_service.domain.model.RiskRule;
import transactions_service.domain.repository.AccountRepository;
import transactions_service.domain.repository.RiskRuleRepository;

import java.math.BigDecimal;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RiskRuleRepository riskRepo;
    private final AccountRepository accountRepo;

    @Override
    public void run(String... args) {

        // 🔹 Bloqueante (JPA/H2)
        riskRepo.save(RiskRule.builder()
                .currency("PEN")
                .maxDebitPerTx(new BigDecimal("1500"))
                .build());

        riskRepo.save(RiskRule.builder()
                .currency("USD")
                .maxDebitPerTx(new BigDecimal("500"))
                .build());

        // 🔹 Reactivo (Mongo)
        accountRepo.deleteAll()
                .thenMany(Flux.just(
                        Account.builder()
                                .number("001-0001")
                                .holderName("Ana Peru")
                                .currency("PEN")
                                .balance(new BigDecimal("2000"))
                                .build(),
                        Account.builder()
                                .number("001-0002")
                                .holderName("Luis Acuña")
                                .currency("PEN")
                                .balance(new BigDecimal("800"))
                                .build()
                ))
                .flatMap(accountRepo::save)
                .blockLast(); // ✔️ solo permitido en bootstrap

        System.out.println("✅ Datos iniciales cargados: 2 cuentas y 2 reglas de riesgo");
    }
}
