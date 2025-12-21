package transactions.service.infrastructure.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

/**
 * Manejador global de excepciones para la API.
 */
@RestControllerAdvice
public final class GlobalExceptionHandler {

  /**
   * Crea un manejador global de excepciones.
   */
  public GlobalExceptionHandler() {
    System.out.println("🚀 GlobalExceptionHandler LOADED!");
  }

  /**
   * Maneja errores de validación provocados por {@code @Valid}.
   *
   * @param ex excepción de validación
   * @return respuesta HTTP con detalles de validación
   */
  @ExceptionHandler(WebExchangeBindException.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleValidationErrors(
      final WebExchangeBindException ex) {

    final Map<String, Object> response = new HashMap<>();
    response.put("timestamp", Instant.now().toString());
    response.put("status", HttpStatus.BAD_REQUEST.value());
    response.put("error", "Bad Request");
    response.put("message", "Validation failed");

    final Map<String, String> fieldErrors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .collect(Collectors.toMap(
            error -> error.getField(),
            error -> error.getDefaultMessage() != null
                ? error.getDefaultMessage()
                : "Invalid value",
            (existing, replacement) -> existing
        ));

    response.put("errors", fieldErrors);

    return Mono.just(ResponseEntity.badRequest().body(response));
  }

  /**
   * Maneja JSON malformado o errores de deserialización.
   *
   * @param ex excepción de entrada inválida
   * @return respuesta HTTP con el motivo del error
   */
  @ExceptionHandler(ServerWebInputException.class)
  public Mono<ResponseEntity<Map<String,
      Object>>> handleServerWebInputException(
      final ServerWebInputException ex) {

    final Map<String, Object> response = new HashMap<>();
    response.put("timestamp", Instant.now().toString());
    response.put("status", HttpStatus.BAD_REQUEST.value());
    response.put("error", "Bad Request");
    response.put("message", "Invalid input: " + ex.getReason());

    return Mono.just(ResponseEntity.badRequest().body(response));
  }

  /**
   * Maneja {@link IllegalArgumentException}.
   *
   * @param ex excepción por argumentos inválidos
   * @return respuesta HTTP con el mensaje de error
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleIllegalArgument(
      final IllegalArgumentException ex) {

    final Map<String, Object> response = new HashMap<>();
    response.put("timestamp", Instant.now().toString());
    response.put("status", HttpStatus.BAD_REQUEST.value());
    response.put("error", "Bad Request");
    response.put("message", ex.getMessage());

    return Mono.just(ResponseEntity.badRequest().body(response));
  }

  /**
   * Maneja cualquier otra excepción no capturada.
   *
   * @param ex excepción genérica
   * @return respuesta HTTP con error interno
   */
  @ExceptionHandler(Exception.class)
  public Mono<ResponseEntity<Map<String, Object>>> handleGenericException(
      final Exception ex) {

    final Map<String, Object> response = new HashMap<>();
    response.put("timestamp", Instant.now().toString());
    response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    response.put("error", "Internal Server Error");
    response.put(
        "message",
        "An unexpected error occurred: " + ex.getMessage());

    return Mono.just(
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(response)
    );
  }
}
