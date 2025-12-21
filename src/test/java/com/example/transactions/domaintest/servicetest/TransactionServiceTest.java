package com.example.transactions.domaintest.servicetest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;
import transactions.service.domain.dto.CreateTxRequest;
import transactions.service.domain.model.Account;
import transactions.service.domain.model.Transaction;
import transactions.service.domain.repository.AccountRepository;
import transactions.service.domain.repository.TransactionRepository;
import transactions.service.domain.service.RiskService;
import transactions.service.domain.service.TransactionService;
import transactions.service.infrastructure.exception.BusinessException;

/**
 * Tests unitarios para {@link TransactionService}.
 */
@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

  @Mock
  private AccountRepository accountRepo;

  @Mock
  private TransactionRepository txRepo;

  @Mock
  private RiskService riskService;

  @Mock
  private Sinks.Many<Transaction> txSink;

  @InjectMocks
  private TransactionService transactionService;

  private Account testAccount;
  private CreateTxRequest debitRequest;
  private CreateTxRequest creditRequest;

  /**
   * Configura datos de prueba antes de cada test.
   */
  @BeforeEach
  public void setUp() {
    testAccount = Account.builder()
        .id("acc-1")
        .number("1234567890")
        .currency("PEN")
        .balance(new BigDecimal("10000"))
        .build();

    debitRequest = CreateTxRequest.builder()
        .accountNumber("1234567890")
        .type("DEBIT")
        .amount(new BigDecimal("1000"))
        .build();

    creditRequest = CreateTxRequest.builder()
        .accountNumber("1234567890")
        .type("CREDIT")
        .amount(new BigDecimal("500"))
        .build();
  }

  /**
   * Verifica que cree débito exitosamente cuando hay saldo suficiente.
   */
  @Test
  public void shouldCreateDebitWhenSufficientBalance() {
    final Account updatedAccount = Account.builder()
        .id("acc-1")
        .number("1234567890")
        .currency("PEN")
        .balance(new BigDecimal("9000"))
        .build();

    final Transaction savedTx = Transaction.builder()
        .id("tx-1")
        .accountId("acc-1")
        .type("DEBIT")
        .amount(new BigDecimal("1000"))
        .timestamp(Instant.now())
        .status("OK")
        .build();

    when(accountRepo.findByNumber("1234567890"))
        .thenReturn(Mono.just(testAccount));
    when(riskService.isAllowed("PEN", "DEBIT", new BigDecimal("1000")))
        .thenReturn(Mono.just(true));
    when(accountRepo.save(any(Account.class)))
        .thenReturn(Mono.just(updatedAccount));
    when(txRepo.save(any(Transaction.class)))
        .thenReturn(Mono.just(savedTx));
    when(txSink.tryEmitNext(any(Transaction.class)))
        .thenReturn(Sinks.EmitResult.OK);

    final Mono<Transaction> result = transactionService.create(debitRequest);

    StepVerifier.create(result)
        .expectNextMatches(tx ->
            tx.getType().equals("DEBIT")
                && tx.getAmount().equals(new BigDecimal("1000"))
                && tx.getStatus().equals("OK"))
        .verifyComplete();

    verify(accountRepo).save(any(Account.class));
    verify(txRepo).save(any(Transaction.class));
  }

  /**
   * Verifica que cree crédito exitosamente.
   */
  @Test
  public void shouldCreateCreditSuccessfully() {
    final Account updatedAccount = Account.builder()
        .id("acc-1")
        .number("1234567890")
        .currency("PEN")
        .balance(new BigDecimal("10500"))
        .build();

    final Transaction savedTx = Transaction.builder()
        .id("tx-2")
        .accountId("acc-1")
        .type("CREDIT")
        .amount(new BigDecimal("500"))
        .timestamp(Instant.now())
        .status("OK")
        .build();

    when(accountRepo.findByNumber("1234567890"))
        .thenReturn(Mono.just(testAccount));
    when(riskService.isAllowed("PEN", "CREDIT", new BigDecimal("500")))
        .thenReturn(Mono.just(true));
    when(accountRepo.save(any(Account.class)))
        .thenReturn(Mono.just(updatedAccount));
    when(txRepo.save(any(Transaction.class)))
        .thenReturn(Mono.just(savedTx));
    when(txSink.tryEmitNext(any(Transaction.class)))
        .thenReturn(Sinks.EmitResult.OK);

    final Mono<Transaction> result = transactionService.create(creditRequest);

    StepVerifier.create(result)
        .expectNextMatches(tx ->
            tx.getType().equals("CREDIT")
                && tx.getAmount().equals(new BigDecimal("500")))
        .verifyComplete();
  }

  /**
   * Verifica que falle cuando la cuenta no existe.
   */
  @Test
  public void shouldFailWhenAccountNotFound() {
    when(accountRepo.findByNumber(anyString()))
        .thenReturn(Mono.empty());

    final Mono<Transaction> result = transactionService.create(debitRequest);

    StepVerifier.create(result)
        .expectErrorMatches(error ->
            error instanceof BusinessException
                && error.getMessage().equals("account_not_found"))
        .verify();
  }

  /**
   * Verifica que falle cuando hay saldo insuficiente.
   */
  @Test
  public void shouldFailWhenInsufficientFunds() {
    final Account poorAccount = Account.builder()
        .id("acc-1")
        .number("1234567890")
        .currency("PEN")
        .balance(new BigDecimal("500"))
        .build();

    when(accountRepo.findByNumber("1234567890"))
        .thenReturn(Mono.just(poorAccount));
    when(riskService.isAllowed("PEN", "DEBIT", new BigDecimal("1000")))
        .thenReturn(Mono.just(true));

    final Mono<Transaction> result = transactionService.create(debitRequest);

    StepVerifier.create(result)
        .expectErrorMatches(error ->
            error instanceof BusinessException
                && error.getMessage().equals("insufficient_funds"))
        .verify();
  }

  /**
   * Verifica que falle cuando la regla de riesgo rechaza la operación.
   */
  @Test
  public void shouldFailWhenRiskRejected() {
    when(accountRepo.findByNumber("1234567890"))
        .thenReturn(Mono.just(testAccount));
    when(riskService.isAllowed("PEN", "DEBIT", new BigDecimal("1000")))
        .thenReturn(Mono.just(false));

    final Mono<Transaction> result = transactionService.create(debitRequest);

    StepVerifier.create(result)
        .expectErrorMatches(error ->
            error instanceof BusinessException
                && error.getMessage().equals("risk_rejected"))
        .verify();

    verify(accountRepo, never()).save(any());
    verify(txRepo, never()).save(any());
  }

  /**
   * Verifica que obtenga las transacciones de una cuenta.
   */
  @Test
  public void shouldGetTransactionsByAccount() {
    final Transaction tx1 = Transaction.builder()
        .id("tx-1")
        .accountId("acc-1")
        .type("DEBIT")
        .amount(new BigDecimal("100"))
        .timestamp(Instant.now())
        .status("OK")
        .build();

    final Transaction tx2 = Transaction.builder()
        .id("tx-2")
        .accountId("acc-1")
        .type("CREDIT")
        .amount(new BigDecimal("200"))
        .timestamp(Instant.now())
        .status("OK")
        .build();

    when(accountRepo.findByNumber("1234567890"))
        .thenReturn(Mono.just(testAccount));
    when(txRepo.findByAccountIdOrderByTimestampDesc("acc-1"))
        .thenReturn(Flux.just(tx1, tx2));

    final Flux<Transaction> result =
        transactionService.byAccount("1234567890");

    StepVerifier.create(result)
        .expectNext(tx1)
        .expectNext(tx2)
        .verifyComplete();
  }

  /**
   * Verifica que falle al buscar transacciones de cuenta inexistente.
   */
  @Test
  public void shouldFailWhenGettingTransactionsForNonExistentAccount() {
    when(accountRepo.findByNumber(anyString()))
        .thenReturn(Mono.empty());

    final Flux<Transaction> result =
        transactionService.byAccount("9999999999");

    StepVerifier.create(result)
        .expectErrorMatches(error ->
            error instanceof BusinessException
                && error.getMessage().equals("account_not_found"))
        .verify();
  }
}
