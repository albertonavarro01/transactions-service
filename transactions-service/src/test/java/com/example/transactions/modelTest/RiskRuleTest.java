package com.example.transactions.modelTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import transactions_service.domain.model.RiskRule;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RiskRule Model Tests")
public class RiskRuleTest {
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create risk rule with builder")
        void shouldCreateRiskRuleWithBuilder() {
            // Given & When
            RiskRule riskRule = RiskRule.builder()
                    .id("rule-001")
                    .currency("PEN")
                    .maxDebitPerTx(new BigDecimal("10000.00"))
                    .build();

            // Then
            assertNotNull(riskRule);
            assertEquals("rule-001", riskRule.getId());
            assertEquals("PEN", riskRule.getCurrency());
            assertEquals(new BigDecimal("10000.00"), riskRule.getMaxDebitPerTx());
        }

        @Test
        @DisplayName("Should create risk rule with no args constructor")
        void shouldCreateRiskRuleWithNoArgsConstructor() {
            // When
            RiskRule riskRule = new RiskRule();

            // Then
            assertNotNull(riskRule);
            assertNull(riskRule.getId());
            assertNull(riskRule.getCurrency());
            assertNull(riskRule.getMaxDebitPerTx());
        }

        @Test
        @DisplayName("Should create risk rule with all args constructor")
        void shouldCreateRiskRuleWithAllArgsConstructor() {
            // When
            RiskRule riskRule = new RiskRule(
                    "rule-002",
                    "USD",
                    new BigDecimal("5000.00")
            );

            // Then
            assertNotNull(riskRule);
            assertEquals("rule-002", riskRule.getId());
            assertEquals("USD", riskRule.getCurrency());
            assertEquals(new BigDecimal("5000.00"), riskRule.getMaxDebitPerTx());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should use setters correctly")
        void shouldUseSettersCorrectly() {
            // Given
            RiskRule riskRule = new RiskRule();

            // When
            riskRule.setId("rule-003");
            riskRule.setCurrency("PEN");
            riskRule.setMaxDebitPerTx(new BigDecimal("15000.00"));

            // Then
            assertEquals("rule-003", riskRule.getId());
            assertEquals("PEN", riskRule.getCurrency());
            assertEquals(new BigDecimal("15000.00"), riskRule.getMaxDebitPerTx());
        }

        @Test
        @DisplayName("Should get all properties correctly")
        void shouldGetAllPropertiesCorrectly() {
            // Given
            RiskRule riskRule = RiskRule.builder()
                    .id("rule-004")
                    .currency("USD")
                    .maxDebitPerTx(new BigDecimal("8000.00"))
                    .build();

            // When & Then
            assertEquals("rule-004", riskRule.getId());
            assertEquals("USD", riskRule.getCurrency());
            assertEquals(new BigDecimal("8000.00"), riskRule.getMaxDebitPerTx());
        }
    }

    @Nested
    @DisplayName("Currency Tests")
    class CurrencyTests {

        @Test
        @DisplayName("Should handle PEN currency")
        void shouldHandlePENCurrency() {
            // Given & When
            RiskRule riskRule = RiskRule.builder()
                    .id("rule-pen-001")
                    .currency("PEN")
                    .maxDebitPerTx(new BigDecimal("8000.00"))
                    .build();

            // Then
            assertEquals("PEN", riskRule.getCurrency());
        }

        @Test
        @DisplayName("Should handle USD currency")
        void shouldHandleUSDCurrency() {
            // Given & When
            RiskRule riskRule = RiskRule.builder()
                    .id("rule-usd-001")
                    .currency("USD")
                    .maxDebitPerTx(new BigDecimal("3000.00"))
                    .build();

            // Then
            assertEquals("USD", riskRule.getCurrency());
        }

        @Test
        @DisplayName("Should update currency after creation")
        void shouldUpdateCurrencyAfterCreation() {
            // Given
            RiskRule riskRule = RiskRule.builder()
                    .id("rule-005")
                    .currency("PEN")
                    .maxDebitPerTx(new BigDecimal("10000.00"))
                    .build();

            // When
            riskRule.setCurrency("USD");

            // Then
            assertEquals("USD", riskRule.getCurrency());
        }
    }

    @Nested
    @DisplayName("Max Debit Amount Tests")
    class MaxDebitAmountTests {

