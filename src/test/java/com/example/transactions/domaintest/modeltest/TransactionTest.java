package com.example.transactions.domaintest.modeltest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.mapping.Document;
import transactions.service.domain.model.Transaction;

/**
 * Tests unitarios para {@link Transaction}.
 */
public class TransactionTest {

  /**
   * Verifica que {@link Transaction} tenga {@link Document} con el valor esperado.
   */
  @Test
  public void shouldHaveMongoDocumentAnnotation() {
    final Document document = Transaction.class.getAnnotation(Document.class);
    assertNotNull(document);
    assertEquals("transactions", document.value());
  }

  /**
   * Verifica que el builder asigne correctamente los campos.
   */
  @Test
  public void shouldBuildTransaction() {
    final Instant now = Instant.now();

    final Transaction tx = Transaction.builder()
        .id("tx-1")
        .accountId("acc-1")
        .type("DEBIT")
        .amount(new BigDecimal("12.34"))
        .timestamp(now)
        .status("OK")
        .reason(null)
        .build();

    assertEquals("tx-1", tx.getId());
    assertEquals("acc-1", tx.getAccountId());
    assertEquals("DEBIT", tx.getType());
    assertEquals(new BigDecimal("12.34"), tx.getAmount());
    assertEquals(now, tx.getTimestamp());
    assertEquals("OK", tx.getStatus());
    assertNull(tx.getReason());
  }

  /**
   * Verifica que setters/getters funcionen.
   */
  @Test
  public void shouldAllowSettingReason() {
    final Transaction tx = new Transaction();
    tx.setReason("manual_review");

    assertEquals("manual_review", tx.getReason());
  }
}
