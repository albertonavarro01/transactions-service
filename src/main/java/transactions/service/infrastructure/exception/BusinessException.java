package transactions.service.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción de negocio para errores controlados de la aplicación.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessException extends RuntimeException {

  /**
   * Crea una nueva excepción de negocio con un mensaje descriptivo.
   *
   * @param message mensaje de error
   */
  public BusinessException(final String message) {
    super(message);
  }
}
