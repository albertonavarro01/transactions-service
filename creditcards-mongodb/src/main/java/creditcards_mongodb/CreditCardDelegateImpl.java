package creditcards_mongodb;

import com.nttdata.creditcards.api.ApiApiDelegate;
import com.nttdata.creditcards.model.CreditCardRequest;
import com.nttdata.creditcards.model.CreditCardResponse;
import creditcards_mongodb.bussines.CreditCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class CreditCardDelegateImpl implements ApiApiDelegate {
    @Autowired
    CreditCardService creditCardService;

    @Override
    public ResponseEntity<CreditCardResponse> registerCreditCard(CreditCardRequest creditCardRequest) {
        return ResponseEntity.ok(creditCardService.registerCreditCard(creditCardRequest));

    }

    @Override
    public ResponseEntity<List<CreditCardResponse>> getAllCards() {
        return ResponseEntity.ok(creditCardService.getAllCards());
    }
}
