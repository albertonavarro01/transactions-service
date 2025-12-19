package com.example.transactions.serviceTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;
import transactions_service.domain.model.RiskRule;
import transactions_service.domain.repository.RiskRuleRepository;
import transactions_service.domain.service.RiskService;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RiskService Tests")
public class RiskServiceTest {

    @Mock
    private RiskRuleRepository riskRuleRepository;

    @InjectMocks
    private RiskService riskService;

    private RiskRule penRule;
    private RiskRule usdRule;

    @BeforeEach
    void setUp() {
        penRule = RiskRule.builder()
                .id("rule-pen-001")
                .currency("PEN")
                .maxDebitPerTx(new BigDecimal("10000.00"))
                .build();

        usdRule = RiskRule.builder()
                .id("rule-usd-001")
                .currency("USD")
                .maxDebitPerTx(new BigDecimal("3000.00"))
                .build();
    }

    @Nested
    @DisplayName("DEBIT Transaction Tests")
    class DebitTransactionTests {

        @Test
        @DisplayName("Should allow DEBIT when amount is below limit")
        void shouldAllowDebitWhenAmountIsBelowLimit() {
            // Given
            when(riskRuleRepository.findFirstByCurrency("PEN"))
                    .thenReturn(Optional.of(penRule));

            // When & Then
            StepVerifier.create(riskService.isAllowed("PEN", "DEBIT", new BigDecimal("5000.00")))
                    .expectNext(true)
                    .verifyComplete();

            verify(riskRuleRepository, times(1)).findFirstByCurrency("PEN");
        }

        @Test
        @DisplayName("Should allow DEBIT when amount equals limit")
        void shouldAllowDebitWhenAmountEqualsLimit() {
            // Given
            when(riskRuleRepository.findFirstByCurrency("PEN"))
                    .thenReturn(Optional.of(penRule));

            // When & Then
            StepVerifier.create(riskService.isAllowed("PEN", "DEBIT", new BigDecimal("10000.00")))
                    .expectNext(true)
                    .verifyComplete();

            verify(riskRuleRepository, times(1)).findFirstByCurrency("PEN");
        }

        @Test
        @DisplayName("Should reject DEBIT when amount exceeds limit")
        void shouldRejectDebitWhenAmountExceedsLimit() {
            // Given
            when(riskRuleRepository.findFirstByCurrency("PEN"))
                    .thenReturn(Optional.of(penRule));

            // When & Then
            StepVerifier.create(riskService.isAllowed("PEN", "DEBIT", new BigDecimal("15000.00")))
                    .expectNext(false)
                    .verifyComplete();

            verify(riskRuleRepository, times(1)).findFirstByCurrency("PEN");
        }

        @Test
        @DisplayName("Should reject DEBIT when no rule found for currency")
        void shouldRejectDebitWhenNoRuleFoundForCurrency() {
            // Given
            when(riskRuleRepository.findFirstByCurrency("EUR"))
                    .thenReturn(Optional.empty());

            // When & Then
            StepVerifier.create(riskService.isAllowed("EUR", "DEBIT", new BigDecimal("100.00")))
                    .expectNext(false)
                    .verifyComplete();

            verify(riskRuleRepository, times(1)).findFirstByCurrency("EUR");
        }

        @Test
        @DisplayName("Should allow small DEBIT amount")
        void shouldAllowSmallDebitAmount() {
            // Given
            when(riskRuleRepository.findFirstByCurrency("USD"))
                    .thenReturn(Optional.of(usdRule));

            // When & Then
            StepVerifier.create(riskService.isAllowed("USD", "DEBIT", new BigDecimal("0.01")))
                    .expectNext(true)
                    .verifyComplete();

            verify(riskRuleRepository, times(1)).findFirstByCurrency("USD");
        }

        @Test
        @DisplayName("Should handle DEBIT with case insensitive type")
        void shouldHandleDebitWithCaseInsensitiveType() {
            // Given
            when(riskRuleRepository.findFirstByCurrency("PEN"))
                    .thenReturn(Optional.of(penRule));

            // When & Then
            StepVerifier.create(riskService.isAllowed("PEN", "debit", new BigDecimal("5000.00")))
                    .expectNext(true)
                    .verifyComplete();

            StepVerifier.create(riskService.isAllowed("PEN", "DeBiT", new BigDecimal("5000.00")))
                    .expectNext(true)
                    .verifyComplete();

            verify(riskRuleRepository, times(2)).findFirstByCurrency("PEN");
        }
    }

    @Nested
    @DisplayName("CREDIT Transaction Tests")
    class CreditTransactionTests {

        @Test
        @DisplayName("Should always allow CREDIT transactions")
        void shouldAlwaysAllowCreditTransactions() {
            // Given
            when(riskRuleRepository.findFirstByCurrency("PEN"))
                    .thenReturn(Optional.of(penRule));

            // When & Then
            StepVerifier.create(riskService.isAllowed("PEN", "CREDIT", new BigDecimal("50000.00")))
                    .expectNext(true)
                    .verifyComplete();

            verify(riskRuleRepository, times(1)).findFirstByCurrency("PEN");
        }

