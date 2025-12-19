package creditcards_mongodb.bussines;

import com.nttdata.creditcards.model.CreditCardRequest;
import com.nttdata.creditcards.model.CreditCardResponse;
import creditcards_mongodb.model.CreditCard;
import org.springframework.stereotype.Component;

@Component
public class CreditCardMapper {

    public CreditCard getCreditCardOfCreditCardRequest(CreditCardRequest request){
        CreditCard entity = new CreditCard();
        entity.setCardNumber(request.getCardNumber());
        entity.setCreditLimit(request.getCreditLimit());
        entity.setBalance(request.getBalance());
        entity.setId(request.getId());
        entity.setAccountHolder(request.getAccountHolder());
        return entity;

    }

    public CreditCardResponse getCreditCardResponseOfCreditCard(CreditCard entity){
        CreditCardResponse response = new CreditCardResponse();
        response.setCardNumber(entity.getCardNumber());
        response.setCreditLimit(entity.getCreditLimit());
        response.setBalance(entity.getBalance());
        response.setId(entity.getId());
        response.setAccountHolder(entity.getAccountHolder());
        return response;

    }
}
