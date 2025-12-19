package com.example.transactions.modelTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import transactions_service.domain.model.Account;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class AccountTest {
    @Test
    @DisplayName("Should create account with builder")
    void shouldCreateAccountWithBuilder() {
        // Given & When
        Account account = Account.builder()
                .id("acc-001")
                .number("1234567890")
                .holderName("John Doe")
                .currency("PEN")
                .balance(new BigDecimal("1000.00"))
                .build();

        // Then
        assertNotNull(account);
        assertEquals("acc-001", account.getId());
        assertEquals("1234567890", account.getNumber());
        assertEquals("John Doe", account.getHolderName());
        assertEquals("PEN", account.getCurrency());
        assertEquals(new BigDecimal("1000.00"), account.getBalance());
    }

    @Test
    @DisplayName("Should create account with no args constructor")
    void shouldCreateAccountWithNoArgsConstructor() {
        // When
        Account account = new Account();

        // Then
        assertNotNull(account);
        assertNull(account.getId());
        assertNull(account.getNumber());
        assertNull(account.getHolderName());
        assertNull(account.getCurrency());
        assertNull(account.getBalance());
    }

    @Test
    @DisplayName("Should create account with all args constructor")
    void shouldCreateAccountWithAllArgsConstructor() {
        // When
        Account account = new Account(
                "acc-002",
                "9876543210",
                "Jane Smith",
                "USD",
                new BigDecimal("5000.00")
        );

        // Then
        assertNotNull(account);
        assertEquals("acc-002", account.getId());
        assertEquals("9876543210", account.getNumber());
        assertEquals("Jane Smith", account.getHolderName());
        assertEquals("USD", account.getCurrency());
        assertEquals(new BigDecimal("5000.00"), account.getBalance());
    }

    @Test
    @DisplayName("Should use setters correctly")
    void shouldUseSettersCorrectly() {
        // Given
        Account account = new Account();

        // When
        account.setId("acc-003");
        account.setNumber("1111222233");
        account.setHolderName("Alice Brown");
        account.setCurrency("PEN");
        account.setBalance(new BigDecimal("2500.50"));

        // Then
        assertEquals("acc-003", account.getId());
        assertEquals("1111222233", account.getNumber());
        assertEquals("Alice Brown", account.getHolderName());
        assertEquals("PEN", account.getCurrency());
        assertEquals(new BigDecimal("2500.50"), account.getBalance());
    }

    @Test
    @DisplayName("Should handle USD currency")
    void shouldHandleUSDCurrency() {
        // Given & When
        Account account = Account.builder()
                .id("acc-004")
                .number("5555666677")
                .holderName("Bob Wilson")
                .currency("USD")
                .balance(new BigDecimal("10000.00"))
                .build();

        // Then
        assertEquals("USD", account.getCurrency());
    }

    @Test
    @DisplayName("Should handle zero balance")
    void shouldHandleZeroBalance() {
        // Given & When
        Account account = Account.builder()
                .id("acc-005")
                .number("9999888877")
                .holderName("Charlie Davis")
                .currency("PEN")
                .balance(BigDecimal.ZERO)
                .build();

        // Then
        assertEquals(BigDecimal.ZERO, account.getBalance());
    }

    @Test
    @DisplayName("Should handle negative balance")
    void shouldHandleNegativeBalance() {
        // Given & When
        Account account = Account.builder()
                .id("acc-006")
                .number("7777666655")
                .holderName("Diana Prince")
                .currency("USD")
                .balance(new BigDecimal("-500.00"))
                .build();

        // Then
        assertTrue(account.getBalance().compareTo(BigDecimal.ZERO) < 0);
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void shouldTestEqualsAndHashCode() {
        // Given
        Account account1 = Account.builder()
                .id("acc-007")
                .number("1234567890")
                .holderName("Test User")
                .currency("PEN")
                .balance(new BigDecimal("1000.00"))
                .build();

        Account account2 = Account.builder()
                .id("acc-007")
                .number("1234567890")
                .holderName("Test User")
                .currency("PEN")
                .balance(new BigDecimal("1000.00"))
                .build();

        Account account3 = Account.builder()
                .id("acc-008")
                .number("0987654321")
                .holderName("Different User")
                .currency("USD")
                .balance(new BigDecimal("2000.00"))
                .build();

        // Then
        assertEquals(account1, account2);
        assertNotEquals(account1, account3);
        assertEquals(account1.hashCode(), account2.hashCode());
    }

    @Test
    @DisplayName("Should test toString method")
    void shouldTestToStringMethod() {
        // Given
        Account account = Account.builder()
                .id("acc-009")
                .number("1231231234")
                .holderName("Test ToString")
                .currency("PEN")
                .balance(new BigDecimal("3000.00"))
                .build();

        // When
        String result = account.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("acc-009"));
        assertTrue(result.contains("1231231234"));
        assertTrue(result.contains("Test ToString"));
        assertTrue(result.contains("PEN"));
    }

    @Test
    @DisplayName("Should allow null values")
    void shouldAllowNullValues() {
        // Given & When
        Account account = Account.builder()
                .id(null)
                .number(null)
                .holderName(null)
                .currency(null)
                .balance(null)
                .build();

        // Then
        assertNull(account.getId());
        assertNull(account.getNumber());
        assertNull(account.getHolderName());
        assertNull(account.getCurrency());
        assertNull(account.getBalance());
    }
}