        @Test
        @DisplayName("Should handle large max debit amounts")
        void shouldHandleLargeMaxDebitAmounts() {
            // Given & When
            RiskRule riskRule = RiskRule.builder()
                    .id("rule-large")
                    .currency("USD")
                    .maxDebitPerTx(new BigDecimal("999999.99"))
                    .build();

            // Then
            assertEquals(0, new BigDecimal("999999.99")
                    .compareTo(riskRule.getMaxDebitPerTx()));
        }

        @Test
        @DisplayName("Should handle small max debit amounts")
        void shouldHandleSmallMaxDebitAmounts() {
            // Given & When
            RiskRule riskRule = RiskRule.builder()
                    .id("rule-small")
                    .currency("PEN")
                    .maxDebitPerTx(new BigDecimal("100.00"))
                    .build();

            // Then
            assertEquals(0, new BigDecimal("100.00")
                    .compareTo(riskRule.getMaxDebitPerTx()));
        }

        @Test
        @DisplayName("Should handle zero max debit amount")
        void shouldHandleZeroMaxDebitAmount() {
            // Given & When
            RiskRule riskRule = RiskRule.builder()
                    .id("rule-zero")
                    .currency("PEN")
                    .maxDebitPerTx(BigDecimal.ZERO)
                    .build();

            // Then
            assertEquals(0, BigDecimal.ZERO.compareTo(riskRule.getMaxDebitPerTx()));
        }

        @Test
        @DisplayName("Should update max debit per transaction after creation")
        void shouldUpdateMaxDebitPerTxAfterCreation() {
            // Given
            RiskRule riskRule = RiskRule.builder()
                    .id("rule-update")
                    .currency("PEN")
                    .maxDebitPerTx(new BigDecimal("5000.00"))
                    .build();

            // When
            riskRule.setMaxDebitPerTx(new BigDecimal("12000.00"));

            // Then
            assertEquals(0, new BigDecimal("12000.00")
                    .compareTo(riskRule.getMaxDebitPerTx()));
        }

        @Test
        @DisplayName("Should handle decimal precision")
        void shouldHandleDecimalPrecision() {
            // Given & When
            RiskRule riskRule = RiskRule.builder()
                    .id("rule-decimal")
                    .currency("USD")
                    .maxDebitPerTx(new BigDecimal("5000.50"))
                    .build();

            // Then
            assertEquals(0, new BigDecimal("5000.50")
                    .compareTo(riskRule.getMaxDebitPerTx()));
        }
    }

    @Nested
    @DisplayName("Lombok Generated Methods Tests")
    class LombokMethodsTests {

        @Test
        @DisplayName("Should test equals with same values")
        void shouldTestEqualsWithSameValues() {
            // Given
            RiskRule rule1 = RiskRule.builder()
                    .id("rule-eq-001")
                    .currency("PEN")
                    .maxDebitPerTx(new BigDecimal("5000.00"))
                    .build();

            RiskRule rule2 = RiskRule.builder()
                    .id("rule-eq-001")
                    .currency("PEN")
                    .maxDebitPerTx(new BigDecimal("5000.00"))
                    .build();

            // Then
            assertEquals(rule1, rule2);
        }

        @Test
        @DisplayName("Should test equals with different values")
        void shouldTestEqualsWithDifferentValues() {
            // Given
            RiskRule rule1 = RiskRule.builder()
                    .id("rule-diff-001")
                    .currency("PEN")
                    .maxDebitPerTx(new BigDecimal("5000.00"))
                    .build();

            RiskRule rule2 = RiskRule.builder()
                    .id("rule-diff-002")
                    .currency("USD")
                    .maxDebitPerTx(new BigDecimal("3000.00"))
                    .build();

            // Then
            assertNotEquals(rule1, rule2);
        }

        @Test
        @DisplayName("Should test hashCode consistency")
        void shouldTestHashCodeConsistency() {
            // Given
            RiskRule rule1 = RiskRule.builder()
                    .id("rule-hash-001")
                    .currency("PEN")
                    .maxDebitPerTx(new BigDecimal("5000.00"))
                    .build();

            RiskRule rule2 = RiskRule.builder()
                    .id("rule-hash-001")
                    .currency("PEN")
                    .maxDebitPerTx(new BigDecimal("5000.00"))
                    .build();

            // Then
            assertEquals(rule1.hashCode(), rule2.hashCode());
        }

