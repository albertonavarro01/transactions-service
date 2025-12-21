package transactions.service.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de solicitud para crear una transacción.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTxRequest {

  /**
   * Número de cuenta de origen de la transacción.
   */
  @NotBlank
  private String accountNumber;

  /**
   * Tipo de transacción (DEBIT / CREDIT).
   */
  @NotBlank
  private String type;

  /**
   * Monto de la transacción.
   */
  @NotNull
  @DecimalMin("0.01")
  private BigDecimal amount;
}
