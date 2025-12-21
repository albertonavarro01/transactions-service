package transactions.service.infrastructure.exception;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import transactions.service.domain.model.Account;
import transactions.service.domain.model.RiskRule;
import transactions.service.domain.repository.AccountRepository;
import transactions.service.domain.repository.RiskRuleRepository;

/**
 * Cargador de datos iniciales para reglas de riesgo y cuentas.
 */
@Component
@RequiredArgsConstructor
public final class DataSeeder implements CommandLineRunner {

  /**
   * Repositorio de reglas de riesgo.
   */
  private final RiskRuleRepository riskRepo;

  /**
   * Repositorio de cuentas.
   */
  private final AccountRepository accountRepo;

  /**
   * Inserta datos iniciales al arrancar la aplicación.
   *
   * @param args argumentos de línea de comandos
   */
  @Override
  public void run(final String... args) {

    Flux.just(
            RiskRule.builder()
                .currency("PEN")
                .maxDebitPerTx(new BigDecimal("1500"))
                .build(),
            RiskRule.builder()
                .currency("USD")
                .maxDebitPerTx(new BigDecimal("500"))
                .build()
        )
        .flatMap(riskRepo::save)
        .blockLast();

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
        .blockLast();

    System.out.println(
        "✅ Datos iniciales cargados: 2 cuentas y 2 reglas de riesgo");
  }
}
