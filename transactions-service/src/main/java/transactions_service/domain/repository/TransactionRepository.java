package transactions_service.domain.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import transactions_service.domain.model.Transaction;

public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {

    Flux<Transaction> findByAccountIdOrderByTimestampDesc(String accountId);
 //   Mono<?> save(Transaction ok);
}
