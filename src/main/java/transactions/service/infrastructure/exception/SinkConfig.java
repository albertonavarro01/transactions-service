package transactions.service.infrastructure.exception;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;
import transactions.service.domain.model.Transaction;

/**
 * Configuración de beans reactivos usados por el servicio.
 */
@Configuration
public class SinkConfig {

  /**
   * Crea el sink para emitir transacciones en tiempo real.
   *
   * @return sink multicasting con buffer ante backpressure
   */
  @Bean
  public Sinks.Many<Transaction> txSink() {
    return Sinks.many()
        .multicast()
        .onBackpressureBuffer();
  }
}