        @Test
        @DisplayName("Should allow CREDIT even without rule")
        void shouldAllowCreditEvenWithoutRule() {
            // Given
            when(riskRuleRepository.findFirstByCurrency("EUR"))
                    .thenReturn(Optional.empty());

            // When & Then
            StepVerifier.create(riskService.isAllowed("EUR", "CREDIT", new BigDecimal("100000.00")))
                    .expectNext(true)
                    .verifyComplete();

            verify(riskRuleRepository, times(1)).findFirstByCurrency("EUR");
        }

        @Test
        @DisplayName("Should allow CREDIT with case insensitive type")
        void shouldAllowCreditWithCaseInsensitiveType() {
            // Given
            when(riskRuleRepository.findFirstByCurrency("USD"))
                    .thenReturn(Optional.of(usdRule));

            // When & Then
            StepVerifier.create(riskService.isAllowed("USD", "credit", new BigDecimal("10000.00")))
                    .expectNext(true)
                    .verifyComplete();

            StepVerifier.create(riskService.isAllowed("USD", "CrEdIt", new BigDecimal("10000.00")))
                    .expectNext(true)
                    .verifyComplete();

            verify(riskRuleRepository, times(2)).findFirstByCurrency("USD");
        }

        @Test
        @DisplayName("Should allow very large CREDIT amounts")
        void shouldAllowVeryLargeCreditAmounts() {
            // Given
            when(riskRuleRepository.findFirstByCurrency("PEN"))
                    .thenReturn(Optional.of(penRule));

            // When & Then
            StepVerifier.create(riskService.isAllowed("PEN", "CREDIT", new BigDecimal("999999999.99")))
                    .expectNext(true)
                    .verifyComplete();

            verify(riskRuleRepository, times(1)).findFirstByCurrency("PEN");
        }
    }

    @Nested
    @DisplayName("Currency Specific Tests")
    class CurrencySpecificTests {

        @Test
        @DisplayName("Should apply PEN rules correctly")
        void shouldApplyPENRulesCorrectly() {
            // Given
            when(riskRuleRepository.findFirstByCurrency("PEN"))
                    .thenReturn(Optional.of(penRule));

            // When & Then - Below limit
            StepVerifier.create(riskService.isAllowed("PEN", "DEBIT", new BigDecimal("9999.99")))
                    .expectNext(true)
                    .verifyComplete();

            // Above limit
            StepVerifier.create(riskService.isAllowed("PEN", "DEBIT", new BigDecimal("10000.01")))
                    .expectNext(false)
                    .verifyComplete();

            verify(riskRuleRepository, times(2)).findFirstByCurrency("PEN");
        }

        @Test
        @DisplayName("Should apply USD rules correctly")
        void shouldApplyUSDRulesCorrectly() {
            // Given
            when(riskRuleRepository.findFirstByCurrency("USD"))
                    .thenReturn(Optional.of(usdRule));

            // When & Then - Below limit
            StepVerifier.create(riskService.isAllowed("USD", "DEBIT", new BigDecimal("2999.99")))
                    .expectNext(true)
                    .verifyComplete();

            // Above limit
            StepVerifier.create(riskService.isAllowed("USD", "DEBIT", new BigDecimal("3000.01")))
                    .expectNext(false)
                    .verifyComplete();

            verify(riskRuleRepository, times(2)).findFirstByCurrency("USD");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle zero amount DEBIT")
        void shouldHandleZeroAmountDebit() {
            // Given
            when(riskRuleRepository.findFirstByCurrency("PEN"))
                    .thenReturn(Optional.of(penRule));

            // When & Then
            StepVerifier.create(riskService.isAllowed("PEN", "DEBIT", BigDecimal.ZERO))
                    .expectNext(true)
                    .verifyComplete();

            verify(riskRuleRepository, times(1)).findFirstByCurrency("PEN");
        }

        @Test
        @DisplayName("Should handle zero amount CREDIT")
        void shouldHandleZeroAmountCredit() {
            // Given
            when(riskRuleRepository.findFirstByCurrency("PEN"))
                    .thenReturn(Optional.of(penRule));

            // When & Then
            StepVerifier.create(riskService.isAllowed("PEN", "CREDIT", BigDecimal.ZERO))
                    .expectNext(true)
                    .verifyComplete();

            verify(riskRuleRepository, times(1)).findFirstByCurrency("PEN");
        }

        @Test
        @DisplayName("Should handle rule with zero limit")
        void shouldHandleRuleWithZeroLimit() {
            // Given
            RiskRule zeroLimitRule = RiskRule.builder()
                    .id("rule-zero")
                    .currency("EUR")
                    .maxDebitPerTx(BigDecimal.ZERO)
                    .build();

            when(riskRuleRepository.findFirstByCurrency("EUR"))
                    .thenReturn(Optional.of(zeroLimitRule));

            // When & Then
            StepVerifier.create(riskService.isAllowed("EUR", "DEBIT", new BigDecimal("100.00")))
                    .expectNext(false)
                    .verifyComplete();

            verify(riskRuleRepository, times(1)).findFirstByCurrency("EUR");
        }

        @Test
        @DisplayName("Should handle unknown transaction type")
        void shouldHandleUnknownTransactionType() {
            // Given
            when(riskRuleRepository.findFirstByCurrency("PEN"))
                    .thenReturn(Optional.of(penRule));

            // When & Then - Unknown type should be treated as non-DEBIT (allowed)
            StepVerifier.create(riskService.isAllowed("PEN", "TRANSFER", new BigDecimal("50000.00")))
                    .expectNext(true)
                    .verifyComplete();

            verify(riskRuleRepository, times(1)).findFirstByCurrency("PEN");
        }

        @Test
        @DisplayName("Should handle null max debit in rule")
        void shouldHandleNullMaxDebitInRule() {
            // Given
            RiskRule nullMaxRule = RiskRule.builder()
                    .id("rule-null")
                    .currency("EUR")
                    .maxDebitPerTx(null)
                    .build();

            when(riskRuleRepository.findFirstByCurrency("EUR"))
                    .thenReturn(Optional.of(nullMaxRule));

            // When & Then - Should use default 0
            StepVerifier.create(riskService.isAllowed("EUR", "DEBIT", new BigDecimal("100.00")))
                    .expectNext(false)
                    .verifyComplete();

            verify(riskRuleRepository, times(1)).findFirstByCurrency("EUR");
        }
    }

