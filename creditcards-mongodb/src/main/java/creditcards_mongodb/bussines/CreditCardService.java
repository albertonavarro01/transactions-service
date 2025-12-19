package creditcards_mongodb.bussines;

import com.nttdata.creditcards.model.CreditCardRequest;
import com.nttdata.creditcards.model.CreditCardResponse;

import java.util.List;

public interface CreditCardService {

   public CreditCardResponse registerCreditCard(CreditCardRequest creditCardRequest);
   List<CreditCardResponse> getAllCards();
   public CreditCardResponse getCardById(CreditCardResponse creditCardResponse);


}
