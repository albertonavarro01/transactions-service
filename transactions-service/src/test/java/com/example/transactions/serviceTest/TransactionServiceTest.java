package com.example.transactions.serviceTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;
import transactions_service.domain.dto.CreateTxRequest;
import transactions_service.domain.model.Account;
import transactions_service.domain.model.Transaction;
import transactions_service.domain.repository.AccountRepository;
import transactions_service.domain.repository.TransactionRepository;
import transactions_service.domain.service.RiskService;
import transactions_service.domain.service.TransactionService;
import transactions_service.infrastructure.exception.BusinessException;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id(String.valueOf(1L))
                .number("ACC-001")
                .balance(new BigDecimal("1000.00"))
                .currency("USD")
                .build();

        debitRequest = CreateTxRequest.builder()
                .accountNumber("ACC-001")
                .type("DEBIT")
                .amount(new BigDecimal("100.00"))
                .build();

        creditRequest = CreateTxRequest.builder()
                .accountNumber("ACC-001")
                .type("CREDIT")
                .amount(new BigDecimal("200.00"))
                .build();
    }

    @Test
    void create_DebitTransaction_Success() {
        // Arrange
        when(accountRepo.findByNumber("ACC-001")).thenReturn(Mono.just(testAccount));
        when(riskService.isAllowed("USD", "DEBIT", new BigDecimal("100.00")))
                .thenReturn(Mono.just(true));

        Account updatedAccount = Account.builder()
                .id(String.valueOf(1L))
                .number("ACC-001")
                .balance(new BigDecimal("900.00"))
                .currency("USD")
                .build();
        when(accountRepo.save(any(Account.class))).thenReturn(Mono.just(updatedAccount));

        Transaction savedTx = Transaction.builder()
                .id(String.valueOf(1L))
                .accountId(String.valueOf(1L))
                .type("DEBIT")
                .amount(new BigDecimal("100.00"))
                .timestamp(Instant.now())
                .status("OK")
                .build();
        when(txRepo.save(any(Transaction.class))).thenReturn(Mono.just(savedTx));
        when(txSink.tryEmitNext(any())).thenReturn(Sinks.EmitResult.OK);

        // Act & Assert
        StepVerifier.create(transactionService.create(debitRequest))
                .assertNext(tx -> {
                    assertThat(tx.getType()).isEqualTo("DEBIT");
                    assertThat(tx.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
                    assertThat(tx.getStatus()).isEqualTo("OK");
                    assertThat(tx.getAccountId()).isEqualTo(1L);
                })
                .verifyComplete();

        // Verify account balance was updated
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepo).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getBalance())
                .isEqualByComparingTo(new BigDecimal("900.00"));
    }

    @Test
    void create_CreditTransaction_Success() {
        // Arrange
        when(accountRepo.findByNumber("ACC-001")).thenReturn(Mono.just(testAccount));
        when(riskService.isAllowed("USD", "CREDIT", new BigDecimal("200.00")))
                .thenReturn(Mono.just(true));

        Account updatedAccount = Account.builder()
                .id(String.valueOf(1L))
                .number("ACC-001")
                .balance(new BigDecimal("1200.00"))
                .currency("USD")
                .build();
        when(accountRepo.save(any(Account.class))).thenReturn(Mono.just(updatedAccount));

        Transaction savedTx = Transaction.builder()
                .id(String.valueOf(2L))
                .accountId(String.valueOf(1L))
                .type("CREDIT")
                .amount(new BigDecimal("200.00"))
                .timestamp(Instant.now())
                .status("OK")
                .build();
        when(txRepo.save(any(Transaction.class))).thenReturn(Mono.just(savedTx));
        when(txSink.tryEmitNext(any())).thenReturn(Sinks.EmitResult.OK);

        // Act & Assert
        StepVerifier.create(transactionService.create(creditRequest))
                .assertNext(tx -> {
                    assertThat(tx.getType()).isEqualTo("CREDIT");
                    assertThat(tx.getAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
                })
                .verifyComplete();

        // Verify balance increased
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepo).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getBalance())
                .isEqualByComparingTo(new BigDecimal("1200.00"));
    }

    @Test
    void create_AccountNotFound_ThrowsException() {
        // Arrange
        when(accountRepo.findByNumber("ACC-999")).thenReturn(Mono.empty());

        CreateTxRequest request = CreateTxRequest.builder()
                .accountNumber("ACC-999")
                .type("DEBIT")
                .amount(new BigDecimal("100.00"))
                .build();

        // Act & Assert
        StepVerifier.create(transactionService.create(request))
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        e.getMessage().equals("account_not_found"))
                .verify();
    }

    @Test
    void create_InsufficientFunds_ThrowsException() {
        // Arrange
        when(accountRepo.findByNumber("ACC-001")).thenReturn(Mono.just(testAccount));
        when(riskService.isAllowed("USD", "DEBIT", new BigDecimal("2000.00")))
                .thenReturn(Mono.just(true));

        CreateTxRequest request = CreateTxRequest.builder()
                .accountNumber("ACC-001")
                .type("DEBIT")
                .amount(new BigDecimal("2000.00"))
                .build();

        // Act & Assert
        StepVerifier.create(transactionService.create(request))
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        e.getMessage().equals("insufficient_funds"))
                .verify();

        verify(accountRepo, never()).save(any());
        verify(txRepo, never()).save(any());
    }

    @Test
    void create_RiskRejected_ThrowsException() {
        // Arrange
        when(accountRepo.findByNumber("ACC-001")).thenReturn(Mono.just(testAccount));
        when(riskService.isAllowed("USD", "DEBIT", new BigDecimal("100.00")))
                .thenReturn(Mono.just(false));

        // Act & Assert
        StepVerifier.create(transactionService.create(debitRequest))
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        e.getMessage().equals("risk_rejected"))
                .verify();

        verify(accountRepo, never()).save(any());
        verify(txRepo, never()).save(any());
    }

    @Test
    void byAccount_Success() {
        // Arrange
        when(accountRepo.findByNumber("ACC-001")).thenReturn(Mono.just(testAccount));

        Transaction tx1 = Transaction.builder()
                .id(String.valueOf(1L))
                .accountId(String.valueOf(1L))
                .type("DEBIT")
                .amount(new BigDecimal("50.00"))
                .timestamp(Instant.now())
                .status("OK")
                .build();

        Transaction tx2 = Transaction.builder()
                .id(String.valueOf(2L))
                .accountId(String.valueOf(1L))
                .type("CREDIT")
                .amount(new BigDecimal("100.00"))
                .timestamp(Instant.now())
                .status("OK")
                .build();

        when(txRepo.findByAccountIdOrderByTimestampDesc(1L))
                .thenReturn(Flux.just(tx1, tx2));

        // Act & Assert
        StepVerifier.create(transactionService.byAccount("ACC-001"))
                .assertNext(tx -> assertThat(tx.getId()).isEqualTo(1L))
                .assertNext(tx -> assertThat(tx.getId()).isEqualTo(2L))
                .verifyComplete();
    }

    @Test
    void byAccount_AccountNotFound_ThrowsException() {
        // Arrange
        when(accountRepo.findByNumber("ACC-999")).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(transactionService.byAccount("ACC-999"))
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        e.getMessage().equals("account_not_found"))
                .verify();
    }

    @Test
    void stream_ReturnsServerSentEvents() {
        // Arrange
        Transaction tx = Transaction.builder()
                .id(String.valueOf(1L))
                .accountId(String.valueOf(1L))
                .type("DEBIT")
                .amount(new BigDecimal("100.00"))
                .timestamp(Instant.now())
                .status("OK")
                .build();

        Flux<Transaction> txFlux = Flux.just(tx);
        when(txSink.asFlux()).thenReturn(txFlux);

        // Act & Assert
        StepVerifier.create(transactionService.stream())
                .assertNext(sse -> {
                    assertThat(sse.event()).isEqualTo("transaction");
                    assertThat(sse.data()).isNotNull();
                    assertThat(sse.data().getId()).isEqualTo(1L);
                })
                .verifyComplete();
    }

    @Test
    void create_TypeConvertedToUpperCase() {
        // Arrange
        CreateTxRequest lowerCaseRequest = CreateTxRequest.builder()
                .accountNumber("ACC-001")
                .type("debit")
                .amount(new BigDecimal("100.00"))
                .build();

        when(accountRepo.findByNumber("ACC-001")).thenReturn(Mono.just(testAccount));
        when(riskService.isAllowed(eq("USD"), eq("DEBIT"), any()))
                .thenReturn(Mono.just(true));
        when(accountRepo.save(any())).thenReturn(Mono.just(testAccount));

        Transaction savedTx = Transaction.builder()
                .id(String.valueOf(1L))
                .accountId(String.valueOf(1L))
                .type("DEBIT")
                .amount(new BigDecimal("100.00"))
                .timestamp(Instant.now())
                .status("OK")
                .build();
        when(txRepo.save(any())).thenReturn(Mono.just(savedTx));
        when(txSink.tryEmitNext(any())).thenReturn(Sinks.EmitResult.OK);

        // Act & Assert
        StepVerifier.create(transactionService.create(lowerCaseRequest))
                .assertNext(tx -> assertThat(tx.getType()).isEqualTo("DEBIT"))
                .verifyComplete();
    }
}
