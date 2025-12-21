package transactions.service.domain.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Regla de riesgo para validación de transacciones.
 */
@Document(collection = "risk_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskRule {

  /**
   * Identificador de la regla de riesgo.
   */
  @Id
  private String id;

  /**
   * Moneda aplicada por la regla (ej. "PEN", "USD").
   */
  private String currency;

  /**
   * Importe máximo permitido por transacción para débitos bajo esta regla.
   */
  private BigDecimal maxDebitPerTx;
}
