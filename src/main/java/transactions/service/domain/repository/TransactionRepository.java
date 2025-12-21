package transactions.service.domain.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import transactions.service.domain.model.Transaction;

/**
 * Repositorio reactivo para transacciones.
 */
public interface TransactionRepository
    extends ReactiveMongoRepository<Transaction, String> {

  /**
   * Obtiene las transacciones de una cuenta ordenadas por fecha descendente.
   *
   * @param accountId id de la cuenta
   * @return flujo de transacciones
   */
  Flux<Transaction> findByAccountIdOrderByTimestampDesc(String accountId);
}
