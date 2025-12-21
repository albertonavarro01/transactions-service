package transactions.service.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entidad que representa una transacción bancaria.
 */
@Document("transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

  /**
   * Identificador único de la transacción.
   */
  @Id
  private String id;

  /**
   * Identificador de la cuenta asociada.
   */
  private String accountId;

  /**
   * Tipo de transacción (DEBIT/CREDIT).
   */
  private String type;

  /**
   * Importe de la transacción.
   */
  private BigDecimal amount;

  /**
   * Timestamp de la transacción.
   */
  private Instant timestamp;

  /**
   * Estado de la transacción.
   */
  private String status;

  /**
   * Campo adicional (por ejemplo: descripción).
   */
  private String reason;
}
