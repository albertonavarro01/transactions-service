package transactions.service.domain.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;
import transactions.service.domain.model.Account;

/**
 * Repositorio para la entidad Account.
 */
public interface AccountRepository
    extends ReactiveMongoRepository<Account, String> {

  /**
   * Busca una cuenta por su número.
   *
   * @param number número de la cuenta
   * @return cuenta si existe; vacío en caso contrario
   */
  Mono<Account> findByNumber(String number);
}
