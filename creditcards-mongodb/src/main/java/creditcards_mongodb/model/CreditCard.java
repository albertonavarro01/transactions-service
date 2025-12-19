package creditcards_mongodb.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;

@Data
@Document(collection = "credit_card")
public class CreditCard {

    @Id
    private  String id;
    private String cardNumber;
    private String accountHolder;
    private BigDecimal balance;
    private BigDecimal creditLimit;

}
