package com.example.transactions.domaintest.repositrytest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import transactions.service.TransactionsServiceApplication;
import transactions.service.domain.model.RiskRule;
import transactions.service.domain.repository.RiskRuleRepository;

/**
 * Tests de integración para {@link RiskRuleRepository}.
 */
@DataMongoTest
@ContextConfiguration(classes = TransactionsServiceApplication.class)
public class RiskRuleRepositoryTest {

  /**
   * Repositorio de reglas de riesgo.
   */
  @Autowired
  private RiskRuleRepository riskRuleRepository;

  /**
   * Verifica que se obtenga la primera regla de riesgo por moneda.
   */
  @Test
  public void shouldFindFirstByCurrency() {
    final String currency = "PEN";

    final RiskRule rule1 = RiskRule.builder()
        .currency(currency)
        .maxDebitPerTx(new BigDecimal("5000"))
        .build();

    final RiskRule rule2 = RiskRule.builder()
        .currency(currency)
        .maxDebitPerTx(new BigDecimal("3000"))
        .build();

    final Mono<RiskRule> test = riskRuleRepository
        .deleteAll()
        .then(riskRuleRepository.save(rule1))
        .then(riskRuleRepository.save(rule2))
        .then(riskRuleRepository.findFirstByCurrency(currency));

    StepVerifier.create(test)
        .assertNext(rule -> {
          assertNotNull(rule);
          assertEquals(currency, rule.getCurrency());
        })
        .verifyComplete();
  }

  /**
   * Verifica que devuelva vacío si no existe regla para la moneda.
   */
  @Test
  public void shouldReturnEmptyWhenCurrencyNotFound() {
    final Mono<RiskRule> test = riskRuleRepository
        .deleteAll()
        .then(riskRuleRepository.findFirstByCurrency("USD"));

    StepVerifier.create(test)
        .verifyComplete();
  }

  /**
   * Verifica que encuentre la regla correcta entre múltiples monedas.
   */
  @Test
  public void shouldFindCorrectCurrencyAmongMultiple() {
    final RiskRule penRule = RiskRule.builder()
        .currency("PEN")
        .maxDebitPerTx(new BigDecimal("5000"))
        .build();

    final RiskRule usdRule = RiskRule.builder()
        .currency("USD")
        .maxDebitPerTx(new BigDecimal("1000"))
        .build();

    final Mono<RiskRule> test = riskRuleRepository
        .deleteAll()
        .then(riskRuleRepository.save(penRule))
        .then(riskRuleRepository.save(usdRule))
        .then(riskRuleRepository.findFirstByCurrency("USD"));

    StepVerifier.create(test)
        .assertNext(rule -> {
          assertEquals("USD", rule.getCurrency());
          assertEquals(new BigDecimal("1000"), rule.getMaxDebitPerTx());
        })
        .verifyComplete();
  }
}