    @Nested
    @DisplayName("Repository Interaction Tests")
    class RepositoryInteractionTests {

        @Test
        @DisplayName("Should call repository once per request")
        void shouldCallRepositoryOncePerRequest() {
            // Given
            when(riskRuleRepository.findFirstByCurrency("PEN"))
                    .thenReturn(Optional.of(penRule));

            // When
            StepVerifier.create(riskService.isAllowed("PEN", "DEBIT", new BigDecimal("5000.00")))
                    .expectNext(true)
                    .verifyComplete();

            // Then
            verify(riskRuleRepository, times(1)).findFirstByCurrency("PEN");
            verifyNoMoreInteractions(riskRuleRepository);
        }

        @Test
        @DisplayName("Should handle repository returning empty Optional")
        void shouldHandleRepositoryReturningEmptyOptional() {
            // Given
            when(riskRuleRepository.findFirstByCurrency(anyString()))
                    .thenReturn(Optional.empty());

            // When & Then
            StepVerifier.create(riskService.isAllowed("XXX", "DEBIT", new BigDecimal("100.00")))
                    .expectNext(false)
                    .verifyComplete();

            verify(riskRuleRepository, times(1)).findFirstByCurrency("XXX");
        }
    }

    @Nested
    @DisplayName("Precision Tests")
    class PrecisionTests {

        @Test
        @DisplayName("Should handle precise decimal comparisons")
        void shouldHandlePreciseDecimalComparisons() {
            // Given
            when(riskRuleRepository.findFirstByCurrency("PEN"))
                    .thenReturn(Optional.of(penRule));

            // When & Then - Just below limit
            StepVerifier.create(riskService.isAllowed("PEN", "DEBIT", new BigDecimal("9999.99")))
                    .expectNext(true)
                    .verifyComplete();

            // Exactly at limit
            StepVerifier.create(riskService.isAllowed("PEN", "DEBIT", new BigDecimal("10000.00")))
                    .expectNext(true)
                    .verifyComplete();

            // Just above limit
            StepVerifier.create(riskService.isAllowed("PEN", "DEBIT", new BigDecimal("10000.01")))
                    .expectNext(false)
                    .verifyComplete();

            verify(riskRuleRepository, times(3)).findFirstByCurrency("PEN");
        }

        @Test
        @DisplayName("Should handle different decimal scales")
        void shouldHandleDifferentDecimalScales() {
            // Given
            when(riskRuleRepository.findFirstByCurrency("USD"))
                    .thenReturn(Optional.of(usdRule));

            // When & Then
            StepVerifier.create(riskService.isAllowed("USD", "DEBIT", new BigDecimal("3000")))
                    .expectNext(true)
                    .verifyComplete();

            StepVerifier.create(riskService.isAllowed("USD", "DEBIT", new BigDecimal("3000.0")))
                    .expectNext(true)
                    .verifyComplete();

            StepVerifier.create(riskService.isAllowed("USD", "DEBIT", new BigDecimal("3000.000")))
                    .expectNext(true)
                    .verifyComplete();

            verify(riskRuleRepository, times(3)).findFirstByCurrency("USD");
        }
    }
}

