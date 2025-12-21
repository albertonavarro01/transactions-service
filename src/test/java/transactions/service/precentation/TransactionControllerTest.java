package transactions.service.precentation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import transactions.service.domain.dto.CreateTxRequest;
import transactions.service.domain.model.Transaction;
import transactions.service.domain.service.TransactionService;

/**
 * Tests de {@link TransactionController}.
 */
@WebFluxTest(controllers = TransactionController.class)
public class TransactionControllerTest {

  /**
   * Cliente HTTP de pruebas para WebFlux.
   */
  @Autowired
  private WebTestClient webTestClient;

  /**
   * Servicio mockeado.
   */
  @MockitoBean
  private TransactionService service;

  /**
   * Verifica que POST /api/transactions devuelve 201 y el body de la transacción.
   */
  @Test
  public void shouldCreateTransaction() {
    final CreateTxRequest req = CreateTxRequest.builder()
        .accountNumber("001-0001")
        .type("DEBIT")
        .amount(new BigDecimal("10.00"))
        .build();

    final Transaction tx = Transaction.builder()
        .id("tx-1")
        .accountId("acc-1")
        .type("DEBIT")
        .amount(new BigDecimal("10.00"))
        .timestamp(Instant.parse("2025-01-01T10:00:00Z"))
        .status("OK")
        .build();

    Mockito.when(service.create(Mockito.any(CreateTxRequest.class)))
        .thenReturn(Mono.just(tx));

    webTestClient.post()
        .uri("/api/transactions")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("""
            {
              "accountNumber": "001-0001",
              "type": "DEBIT",
              "amount": 10.00
            }
            """)
        .exchange()
        .expectStatus().isCreated()
        .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.id").isEqualTo("tx-1")
        .jsonPath("$.type").isEqualTo("DEBIT")
        .jsonPath("$.status").isEqualTo("OK");

    Mockito.verify(service)
        .create(Mockito.argThat(r ->
            "001-0001".equals(r.getAccountNumber())
                && "DEBIT".equals(r.getType())
                && new BigDecimal("10.00").compareTo(r.getAmount()) == 0));
  }

  /**
   * Verifica que GET /api/transactions?accountNumber=... devuelve lista.
   */
  @Test
  public void shouldListTransactionsByAccount() {
    final Transaction tx1 = Transaction.builder()
        .id("tx-1")
        .accountId("acc-1")
        .type("DEBIT")
        .amount(new BigDecimal("5"))
        .timestamp(Instant.parse("2025-01-01T10:00:00Z"))
        .status("OK")
        .build();

    final Transaction tx2 = Transaction.builder()
        .id("tx-2")
        .accountId("acc-1")
        .type("CREDIT")
        .amount(new BigDecimal("7"))
        .timestamp(Instant.parse("2025-01-01T11:00:00Z"))
        .status("OK")
        .build();

    Mockito.when(service.byAccount("001-0001"))
        .thenReturn(Flux.just(tx1, tx2));

    webTestClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/api/transactions")
            .queryParam("accountNumber", "001-0001")
            .build())
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$[0].id").isEqualTo("tx-1")
        .jsonPath("$[1].id").isEqualTo("tx-2");

    Mockito.verify(service).byAccount("001-0001");
  }

  /**
   * Verifica que GET /api/stream/transactions devuelve SSE (text/event-stream).
   */
  @Test
  public void shouldStreamTransactionsAsSse() {
    final Transaction tx = Transaction.builder()
        .id("tx-1")
        .accountId("acc-1")
        .type("DEBIT")
        .amount(new BigDecimal("1"))
        .timestamp(Instant.parse("2025-01-01T10:00:00Z"))
        .status("OK")
        .build();

    Mockito.when(service.stream())
        .thenReturn(Flux.just(
            org.springframework.http.codec.ServerSentEvent.builder(tx)
                .event("transaction")
                .build()
        ));

    webTestClient.get()
        .uri("/api/stream/transactions")
        .accept(MediaType.TEXT_EVENT_STREAM)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
        .expectBody(String.class)
        .consumeWith(resp -> {
          final String body = resp.getResponseBody();
          assertNotNull(body);
          assertTrue(body.contains("event:transaction"));
        });

    Mockito.verify(service).stream();
  }
}
