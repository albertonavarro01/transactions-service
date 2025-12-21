package transactions.service.infrastructure.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

/**
 * Manejador global de excepciones de la API.
 */
@RestControllerAdvice
public final class GlobalErrorHandler {

  /**
   * Maneja excepciones de negocio.
   *
   * @param ex excepción de negocio
   * @return respuesta HTTP con error controlado
   */
  @ExceptionHandler(BusinessException.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleBiz(
      final BusinessException ex) {

    return Mono.just(
        ResponseEntity.badRequest()
            .body(Map.of("error", ex.getMessage())));
  }

  /**
   * Maneja excepciones no controladas.
   *
   * @param ex excepción genérica
   * @return respuesta HTTP con error interno
   */
  @ExceptionHandler(Exception.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleGen(
      final Exception ex) {

    return Mono.just(
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of(
                "error", "internal_error",
                "message", ex.getMessage())));
  }
}
