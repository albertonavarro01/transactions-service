package transactions_service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "risk_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskRule {

    @Id
    private String id;   // Mongo usa String/ObjectId, NO Long

    private String currency;

    private BigDecimal maxDebitPerTx;
}