        @Test
        @DisplayName("Should test toString method")
        void shouldTestToStringMethod() {
            // Given
            RiskRule riskRule = RiskRule.builder()
                    .id("rule-str-001")
                    .currency("USD")
                    .maxDebitPerTx(new BigDecimal("7500.00"))
                    .build();

            // When
            String result = riskRule.toString();

            // Then
            assertNotNull(result);
            assertTrue(result.contains("rule-str-001"));
            assertTrue(result.contains("USD"));
            assertTrue(result.contains("7500"));
        }

        @Test
        @DisplayName("Should test equals with null")
        void shouldTestEqualsWithNull() {
            // Given
            RiskRule riskRule = RiskRule.builder()
                    .id("rule-null-test")
                    .currency("PEN")
                    .maxDebitPerTx(new BigDecimal("5000.00"))
                    .build();

            // Then
            assertNotEquals(null, riskRule);
        }

        @Test
        @DisplayName("Should test equals with same reference")
        void shouldTestEqualsWithSameReference() {
            // Given
            RiskRule riskRule = RiskRule.builder()
                    .id("rule-ref-test")
                    .currency("PEN")
                    .maxDebitPerTx(new BigDecimal("5000.00"))
                    .build();

            // Then
            assertEquals(riskRule, riskRule);
        }
    }

    @Nested
    @DisplayName("Null Value Tests")
    class NullValueTests {

        @Test
        @DisplayName("Should allow null values in builder")
        void shouldAllowNullValuesInBuilder() {
            // Given & When
            RiskRule riskRule = RiskRule.builder()
                    .id(null)
                    .currency(null)
                    .maxDebitPerTx(null)
                    .build();

            // Then
            assertNull(riskRule.getId());
            assertNull(riskRule.getCurrency());
            assertNull(riskRule.getMaxDebitPerTx());
        }

        @Test
        @DisplayName("Should allow setting null values with setters")
        void shouldAllowSettingNullValuesWithSetters() {
            // Given
            RiskRule riskRule = RiskRule.builder()
                    .id("rule-null")
                    .currency("PEN")
                    .maxDebitPerTx(new BigDecimal("5000.00"))
                    .build();

            // When
            riskRule.setId(null);
            riskRule.setCurrency(null);
            riskRule.setMaxDebitPerTx(null);

            // Then
            assertNull(riskRule.getId());
            assertNull(riskRule.getCurrency());
            assertNull(riskRule.getMaxDebitPerTx());
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should create rule for PEN with typical limit")
        void shouldCreateRuleForPENWithTypicalLimit() {
            // Given & When
            RiskRule riskRule = RiskRule.builder()
                    .id("rule-pen-typical")
                    .currency("PEN")
                    .maxDebitPerTx(new BigDecimal("10000.00"))
                    .build();

            // Then
            assertAll(
                    () -> assertEquals("PEN", riskRule.getCurrency()),
                    () -> assertTrue(riskRule.getMaxDebitPerTx()
                            .compareTo(BigDecimal.ZERO) > 0)
            );
        }

        @Test
        @DisplayName("Should create rule for USD with typical limit")
        void shouldCreateRuleForUSDWithTypicalLimit() {
            // Given & When
            RiskRule riskRule = RiskRule.builder()
                    .id("rule-usd-typical")
                    .currency("USD")
                    .maxDebitPerTx(new BigDecimal("3000.00"))
                    .build();

            // Then
            assertAll(
                    () -> assertEquals("USD", riskRule.getCurrency()),
                    () -> assertTrue(riskRule.getMaxDebitPerTx()
                            .compareTo(BigDecimal.ZERO) > 0)
            );
        }

        @Test
        @DisplayName("Should verify max debit is positive for valid rules")
        void shouldVerifyMaxDebitIsPositiveForValidRules() {
            // Given & When
            RiskRule riskRule = RiskRule.builder()
                    .id("rule-positive")
                    .currency("PEN")
                    .maxDebitPerTx(new BigDecimal("7500.00"))
                    .build();

            // Then
            assertTrue(riskRule.getMaxDebitPerTx().compareTo(BigDecimal.ZERO) > 0,
                    "Max debit should be positive for valid rules");
        }
    }
}
