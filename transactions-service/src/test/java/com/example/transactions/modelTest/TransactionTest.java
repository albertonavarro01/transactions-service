package com.example.transactions.modelTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import transactions_service.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    @DisplayName("Should create transaction with builder")
    void shouldCreateTransactionWithBuilder() {
        // Given
        Instant now = Instant.now();

        // When
        Transaction transaction = Transaction.builder()
                .id("tx-001")
                .accountId("acc-001")
                .type("DEBIT")
                .amount(new BigDecimal("500.00"))
                .timestamp(now)
                .status("OK")
                .reason(null)
                .build();

        // Then
        assertNotNull(transaction);
        assertEquals("tx-001", transaction.getId());
        assertEquals("acc-001", transaction.getAccountId());
        assertEquals("DEBIT", transaction.getType());
        assertEquals(new BigDecimal("500.00"), transaction.getAmount());
        assertEquals(now, transaction.getTimestamp());
        assertEquals("OK", transaction.getStatus());
        assertNull(transaction.getReason());
    }

    @Test
    @DisplayName("Should create transaction with no args constructor")
    void shouldCreateTransactionWithNoArgsConstructor() {
        // When
        Transaction transaction = new Transaction();

        // Then
        assertNotNull(transaction);
        assertNull(transaction.getId());
        assertNull(transaction.getAccountId());
        assertNull(transaction.getType());
        assertNull(transaction.getAmount());
        assertNull(transaction.getTimestamp());
        assertNull(transaction.getStatus());
        assertNull(transaction.getReason());
    }

    @Test
    @DisplayName("Should create transaction with all args constructor")
    void shouldCreateTransactionWithAllArgsConstructor() {
        // Given
        Instant now = Instant.now();

        // When
        Transaction transaction = new Transaction(
                "tx-002",
                "acc-002",
                "CREDIT",
                new BigDecimal("1000.00"),
                now,
                "OK",
                null
        );

        // Then
        assertNotNull(transaction);
        assertEquals("tx-002", transaction.getId());
        assertEquals("acc-002", transaction.getAccountId());
        assertEquals("CREDIT", transaction.getType());
        assertEquals(new BigDecimal("1000.00"), transaction.getAmount());
        assertEquals(now, transaction.getTimestamp());
        assertEquals("OK", transaction.getStatus());
        assertNull(transaction.getReason());
    }

    @Test
    @DisplayName("Should use setters correctly")
    void shouldUseSettersCorrectly() {
        // Given
        Transaction transaction = new Transaction();
        Instant now = Instant.now();

        // When
        transaction.setId("tx-003");
        transaction.setAccountId("acc-003");
        transaction.setType("DEBIT");
        transaction.setAmount(new BigDecimal("750.00"));
        transaction.setTimestamp(now);
        transaction.setStatus("REJECTED");
        transaction.setReason("Insufficient funds");

        // Then
        assertEquals("tx-003", transaction.getId());
        assertEquals("acc-003", transaction.getAccountId());
        assertEquals("DEBIT", transaction.getType());
        assertEquals(new BigDecimal("750.00"), transaction.getAmount());
        assertEquals(now, transaction.getTimestamp());
        assertEquals("REJECTED", transaction.getStatus());
        assertEquals("Insufficient funds", transaction.getReason());
    }

    @Test
    @DisplayName("Should create DEBIT transaction")
    void shouldCreateDebitTransaction() {
        // Given & When
        Transaction transaction = Transaction.builder()
                .id("tx-004")
                .accountId("acc-004")
                .type("DEBIT")
                .amount(new BigDecimal("250.00"))
                .timestamp(Instant.now())
                .status("OK")
                .build();

        // Then
        assertEquals("DEBIT", transaction.getType());
    }

    @Test
    @DisplayName("Should create CREDIT transaction")
    void shouldCreateCreditTransaction() {
        // Given & When
        Transaction transaction = Transaction.builder()
                .id("tx-005")
                .accountId("acc-005")
                .type("CREDIT")
                .amount(new BigDecimal("1500.00"))
                .timestamp(Instant.now())
                .status("OK")
                .build();

        // Then
        assertEquals("CREDIT", transaction.getType());
    }

    @Test
    @DisplayName("Should create OK transaction without reason")
    void shouldCreateOKTransactionWithoutReason() {
        // Given & When
        Transaction transaction = Transaction.builder()
                .id("tx-006")
                .accountId("acc-006")
                .type("DEBIT")
                .amount(new BigDecimal("300.00"))
                .timestamp(Instant.now())
                .status("OK")
                .reason(null)
                .build();

        // Then
        assertEquals("OK", transaction.getStatus());
        assertNull(transaction.getReason());
    }

    @Test
    @DisplayName("Should create REJECTED transaction with reason")
    void shouldCreateRejectedTransactionWithReason() {
        // Given & When
        Transaction transaction = Transaction.builder()
                .id("tx-007")
                .accountId("acc-007")
                .type("DEBIT")
                .amount(new BigDecimal("15000.00"))
                .timestamp(Instant.now())
                .status("REJECTED")
                .reason("Exceeds maximum debit limit")
                .build();

        // Then
        assertEquals("REJECTED", transaction.getStatus());
        assertEquals("Exceeds maximum debit limit", transaction.getReason());
    }

    @Test
    @DisplayName("Should handle different rejection reasons")
    void shouldHandleDifferentRejectionReasons() {
        // Given & When
        Transaction tx1 = Transaction.builder()
                .id("tx-008")
                .accountId("acc-008")
                .type("DEBIT")
                .amount(new BigDecimal("100.00"))
                .timestamp(Instant.now())
                .status("REJECTED")
                .reason("Insufficient funds")
                .build();

        Transaction tx2 = Transaction.builder()
                .id("tx-009")
                .accountId("acc-009")
                .type("DEBIT")
                .amount(new BigDecimal("20000.00"))
                .timestamp(Instant.now())
                .status("REJECTED")
                .reason("Exceeds risk limit")
                .build();

        // Then
        assertEquals("Insufficient funds", tx1.getReason());
        assertEquals("Exceeds risk limit", tx2.getReason());
    }

    @Test
    @DisplayName("Should handle large amounts")
    void shouldHandleLargeAmounts() {
        // Given & When
        Transaction transaction = Transaction.builder()
                .id("tx-010")
                .accountId("acc-010")
                .type("CREDIT")
                .amount(new BigDecimal("999999.99"))
                .timestamp(Instant.now())
                .status("OK")
                .build();

        // Then
        assertEquals(new BigDecimal("999999.99"), transaction.getAmount());
    }

    @Test
    @DisplayName("Should handle small amounts")
    void shouldHandleSmallAmounts() {
        // Given & When
        Transaction transaction = Transaction.builder()
                .id("tx-011")
                .accountId("acc-011")
                .type("DEBIT")
                .amount(new BigDecimal("0.01"))
                .timestamp(Instant.now())
                .status("OK")
                .build();

        // Then
        assertEquals(new BigDecimal("0.01"), transaction.getAmount());
    }

    @Test
    @DisplayName("Should store timestamp correctly")
    void shouldStoreTimestampCorrectly() {
        // Given
        Instant beforeCreation = Instant.now();

        // When
        Transaction transaction = Transaction.builder()
                .id("tx-012")
                .accountId("acc-012")
                .type("CREDIT")
                .amount(new BigDecimal("500.00"))
                .timestamp(beforeCreation)
                .status("OK")
                .build();

        Instant afterCreation = Instant.now();

        // Then
        assertNotNull(transaction.getTimestamp());
        assertTrue(transaction.getTimestamp().equals(beforeCreation) ||
                transaction.getTimestamp().isBefore(afterCreation));
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void shouldTestEqualsAndHashCode() {
        // Given
        Instant now = Instant.now();

        Transaction tx1 = Transaction.builder()
                .id("tx-013")
                .accountId("acc-013")
                .type("DEBIT")
                .amount(new BigDecimal("100.00"))
                .timestamp(now)
                .status("OK")
                .reason(null)
                .build();

        Transaction tx2 = Transaction.builder()
                .id("tx-013")
                .accountId("acc-013")
                .type("DEBIT")
                .amount(new BigDecimal("100.00"))
                .timestamp(now)
                .status("OK")
                .reason(null)
                .build();

        Transaction tx3 = Transaction.builder()
                .id("tx-014")
                .accountId("acc-014")
                .type("CREDIT")
                .amount(new BigDecimal("200.00"))
                .timestamp(Instant.now())
                .status("OK")
                .reason(null)
                .build();

        // Then
        assertEquals(tx1, tx2);
        assertNotEquals(tx1, tx3);
        assertEquals(tx1.hashCode(), tx2.hashCode());
    }

    @Test
    @DisplayName("Should test toString method")
    void shouldTestToStringMethod() {
        // Given
        Transaction transaction = Transaction.builder()
                .id("tx-015")
                .accountId("acc-015")
                .type("DEBIT")
                .amount(new BigDecimal("350.00"))
                .timestamp(Instant.now())
                .status("OK")
                .build();

        // When
        String result = transaction.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("tx-015"));
        assertTrue(result.contains("acc-015"));
        assertTrue(result.contains("DEBIT"));
        assertTrue(result.contains("OK"));
    }

    @Test
    @DisplayName("Should allow null values")
    void shouldAllowNullValues() {
        // Given & When
        Transaction transaction = Transaction.builder()
                .id(null)
                .accountId(null)
                .type(null)
                .amount(null)
                .timestamp(null)
                .status(null)
                .reason(null)
                .build();

        // Then
        assertNull(transaction.getId());
        assertNull(transaction.getAccountId());
        assertNull(transaction.getType());
        assertNull(transaction.getAmount());
        assertNull(transaction.getTimestamp());
        assertNull(transaction.getStatus());
        assertNull(transaction.getReason());
    }

    @Test
    @DisplayName("Should update status after creation")
    void shouldUpdateStatusAfterCreation() {
        // Given
        Transaction transaction = Transaction.builder()
                .id("tx-016")
                .accountId("acc-016")
                .type("DEBIT")
                .amount(new BigDecimal("500.00"))
                .timestamp(Instant.now())
                .status("OK")
                .build();

        // When
        transaction.setStatus("REJECTED");
        transaction.setReason("Fraud detected");

        // Then
        assertEquals("REJECTED", transaction.getStatus());
        assertEquals("Fraud detected", transaction.getReason());
    }
}