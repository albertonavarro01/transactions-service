package transactions.service.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

/**
 * Entidad que representa una cuenta bancaria.
 */
@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

  /**
   * Identificador único de la cuenta.
   */
  @Id
  private String id;

  /**
   * Número de la cuenta (formato interno).
   */
  private String number;

  /**
   * Nombre o titular de la cuenta.
   */
  private String holderName;

  /**
   * Estado de la cuenta (activo/inactivo).
   */
  private String status;

  /**
   * Tipo de cuenta (e.g. CHECKING, SAVINGS).
   */
  private String type;

  /**
   * Moneda de la cuenta ("PEN" o "USD").
   */
  private String currency;

  /**
   * Saldo disponible de la cuenta.
   */
  private BigDecimal balance;
}
