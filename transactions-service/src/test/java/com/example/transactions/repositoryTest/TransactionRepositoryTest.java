package com.example.transactions.repositoryTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import transactions_service.domain.model.Transaction;
import transactions_service.domain.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@TestPropertySource(properties = {
        "spring.data.mongodb.database=test-transactions-db"
})
@DisplayName("Transaction Repository Integration Tests")
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    private Transaction transaction1;
    private Transaction transaction2;
    private Transaction transaction3;
    private Transaction transaction4;

    @BeforeEach
    void setUp() {
        // Limpiar la base de datos antes de cada test
        transactionRepository.deleteAll().block();

        Instant now = Instant.now();

        // Preparar datos de prueba
        transaction1 = Transaction.builder()
                .id("tx-001")
                .accountId("acc-001")
                .type("DEBIT")
                .amount(new BigDecimal("500.00"))
                .timestamp(now.minus(3, ChronoUnit.HOURS))
                .status("OK")
                .reason(null)
                .build();

        transaction2 = Transaction.builder()
                .id("tx-002")
                .accountId("acc-001")
                .type("CREDIT")
                .amount(new BigDecimal("1000.00"))
                .timestamp(now.minus(2, ChronoUnit.HOURS))
                .status("OK")
                .reason(null)
                .build();

        transaction3 = Transaction.builder()
                .id("tx-003")
                .accountId("acc-001")
                .type("DEBIT")
                .amount(new BigDecimal("15000.00"))
                .timestamp(now.minus(1, ChronoUnit.HOURS))
                .status("REJECTED")
                .reason("Exceeds maximum debit limit")
                .build();

        transaction4 = Transaction.builder()
                .id("tx-004")
                .accountId("acc-002")
                .type("DEBIT")
                .amount(new BigDecimal("200.00"))
                .timestamp(now)
                .status("OK")
                .reason(null)
                .build();
    }

    @AfterEach
    void tearDown() {
        // Limpiar después de cada test
        transactionRepository.deleteAll().block();
    }

    @Test
    @DisplayName("Should save transaction successfully")
    void shouldSaveTransactionSuccessfully() {
        // When
        Mono<Transaction> savedTransaction = transactionRepository.save(transaction1);

        // Then
        StepVerifier.create(savedTransaction)
                .assertNext(tx -> {
                    assertNotNull(tx);
                    assertEquals("tx-001", tx.getId());
                    assertEquals("acc-001", tx.getAccountId());
                    assertEquals("DEBIT", tx.getType());
                    assertEquals(new BigDecimal("500.00"), tx.getAmount());
                    assertEquals("OK", tx.getStatus());
                    assertNull(tx.getReason());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should find transaction by id")
    void shouldFindTransactionById() {
        // Given
        transactionRepository.save(transaction1).block();

        // When
        Mono<Transaction> foundTransaction = transactionRepository.findById("tx-001");

        // Then
        StepVerifier.create(foundTransaction)
                .assertNext(tx -> {
                    assertNotNull(tx);
                    assertEquals("tx-001", tx.getId());
                    assertEquals("acc-001", tx.getAccountId());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should find transactions by account id ordered by timestamp desc")
    void shouldFindTransactionsByAccountIdOrderedByTimestampDesc() {
        // Given
        transactionRepository.save(transaction1).block();
        transactionRepository.save(transaction2).block();
        transactionRepository.save(transaction3).block();

        // When
        Flux<Transaction> transactions = transactionRepository
                .findByAccountIdOrderByTimestampDesc("acc-001");

        // Then
        StepVerifier.create(transactions.collectList())
                .assertNext(txList -> {
                    assertEquals(3, txList.size());
                    // Verificar orden descendente por timestamp
                    assertEquals("tx-003", txList.get(0).getId()); // más reciente
                    assertEquals("tx-002", txList.get(1).getId());
                    assertEquals("tx-001", txList.get(2).getId()); // más antiguo
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty flux when no transactions found for account")
    void shouldReturnEmptyFluxWhenNoTransactionsFoundForAccount() {
        // When
        Flux<Transaction> transactions = transactionRepository
                .findByAccountIdOrderByTimestampDesc("acc-999");

        // Then
        StepVerifier.create(transactions)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should find all transactions")
    void shouldFindAllTransactions() {
        // Given
        transactionRepository.save(transaction1).block();
        transactionRepository.save(transaction2).block();
        transactionRepository.save(transaction3).block();
        transactionRepository.save(transaction4).block();

        // When
        Flux<Transaction> allTransactions = transactionRepository.findAll();

        // Then
        StepVerifier.create(allTransactions)
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should count all transactions")
    void shouldCountAllTransactions() {
        // Given
        transactionRepository.save(transaction1).block();
        transactionRepository.save(transaction2).block();
        transactionRepository.save(transaction3).block();

        // When
        Mono<Long> count = transactionRepository.count();

        // Then
        StepVerifier.create(count)
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should save DEBIT transaction")
    void shouldSaveDebitTransaction() {
        // When
        Mono<Transaction> savedTransaction = transactionRepository.save(transaction1);

        // Then
        StepVerifier.create(savedTransaction)
                .assertNext(tx -> {
                    assertEquals("DEBIT", tx.getType());
                    assertEquals("OK", tx.getStatus());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should save CREDIT transaction")
    void shouldSaveCreditTransaction() {
        // When
        Mono<Transaction> savedTransaction = transactionRepository.save(transaction2);

        // Then
        StepVerifier.create(savedTransaction)
                .assertNext(tx -> {
                    assertEquals("CREDIT", tx.getType());
                    assertEquals("OK", tx.getStatus());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should save REJECTED transaction with reason")
    void shouldSaveRejectedTransactionWithReason() {
        // When
        Mono<Transaction> savedTransaction = transactionRepository.save(transaction3);

        // Then
        StepVerifier.create(savedTransaction)
                .assertNext(tx -> {
                    assertEquals("REJECTED", tx.getStatus());
                    assertEquals("Exceeds maximum debit limit", tx.getReason());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should delete transaction by id")
    void shouldDeleteTransactionById() {
        // Given
        transactionRepository.save(transaction1).block();

        // When
        Mono<Void> deleteOperation = transactionRepository.deleteById("tx-001");

        // Then
        StepVerifier.create(deleteOperation)
                .verifyComplete();

        // Verify transaction is deleted
        StepVerifier.create(transactionRepository.findById("tx-001"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should delete transaction by entity")
    void shouldDeleteTransactionByEntity() {
        // Given
        Transaction savedTransaction = transactionRepository.save(transaction1).block();

        // When
        Mono<Void> deleteOperation = transactionRepository.delete(savedTransaction);

        // Then
        StepVerifier.create(deleteOperation)
                .verifyComplete();

        // Verify transaction is deleted
        StepVerifier.create(transactionRepository.findById("tx-001"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should delete all transactions")
    void shouldDeleteAllTransactions() {
        // Given
        transactionRepository.save(transaction1).block();
        transactionRepository.save(transaction2).block();
        transactionRepository.save(transaction3).block();

        // When
        Mono<Void> deleteAll = transactionRepository.deleteAll();

        // Then
        StepVerifier.create(deleteAll)
                .verifyComplete();

        StepVerifier.create(transactionRepository.count())
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should check if transaction exists by id")
    void shouldCheckIfTransactionExistsById() {
        // Given
        transactionRepository.save(transaction1).block();

        // When
        Mono<Boolean> exists = transactionRepository.existsById("tx-001");
        Mono<Boolean> notExists = transactionRepository.existsById("tx-999");

        // Then
        StepVerifier.create(exists)
                .expectNext(true)
                .verifyComplete();

        StepVerifier.create(notExists)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should save multiple transactions")
    void shouldSaveMultipleTransactions() {
        // Given
        Flux<Transaction> transactions = Flux.just(
                transaction1, transaction2, transaction3, transaction4
        );

        // When
        Flux<Transaction> savedTransactions = transactionRepository.saveAll(transactions);

        // Then
        StepVerifier.create(savedTransactions)
                .expectNextCount(4)
                .verifyComplete();

        StepVerifier.create(transactionRepository.count())
                .expectNext(4L)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should update transaction status")
    void shouldUpdateTransactionStatus() {
        // Given
        Transaction savedTransaction = transactionRepository.save(transaction1).block();

        // When
        savedTransaction.setStatus("REJECTED");
        savedTransaction.setReason("Fraud detected");
        Mono<Transaction> updatedTransaction = transactionRepository.save(savedTransaction);

        // Then
        StepVerifier.create(updatedTransaction)
                .assertNext(tx -> {
                    assertEquals("REJECTED", tx.getStatus());
                    assertEquals("Fraud detected", tx.getReason());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle transactions with same timestamp")
    void shouldHandleTransactionsWithSameTimestamp() {
        // Given
        Instant sameTime = Instant.now();

        Transaction tx1 = Transaction.builder()
                .id("tx-same-1")
                .accountId("acc-001")
                .type("DEBIT")
                .amount(new BigDecimal("100.00"))
                .timestamp(sameTime)
                .status("OK")
                .build();

        Transaction tx2 = Transaction.builder()
                .id("tx-same-2")
                .accountId("acc-001")
                .type("CREDIT")
                .amount(new BigDecimal("200.00"))
                .timestamp(sameTime)
                .status("OK")
                .build();

        transactionRepository.save(tx1).block();
        transactionRepository.save(tx2).block();

        // When
        Flux<Transaction> transactions = transactionRepository
                .findByAccountIdOrderByTimestampDesc("acc-001");

        // Then
        StepVerifier.create(transactions)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle large transaction amounts")
    void shouldHandleLargeTransactionAmounts() {
        // Given
        Transaction largeTransaction = Transaction.builder()
                .id("tx-large")
                .accountId("acc-001")
                .type("CREDIT")
                .amount(new BigDecimal("999999999.99"))
                .timestamp(Instant.now())
                .status("OK")
                .build();

        // When
        Mono<Transaction> savedTransaction = transactionRepository.save(largeTransaction);

        // Then
        StepVerifier.create(savedTransaction)
                .assertNext(tx -> {
                    assertEquals(new BigDecimal("999999999.99"), tx.getAmount());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle small transaction amounts")
    void shouldHandleSmallTransactionAmounts() {
        // Given
        Transaction smallTransaction = Transaction.builder()
                .id("tx-small")
                .accountId("acc-001")
                .type("DEBIT")
                .amount(new BigDecimal("0.01"))
                .timestamp(Instant.now())
                .status("OK")
                .build();

        // When
        Mono<Transaction> savedTransaction = transactionRepository.save(smallTransaction);

        // Then
        StepVerifier.create(savedTransaction)
                .assertNext(tx -> {
                    assertEquals(new BigDecimal("0.01"), tx.getAmount());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle null reason for OK transactions")
    void shouldHandleNullReasonForOKTransactions() {
        // When
        Mono<Transaction> savedTransaction = transactionRepository.save(transaction1);

        // Then
        StepVerifier.create(savedTransaction)
                .assertNext(tx -> {
                    assertEquals("OK", tx.getStatus());
                    assertNull(tx.getReason());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should filter transactions by different accounts")
    void shouldFilterTransactionsByDifferentAccounts() {
        // Given
        transactionRepository.save(transaction1).block(); // acc-001
        transactionRepository.save(transaction2).block(); // acc-001
        transactionRepository.save(transaction3).block(); // acc-001
        transactionRepository.save(transaction4).block(); // acc-002

        // When
        Flux<Transaction> acc001Transactions = transactionRepository
                .findByAccountIdOrderByTimestampDesc("acc-001");
        Flux<Transaction> acc002Transactions = transactionRepository
                .findByAccountIdOrderByTimestampDesc("acc-002");

        // Then
        StepVerifier.create(acc001Transactions)
                .expectNextCount(3)
                .verifyComplete();

        StepVerifier.create(acc002Transactions)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should verify timestamp ordering")
    void shouldVerifyTimestampOrdering() {
        // Given
        transactionRepository.save(transaction1).block();
        transactionRepository.save(transaction2).block();
        transactionRepository.save(transaction3).block();

        // When
        Flux<Transaction> transactions = transactionRepository
                .findByAccountIdOrderByTimestampDesc("acc-001");

        // Then
        StepVerifier.create(transactions.collectList())
                .assertNext(txList -> {
                    assertTrue(txList.get(0).getTimestamp()
                            .isAfter(txList.get(1).getTimestamp()));
                    assertTrue(txList.get(1).getTimestamp()
                            .isAfter(txList.get(2).getTimestamp()));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle concurrent transaction saves")
    void shouldHandleConcurrentTransactionSaves() {
        // Given
        Transaction concurrent1 = Transaction.builder()
                .id("concurrent-1")
                .accountId("acc-concurrent")
                .type("DEBIT")
                .amount(new BigDecimal("100.00"))
                .timestamp(Instant.now())
                .status("OK")
                .build();

        Transaction concurrent2 = Transaction.builder()
                .id("concurrent-2")
                .accountId("acc-concurrent")
                .type("CREDIT")
                .amount(new BigDecimal("200.00"))
                .timestamp(Instant.now())
                .status("OK")
                .build();

        // When
        Mono<Transaction> save1 = transactionRepository.save(concurrent1);
        Mono<Transaction> save2 = transactionRepository.save(concurrent2);

        Flux<Transaction> concurrentSaves = Flux.merge(save1, save2);

        // Then
        StepVerifier.create(concurrentSaves)
                .expectNextCount(2)
                .verifyComplete();
    }
}