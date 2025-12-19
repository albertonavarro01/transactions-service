package creditcards_mongodb.repository;

import creditcards_mongodb.model.CreditCard;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CreditCardRepository extends MongoRepository<CreditCard,String> {
}
