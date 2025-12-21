package com.example.transactions.domaintest.repositrytest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import transactions.service.TransactionsServiceApplication;
import transactions.service.domain.model.Transaction;
import transactions.service.domain.repository.TransactionRepository;

/**
 * Tests de integración para {@link TransactionRepository}.
 * Usa @DataMongoTest para probar solo la capa de datos con MongoDB embebido.
 */
@DataMongoTest
@ContextConfiguration(classes = TransactionsServiceApplication.class)
class TransactionRepositoryTest {

  @Autowired
  private TransactionRepository transactionRepository;

  /**
   * Verifica que se pueda guardar y buscar una transacción por ID.
   */
  @Test
  void shouldSaveAndFindTransactionById() {
    // Given: Una transacción válida
    final Instant now = Instant.now();
    Transaction tx = Transaction.builder()
        .id("tx-1")
        .accountId("acc-1")
        .type("DEBIT")
        .amount(new BigDecimal("100.50"))
        .timestamp(now)
        .status("OK")
        .build();

    // When: Se guarda y se busca por ID
    Mono<Transaction> test = transactionRepository
        .deleteAll()
        .then(transactionRepository.save(tx))
        .then(transactionRepository.findById("tx-1"));

    // Then: Se encuentra la transacción correctamente
    StepVerifier.create(test)
        .assertNext(found -> {
          assertEquals("tx-1", found.getId());
          assertEquals("acc-1", found.getAccountId());
          assertEquals("DEBIT", found.getType());
          assertEquals(0, new BigDecimal("100.50").compareTo(found.getAmount()));
          assertEquals("OK", found.getStatus());
        })
        .verifyComplete();
  }

  /**
   * Verifica que se puedan guardar múltiples transacciones.
   */
  @Test
  void shouldSaveMultipleTransactions() {
    // Given: Dos transacciones para la misma cuenta
    Transaction tx1 = Transaction.builder()
        .id("tx-1")
        .accountId("acc-100")
        .type("DEBIT")
        .amount(new BigDecimal("50.00"))
        .timestamp(Instant.now())
        .status("OK")
        .build();

    Transaction tx2 = Transaction.builder()
        .id("tx-2")
        .accountId("acc-100")
        .type("CREDIT")
        .amount(new BigDecimal("200.00"))
        .timestamp(Instant.now())
        .status("OK")
        .build();

    // When: Se guardan ambas transacciones
    Mono<Long> test = transactionRepository
        .deleteAll()
        .then(transactionRepository.save(tx1))
        .then(transactionRepository.save(tx2))
        .then(transactionRepository.count());

    // Then: Se cuentan 2 transacciones
    StepVerifier.create(test)
        .assertNext(count -> assertEquals(2L, count))
        .verifyComplete();
  }

  /**
   * Verifica que se puedan guardar transacciones con diferentes tipos.
   */
  @Test
  void shouldSaveTransactionsWithDifferentTypes() {
    // Given: Transacciones de tipo DEBIT y CREDIT
    Transaction debit = Transaction.builder()
        .id("tx-debit")
        .accountId("acc-1")
        .type("DEBIT")
        .amount(new BigDecimal("75.00"))
        .timestamp(Instant.now())
        .status("OK")
        .build();

    Transaction credit = Transaction.builder()
        .id("tx-credit")
        .accountId("acc-1")
        .type("CREDIT")
        .amount(new BigDecimal("150.00"))
        .timestamp(Instant.now())
        .status("OK")
        .build();

    // When: Se guardan ambas
    Mono<Long> test = transactionRepository
        .deleteAll()
        .then(transactionRepository.save(debit))
        .then(transactionRepository.save(credit))
        .then(transactionRepository.count());

    // Then: Se cuentan 2 transacciones
    StepVerifier.create(test)
        .assertNext(count -> assertEquals(2L, count))
        .verifyComplete();
  }

  /**
   * Verifica que se puedan guardar transacciones con estado REJECTED.
   */
  @Test
  void shouldSaveRejectedTransaction() {
    // Given: Una transacción rechazada con razón
    Transaction rejected = Transaction.builder()
        .id("tx-rejected")
        .accountId("acc-2")
        .type("DEBIT")
        .amount(new BigDecimal("5000.00"))
        .timestamp(Instant.now())
        .status("REJECTED")
        .reason("Insufficient funds")
        .build();

    // When: Se guarda
    Mono<Transaction> test = transactionRepository
        .deleteAll()
        .then(transactionRepository.save(rejected))
        .then(transactionRepository.findById("tx-rejected"));

    // Then: Se guarda correctamente con la razón
    StepVerifier.create(test)
        .assertNext(found -> {
          assertEquals("REJECTED", found.getStatus());
          assertEquals("Insufficient funds", found.getReason());
        })
        .verifyComplete();
  }

