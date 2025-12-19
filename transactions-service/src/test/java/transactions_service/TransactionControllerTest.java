package transactions_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.mockito.ArgumentMatchers.any;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import transactions_service.domain.model.Account;
import transactions_service.domain.model.RiskRule;
import transactions_service.domain.repository.AccountRepository;
import transactions_service.domain.repository.RiskRuleRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = TransactionsServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration"
        }
)
@DisplayName("Transaction Controller Integration Tests")
class TransactionControllerTest {

    @MockitoBean
    private RiskRuleRepository riskRuleRepository;

    @MockitoBean
    private AccountRepository accountRepository;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        // Configura las cuentas de prueba con saldo suficiente
        Account account1 = Account.builder()
                .id("acc-001")
                .number("001-0001")
                .holderName("Test User 1")
                .currency("PEN")
                .balance(new BigDecimal("1000.00"))
                .build();

        Account account2 = Account.builder()
                .id("acc-002")
                .number("001-0002")
                .holderName("Test User 2")
                .currency("USD")
                .balance(new BigDecimal("500.00"))
                .build();

        // Mock del AccountRepository
        when(accountRepository.findByNumber("001-0001"))
                .thenReturn(Mono.just(account1));

        when(accountRepository.findByNumber("001-0002"))
                .thenReturn(Mono.just(account2));

        // Mock para guardar cuentas (cuando se actualiza el balance)
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Configura reglas de riesgo que PERMITAN las transacciones
        RiskRule penRule = RiskRule.builder()
                .id("rule-pen-001")
                .currency("PEN")
                .maxDebitPerTx(new BigDecimal("1500.00"))
                .build();

        RiskRule usdRule = RiskRule.builder()
                .id("rule-usd-001")
                .currency("USD")
                .maxDebitPerTx(new BigDecimal("1500.00"))
                .build();

        // Mock del RiskRuleRepository
        when(riskRuleRepository.findFirstByCurrency("PEN"))
                .thenReturn(Optional.of(penRule));

        when(riskRuleRepository.findFirstByCurrency("USD"))
                .thenReturn(Optional.of(usdRule));

        when(riskRuleRepository.findFirstByCurrency(anyString()))
                .thenReturn(Optional.of(penRule));

        System.out.println("✅ Mocks configurados: cuentas con saldo y reglas de riesgo permisivas");
    }
}