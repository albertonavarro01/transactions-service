package transactions_service.infrastructure.exception;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;
import transactions_service.domain.model.Transaction;

@Configuration
public class SinkConfig {
    @Bean
    public Sinks.Many<Transaction> txSink() {
        return Sinks.many().multicast().onBackpressureBuffer();
    }
}