  /**
   * Verifica que se pueda eliminar una transacción por ID.
   */
  @Test
  void shouldDeleteTransactionById() {
    // Given: Una transacción guardada
    Transaction tx = Transaction.builder()
        .id("tx-delete")
        .accountId("acc-3")
        .type("DEBIT")
        .amount(new BigDecimal("25.00"))
        .timestamp(Instant.now())
        .status("OK")
        .build();

    // When: Se guarda y luego se elimina
    Mono<Transaction> test = transactionRepository
        .deleteAll()
        .then(transactionRepository.save(tx))
        .then(transactionRepository.deleteById("tx-delete"))
        .then(transactionRepository.findById("tx-delete"));

    // Then: Ya no existe en la BD
    StepVerifier.create(test)
        .verifyComplete();
  }

  /**
   * Verifica que se puedan guardar y contar múltiples transacciones.
   */
  @Test
  void shouldCountMultipleTransactions() {
    // Given: Tres transacciones
    Transaction tx1 = Transaction.builder()
        .id("tx-1")
        .accountId("acc-500")
        .type("DEBIT")
        .amount(new BigDecimal("10.00"))
        .timestamp(Instant.now())
        .status("OK")
        .build();

    Transaction tx2 = Transaction.builder()
        .id("tx-2")
        .accountId("acc-500")
        .type("CREDIT")
        .amount(new BigDecimal("20.00"))
        .timestamp(Instant.now())
        .status("OK")
        .build();

    Transaction tx3 = Transaction.builder()
        .id("tx-3")
        .accountId("acc-500")
        .type("DEBIT")
        .amount(new BigDecimal("30.00"))
        .timestamp(Instant.now())
        .status("OK")
        .build();

    // When: Se guardan y se cuentan
    Mono<Long> test = transactionRepository
        .deleteAll()
        .then(transactionRepository.save(tx1))
        .then(transactionRepository.save(tx2))
        .then(transactionRepository.save(tx3))
        .then(transactionRepository.count());

    // Then: Se cuentan 3 transacciones
    StepVerifier.create(test)
        .assertNext(count -> assertEquals(3L, count))
        .verifyComplete();
  }

  /**
   * Verifica que el timestamp se preserve correctamente.
   */
  @Test
  void shouldPreserveTimestamp() {
    // Given: Una transacción con timestamp específico
    final Instant specificTime = Instant.parse("2024-01-15T10:30:00Z");
    Transaction tx = Transaction.builder()
        .id("tx-time")
        .accountId("acc-4")
        .type("DEBIT")
        .amount(new BigDecimal("99.99"))
        .timestamp(specificTime)
        .status("OK")
        .build();

    // When: Se guarda y se recupera
    Mono<Transaction> test = transactionRepository
        .deleteAll()
        .then(transactionRepository.save(tx))
        .then(transactionRepository.findById("tx-time"));

    // Then: El timestamp se preserva
    StepVerifier.create(test)
        .assertNext(found -> {
          assertNotNull(found.getTimestamp());
          assertEquals(specificTime, found.getTimestamp());
        })
        .verifyComplete();
  }

  /**
   * Verifica que findAll funcione correctamente.
   */
  @Test
  void shouldFindAllTransactions() {
    // Given: Dos transacciones guardadas
    Transaction tx1 = Transaction.builder()
        .id("tx-all-1")
        .accountId("acc-1")
        .type("DEBIT")
        .amount(new BigDecimal("100.00"))
        .timestamp(Instant.now())
        .status("OK")
        .build();

    Transaction tx2 = Transaction.builder()
        .id("tx-all-2")
        .accountId("acc-2")
        .type("CREDIT")
        .amount(new BigDecimal("200.00"))
        .timestamp(Instant.now())
        .status("OK")
        .build();

    // When: Se buscan todas las transacciones
    Flux<Transaction> test = transactionRepository
        .deleteAll()
        .then(transactionRepository.save(tx1))
        .then(transactionRepository.save(tx2))
        .thenMany(transactionRepository.findAll());

    // Then: Se encuentran ambas
    StepVerifier.create(test)
        .expectNextCount(2)
        .verifyComplete();
  }
}
