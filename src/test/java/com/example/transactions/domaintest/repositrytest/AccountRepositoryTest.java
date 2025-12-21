package com.example.transactions.domaintest.repositrytest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import transactions.service.TransactionsServiceApplication;
import transactions.service.domain.model.Account;
import transactions.service.domain.repository.AccountRepository;

/**
 * Tests de integración para {@link AccountRepository}.
 * Usa @DataMongoTest para probar solo la capa de datos con MongoDB embebido.
 */
@DataMongoTest
@ContextConfiguration(classes = TransactionsServiceApplication.class)
class AccountRepositoryTest {

  @Autowired
  private AccountRepository accountRepository;

  /**
   * Verifica que se pueda guardar y buscar una cuenta por su número.
   */
  @Test
  void shouldFindAccountByNumber() {
    // Given: Una cuenta válida
    Account acc = Account.builder()
        .id("acc-1")
        .number("001-0001")
        .holderName("Ana Peru")
        .currency("PEN")
        .balance(new BigDecimal("2000"))
        .status("ACTIVE")
        .type("CHECKING")
        .build();

    // When: Se guarda y se busca por número
    Mono<Account> test = accountRepository
        .deleteAll()
        .then(accountRepository.save(acc))
        .then(accountRepository.findByNumber("001-0001"));

    // Then: Se encuentra la cuenta correctamente
    StepVerifier.create(test)
        .assertNext(found -> {
          assertEquals("acc-1", found.getId());
          assertEquals("001-0001", found.getNumber());
          assertEquals("Ana Peru", found.getHolderName());
          assertEquals("PEN", found.getCurrency());
          assertEquals(0, new BigDecimal("2000").compareTo(found.getBalance()));
        })
        .verifyComplete();
  }

  /**
   * Verifica que devuelva vacío si no existe una cuenta con ese número.
   */
  @Test
  void shouldReturnEmptyWhenAccountDoesNotExist() {
    // When: Se busca una cuenta que no existe
    Mono<Account> test = accountRepository
        .deleteAll()
        .then(accountRepository.findByNumber("NO-EXISTE"));

    // Then: No se encuentra nada
    StepVerifier.create(test)
        .verifyComplete();
  }

  /**
   * Verifica que se puedan guardar múltiples cuentas.
   */
  @Test
  void shouldSaveMultipleAccounts() {
    // Given: Dos cuentas diferentes
    Account acc1 = Account.builder()
        .id("acc-1")
        .number("001-0001")
        .holderName("Juan Pérez")
        .currency("PEN")
        .balance(new BigDecimal("1000"))
        .build();

    Account acc2 = Account.builder()
        .id("acc-2")
        .number("001-0002")
        .holderName("María García")
        .currency("USD")
        .balance(new BigDecimal("500"))
        .build();

    // When: Se guardan ambas cuentas
    Mono<Long> test = accountRepository
        .deleteAll()
        .then(accountRepository.save(acc1))
        .then(accountRepository.save(acc2))
        .then(accountRepository.count());

    // Then: Se cuentan 2 cuentas en la BD
    StepVerifier.create(test)
        .assertNext(count -> assertEquals(2L, count))
        .verifyComplete();
  }

  /**
   * Verifica que se pueda actualizar el saldo de una cuenta.
   */
  @Test
  void shouldUpdateAccountBalance() {
    // Given: Una cuenta con saldo inicial
    Account acc = Account.builder()
        .id("acc-update")
        .number("001-0003")
        .holderName("Pedro López")
        .currency("PEN")
        .balance(new BigDecimal("1000"))
        .build();

    // When: Se guarda, se actualiza el saldo y se vuelve a guardar
    Mono<Account> test = accountRepository
        .deleteAll()
        .then(accountRepository.save(acc))
        .flatMap(saved -> {
          saved.setBalance(new BigDecimal("750")); // Debito de 250
          return accountRepository.save(saved);
        })
        .flatMap(updated -> accountRepository.findByNumber("001-0003"));

    // Then: El saldo se actualizó correctamente
    StepVerifier.create(test)
        .assertNext(found -> {
          assertEquals(0, new BigDecimal("750").compareTo(found.getBalance()));
        })
        .verifyComplete();
  }

  /**
   * Verifica que se pueda eliminar una cuenta por ID.
   */
  @Test
  void shouldDeleteAccountById() {
    // Given: Una cuenta guardada
    Account acc = Account.builder()
        .id("acc-delete")
        .number("001-0004")
        .holderName("Luis Martínez")
        .currency("PEN")
        .balance(new BigDecimal("500"))
        .build();

    // When: Se guarda y luego se elimina
    Mono<Account> test = accountRepository
        .deleteAll()
        .then(accountRepository.save(acc))
        .then(accountRepository.deleteById("acc-delete"))
        .then(accountRepository.findById("acc-delete"));

    // Then: Ya no existe en la BD
    StepVerifier.create(test)
        .verifyComplete();
  }

  /**
   * Verifica que findById retorne la cuenta correcta.
   */
  @Test
  void shouldFindAccountById() {
    // Given: Una cuenta guardada
    Account acc = Account.builder()
        .id("acc-find-id")
        .number("001-0005")
        .holderName("Carla Rodríguez")
        .currency("USD")
        .balance(new BigDecimal("2500"))
        .build();

    // When: Se busca por ID
    Mono<Account> test = accountRepository
        .deleteAll()
        .then(accountRepository.save(acc))
        .then(accountRepository.findById("acc-find-id"));

    // Then: Se encuentra correctamente
    StepVerifier.create(test)
        .assertNext(found -> {
          assertEquals("acc-find-id", found.getId());
          assertEquals("001-0005", found.getNumber());
          assertEquals("Carla Rodríguez", found.getHolderName());
        })
        .verifyComplete();
  }

  /**
   * Verifica que se manejen cuentas con diferentes monedas.
   */
  @Test
  void shouldHandleDifferentCurrencies() {
    // Given: Cuentas en PEN y USD
    Account accPen = Account.builder()
        .id("acc-pen")
        .number("002-0001")
        .currency("PEN")
        .balance(new BigDecimal("1000"))
        .build();

    Account accUsd = Account.builder()
        .id("acc-usd")
        .number("002-0002")
        .currency("USD")
        .balance(new BigDecimal("300"))
        .build();

    // When: Se guardan ambas
    Mono<Account> testPen = accountRepository
        .deleteAll()
        .then(accountRepository.save(accPen))
        .then(accountRepository.save(accUsd))
        .then(accountRepository.findByNumber("002-0001"));

    Mono<Account> testUsd = accountRepository.findByNumber("002-0002");

    // Then: Ambas se encuentran con su moneda correcta
    StepVerifier.create(testPen)
        .assertNext(found -> assertEquals("PEN", found.getCurrency()))
        .verifyComplete();

    StepVerifier.create(testUsd)
        .assertNext(found -> assertEquals("USD", found.getCurrency()))
        .verifyComplete();
  }
}
