package creditcards_mongodb.bussines;

import com.nttdata.creditcards.model.CreditCardRequest;
import com.nttdata.creditcards.model.CreditCardResponse;
import creditcards_mongodb.repository.CreditCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreditCardServiceImpl implements CreditCardService {

    @Autowired
    CreditCardRepository creditCardRepository;

    @Autowired
    CreditCardMapper creditCardMapper;

    @Override
    public CreditCardResponse registerCreditCard(CreditCardRequest creditCardRequest) {
        return creditCardMapper.getCreditCardResponseOfCreditCard(creditCardRepository
                .save(creditCardMapper.getCreditCardOfCreditCardRequest(creditCardRequest)));
    }

    @Override
    public List<CreditCardResponse> getAllCards() {
        return creditCardRepository.findAll()
                .stream()
                .map(creditCardMapper::getCreditCardResponseOfCreditCard)
                .collect(Collectors.toList());
    }

    @Override
    public CreditCardResponse getCardById(CreditCardResponse creditCardResponse) {
        return null;
    }
}
