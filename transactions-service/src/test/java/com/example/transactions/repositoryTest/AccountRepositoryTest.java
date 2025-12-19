package com.example.transactions.repositoryTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import transactions_service.domain.model.Account;
import transactions_service.domain.repository.AccountRepository;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para AccountRepository
 * Usa MongoDB embebido para las pruebas
 */
@DataMongoTest
@ActiveProfiles("test")
@DisplayName("Account Repository Integration Tests")
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    private Account testAccount1;
    private Account testAccount2;
    private Account testAccount3;

    @BeforeEach
    void setUp() {
        // Limpiar la base de datos antes de cada test
        accountRepository.deleteAll().block(Duration.ofSeconds(5));

        // Preparar datos de prueba
        testAccount1 = Account.builder()
                .id("acc-001")
                .number("1234567890")
                .holderName("John Doe")
                .currency("PEN")
                .balance(new BigDecimal("5000.00"))
                .build();

        testAccount2 = Account.builder()
                .id("acc-002")
                .number("0987654321")
                .holderName("Jane Smith")
                .currency("USD")
                .balance(new BigDecimal("10000.00"))
                .build();

        testAccount3 = Account.builder()
                .id("acc-003")
                .number("5555666677")
                .holderName("Bob Wilson")
                .currency("PEN")
                .balance(new BigDecimal("2500.00"))
                .build();
    }

    @AfterEach
    void tearDown() {
        // Limpiar después de cada test
        accountRepository.deleteAll().block(Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("Should save account successfully")
    void shouldSaveAccountSuccessfully() {
        // When
        Mono<Account> savedAccount = accountRepository.save(testAccount1);

        // Then
        StepVerifier.create(savedAccount)
                .assertNext(account -> {
                    assertNotNull(account);
                    assertEquals("acc-001", account.getId());
                    assertEquals("1234567890", account.getNumber());
                    assertEquals("John Doe", account.getHolderName());
                    assertEquals("PEN", account.getCurrency());
                    assertEquals(new BigDecimal("5000.00"), account.getBalance());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should find account by id")
    void shouldFindAccountById() {
        // Given
        accountRepository.save(testAccount1).block(Duration.ofSeconds(5));

        // When
        Mono<Account> foundAccount = accountRepository.findById("acc-001");

        // Then
        StepVerifier.create(foundAccount)
                .assertNext(account -> {
                    assertNotNull(account);
                    assertEquals("acc-001", account.getId());
                    assertEquals("1234567890", account.getNumber());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should find account by number")
    void shouldFindAccountByNumber() {
        // Given
        accountRepository.save(testAccount1).block(Duration.ofSeconds(5));

        // When
        Mono<Account> foundAccount = accountRepository.findByNumber("1234567890");

        // Then
        StepVerifier.create(foundAccount)
                .assertNext(account -> {
                    assertNotNull(account);
                    assertEquals("1234567890", account.getNumber());
                    assertEquals("John Doe", account.getHolderName());
                    assertEquals("PEN", account.getCurrency());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty when account number not found")
    void shouldReturnEmptyWhenAccountNumberNotFound() {
        // When
        Mono<Account> foundAccount = accountRepository.findByNumber("9999999999");

        // Then
        StepVerifier.create(foundAccount)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should find all accounts")
    void shouldFindAllAccounts() {
        // Given
        accountRepository.save(testAccount1).block(Duration.ofSeconds(5));
        accountRepository.save(testAccount2).block(Duration.ofSeconds(5));
        accountRepository.save(testAccount3).block(Duration.ofSeconds(5));

        // When
        Flux<Account> allAccounts = accountRepository.findAll();

        // Then
        StepVerifier.create(allAccounts)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should update account balance")
    void shouldUpdateAccountBalance() {
        // Given
        accountRepository.save(testAccount1).block(Duration.ofSeconds(5));

        // When
        Mono<Account> updatedAccount = accountRepository.findById("acc-001")
                .flatMap(account -> {
                    account.setBalance(new BigDecimal("7500.00"));
                    return accountRepository.save(account);
                });

        // Then
        StepVerifier.create(updatedAccount)
                .assertNext(account -> {
                    assertEquals("acc-001", account.getId());
                    assertEquals(new BigDecimal("7500.00"), account.getBalance());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should delete account by id")
    void shouldDeleteAccountById() {
        // Given
        accountRepository.save(testAccount1).block(Duration.ofSeconds(5));

        // When
        Mono<Void> deleteOperation = accountRepository.deleteById("acc-001");

        // Then
        StepVerifier.create(deleteOperation)
                .verifyComplete();

        // Verify account is deleted
        StepVerifier.create(accountRepository.findById("acc-001"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should delete account by entity")
    void shouldDeleteAccountByEntity() {
        // Given
        Account savedAccount = accountRepository.save(testAccount1).block(Duration.ofSeconds(5));

        // When
        Mono<Void> deleteOperation = accountRepository.delete(savedAccount);

        // Then
        StepVerifier.create(deleteOperation)
                .verifyComplete();

        // Verify account is deleted
        StepVerifier.create(accountRepository.findById("acc-001"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should count all accounts")
    void shouldCountAllAccounts() {
        // Given
        accountRepository.save(testAccount1).block(Duration.ofSeconds(5));
        accountRepository.save(testAccount2).block(Duration.ofSeconds(5));

        // When
        Mono<Long> count = accountRepository.count();

        // Then
        StepVerifier.create(count)
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should check if account exists by id")
    void shouldCheckIfAccountExistsById() {
        // Given
        accountRepository.save(testAccount1).block(Duration.ofSeconds(5));

        // When
        Mono<Boolean> exists = accountRepository.existsById("acc-001");
        Mono<Boolean> notExists = accountRepository.existsById("acc-999");

        // Then
        StepVerifier.create(exists)
                .expectNext(true)
                .verifyComplete();

        StepVerifier.create(notExists)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should delete all accounts")
    void shouldDeleteAllAccounts() {
        // Given
        accountRepository.save(testAccount1).block(Duration.ofSeconds(5));
        accountRepository.save(testAccount2).block(Duration.ofSeconds(5));
        accountRepository.save(testAccount3).block(Duration.ofSeconds(5));

        // When
        Mono<Void> deleteAll = accountRepository.deleteAll();

        // Then
        StepVerifier.create(deleteAll)
                .verifyComplete();

        StepVerifier.create(accountRepository.count())
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should save multiple accounts")
    void shouldSaveMultipleAccounts() {
        // Given
        Flux<Account> accounts = Flux.just(testAccount1, testAccount2, testAccount3);

        // When
        Flux<Account> savedAccounts = accountRepository.saveAll(accounts);

        // Then
        StepVerifier.create(savedAccounts)
                .expectNextCount(3)
                .verifyComplete();

        StepVerifier.create(accountRepository.count())
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle concurrent saves")
    void shouldHandleConcurrentSaves() {
        // Given
        Account account1 = Account.builder()
                .id("concurrent-001")
                .number("1111111111")
                .holderName("Concurrent User 1")
                .currency("PEN")
                .balance(new BigDecimal("1000.00"))
                .build();

        Account account2 = Account.builder()
                .id("concurrent-002")
                .number("2222222222")
                .holderName("Concurrent User 2")
                .currency("USD")
                .balance(new BigDecimal("2000.00"))
                .build();

        // When
        Mono<Account> save1 = accountRepository.save(account1);
        Mono<Account> save2 = accountRepository.save(account2);

        Flux<Account> concurrentSaves = Flux.merge(save1, save2);

        // Then
        StepVerifier.create(concurrentSaves)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should find account by number with special characters")
    void shouldFindAccountByNumberWithSpecialCharacters() {
        // Given
        Account specialAccount = Account.builder()
                .id("special-001")
                .number("1234-5678-90")
                .holderName("Special Account")
                .currency("PEN")
                .balance(new BigDecimal("3000.00"))
                .build();

        accountRepository.save(specialAccount).block(Duration.ofSeconds(5));

        // When
        Mono<Account> foundAccount = accountRepository.findByNumber("1234-5678-90");

        // Then
        StepVerifier.create(foundAccount)
                .assertNext(account -> {
                    assertEquals("1234-5678-90", account.getNumber());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle null balance")
    void shouldHandleNullBalance() {
        // Given
        Account accountWithNullBalance = Account.builder()
                .id("null-balance-001")
                .number("9999999999")
                .holderName("Null Balance User")
                .currency("PEN")
                .balance(null)
                .build();

        // When
        Mono<Account> savedAccount = accountRepository.save(accountWithNullBalance);

        // Then
        StepVerifier.create(savedAccount)
                .assertNext(account -> {
                    assertEquals("null-balance-001", account.getId());
                    assertNull(account.getBalance());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should update multiple fields")
    void shouldUpdateMultipleFields() {
        // Given
        accountRepository.save(testAccount1).block(Duration.ofSeconds(5));

        // When
        Mono<Account> updatedAccount = accountRepository.findById("acc-001")
                .flatMap(account -> {
                    account.setHolderName("John Doe Updated");
                    account.setBalance(new BigDecimal("15000.00"));
                    account.setCurrency("USD");
                    return accountRepository.save(account);
                });

        // Then
        StepVerifier.create(updatedAccount)
                .assertNext(account -> {
                    assertEquals("John Doe Updated", account.getHolderName());
                    assertEquals(new BigDecimal("15000.00"), account.getBalance());
                    assertEquals("USD", account.getCurrency());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should verify reactive timeout")
    void shouldVerifyReactiveTimeout() {
        // Given
        accountRepository.save(testAccount1).block(Duration.ofSeconds(5));

        // When
        Mono<Account> accountWithTimeout = accountRepository.findById("acc-001")
                .timeout(Duration.ofSeconds(5));

        // Then
        StepVerifier.create(accountWithTimeout)
                .assertNext(account -> {
                    assertNotNull(account);
                    assertEquals("acc-001", account.getId());
                })
                .verifyComplete();
    }
}