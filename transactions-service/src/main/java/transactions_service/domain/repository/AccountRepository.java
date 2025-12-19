package transactions_service.domain.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;
import transactions_service.domain.model.Account;

public interface  AccountRepository extends ReactiveMongoRepository<Account, String> {
    Mono<Account> findByNumber(String number);
}
