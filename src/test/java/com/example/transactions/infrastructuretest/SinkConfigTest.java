package com.example.transactions.infrastructuretest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Sinks;
import transactions.service.domain.model.Transaction;
import transactions.service.infrastructure.exception.SinkConfig;

/**
 * Tests unitarios para {@link SinkConfig}.
 */
public class SinkConfigTest {

  private final SinkConfig config = new SinkConfig();

  /**
   * Verifica que el bean txSink se cree correctamente.
   */
  @Test
  public void shouldCreateTxSinkBean() {
    final Sinks.Many<Transaction> sink = config.txSink();

    assertNotNull(sink);
  }

  /**
   * Verifica que el sink pueda emitir transacciones.
   */
  @Test
  public void shouldEmitTransactions() {
    final Sinks.Many<Transaction> sink = config.txSink();

    final Transaction tx = Transaction.builder()
        .id("tx-1")
        .accountId("acc-1")
        .type("DEBIT")
        .amount(new BigDecimal("100"))
        .timestamp(Instant.now())
        .status("OK")
        .build();

    final Sinks.EmitResult result = sink.tryEmitNext(tx);

    assertEquals(Sinks.EmitResult.OK, result);
  }

  /**
   * Verifica que múltiples suscriptores puedan recibir las transacciones.
   */
  @Test
  public void shouldSupportMultipleSubscribers() {
    final Sinks.Many<Transaction> sink = config.txSink();

    final Transaction tx = Transaction.builder()
        .id("tx-1")
        .accountId("acc-1")
        .type("CREDIT")
        .amount(new BigDecimal("200"))
        .timestamp(Instant.now())
        .status("OK")
        .build();

    // Dos suscriptores
    final var subscriber1 = sink.asFlux().subscribe();
    final var subscriber2 = sink.asFlux().subscribe();

    final Sinks.EmitResult result = sink.tryEmitNext(tx);

    assertEquals(Sinks.EmitResult.OK, result);
    assertNotNull(subscriber1);
    assertNotNull(subscriber2);
  }

  /**
   * Verifica que el sink maneje backpressure.
   */
  @Test
  public void shouldHandleBackpressure() {
    final Sinks.Many<Transaction> sink = config.txSink();

    // Emitir múltiples transacciones
    for (int i = 0; i < 10; i++) {
      final Transaction tx = Transaction.builder()
          .id("tx-" + i)
          .accountId("acc-1")
          .type("DEBIT")
          .amount(new BigDecimal(i * 10))
          .timestamp(Instant.now())
          .status("OK")
          .build();

      final Sinks.EmitResult result = sink.tryEmitNext(tx);
      assertEquals(Sinks.EmitResult.OK, result);
    }
  }

  /**
   * Verifica que el sink sea multicast.
   */
  @Test
  public void shouldBeMulticast() {
    final Sinks.Many<Transaction> sink = config.txSink();

    // Crear dos contadores para verificar que ambos suscriptores reciben la emisión
    final AtomicInteger count1 = new AtomicInteger(0);
    final AtomicInteger count2 = new AtomicInteger(0);

    // Suscribir dos listeners
    sink.asFlux().subscribe(tx -> count1.incrementAndGet());
    sink.asFlux().subscribe(tx -> count2.incrementAndGet());

    // Emitir una transacción
    final Transaction tx = Transaction.builder()
        .id("tx-1")
        .accountId("acc-1")
        .type("DEBIT")
        .amount(new BigDecimal("100"))
        .timestamp(Instant.now())
        .status("OK")
        .build();

    sink.tryEmitNext(tx);

    // Verificar que ambos suscriptores recibieron la transacción
    assertEquals(1, count1.get());
    assertEquals(1, count2.get());
  }
}
