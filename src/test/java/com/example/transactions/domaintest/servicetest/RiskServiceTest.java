package com.example.transactions.domaintest.servicetest;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import transactions.service.domain.model.RiskRule;
import transactions.service.domain.repository.RiskRuleRepository;
import transactions.service.domain.service.RiskService;

/**
 * Tests unitarios para {@link RiskService}.
 */
@ExtendWith(MockitoExtension.class)
class RiskServiceTest {

  @Mock
  private RiskRuleRepository riskRuleRepository;

  @InjectMocks
  private RiskService riskService;

  private RiskRule riskRule;

  @BeforeEach
  void setUp() {
    riskRule = new RiskRule();
    riskRule.setMaxDebitPerTx(new BigDecimal("1000"));
  }

  @Test
  void shouldAllowDebitWhenAmountIsLessThanMax() {
    when(riskRuleRepository.findFirstByCurrency("USD"))
        .thenReturn(Mono.just(riskRule));

    StepVerifier.create(
            riskService.isAllowed("USD", "DEBIT", new BigDecimal("500"))
        )
        .expectNext(true)
        .verifyComplete();
  }

  @Test
  void shouldAllowDebitWhenAmountIsEqualToMax() {
    when(riskRuleRepository.findFirstByCurrency("USD"))
        .thenReturn(Mono.just(riskRule));

    StepVerifier.create(
            riskService.isAllowed("USD", "DEBIT", new BigDecimal("1000"))
        )
        .expectNext(true)
        .verifyComplete();
  }

  @Test
  void shouldRejectDebitWhenAmountIsGreaterThanMax() {
    when(riskRuleRepository.findFirstByCurrency("USD"))
        .thenReturn(Mono.just(riskRule));

    StepVerifier.create(
            riskService.isAllowed("USD", "DEBIT", new BigDecimal("1500"))
        )
        .expectNext(false)
        .verifyComplete();
  }

  @Test
  void shouldAllowNonDebitTransactionRegardlessOfAmount() {
    when(riskRuleRepository.findFirstByCurrency("USD"))
        .thenReturn(Mono.just(riskRule));

    StepVerifier.create(
            riskService.isAllowed("USD", "CREDIT", new BigDecimal("10000"))
        )
        .expectNext(true)
        .verifyComplete();
  }

  @Test
  void shouldAllowDebitWhenNoRiskRuleExists() {
    when(riskRuleRepository.findFirstByCurrency("EUR"))
        .thenReturn(Mono.empty());

    StepVerifier.create(
            riskService.isAllowed("EUR", "DEBIT", new BigDecimal("0"))
        )
        .expectNext(true)
        .verifyComplete();
  }

  @Test
  void shouldRejectDebitWhenNoRiskRuleExistsAndAmountGreaterThanZero() {
    when(riskRuleRepository.findFirstByCurrency("EUR"))
        .thenReturn(Mono.empty());

    StepVerifier.create(
            riskService.isAllowed("EUR", "DEBIT", new BigDecimal("10"))
        )
        .expectNext(false)
        .verifyComplete();
  }
}
