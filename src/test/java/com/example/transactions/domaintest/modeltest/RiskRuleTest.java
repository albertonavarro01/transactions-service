package com.example.transactions.domaintest.modeltest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.mapping.Document;
import transactions.service.domain.model.RiskRule;

/**
 * Tests unitarios para {@link RiskRule}.
 */
public class RiskRuleTest {

  /**
   * Verifica que {@link RiskRule} tenga la anotación {@link Document}
   * con el nombre de colección esperado.
   */
  @Test
  public void shouldHaveMongoDocumentAnnotation() {
    final Document document = RiskRule.class.getAnnotation(Document.class);
    assertNotNull(document);
    assertEquals("risk_rules", document.collection());
  }

  /**
   * Verifica que el builder asigne correctamente los campos.
   */
  @Test
  public void shouldBuildRiskRule() {
    final RiskRule rule = RiskRule.builder()
        .id("rr-1")
        .currency("USD")
        .maxDebitPerTx(new BigDecimal("500"))
        .build();

    assertEquals("rr-1", rule.getId());
    assertEquals("USD", rule.getCurrency());
    assertEquals(new BigDecimal("500"), rule.getMaxDebitPerTx());
  }
}
