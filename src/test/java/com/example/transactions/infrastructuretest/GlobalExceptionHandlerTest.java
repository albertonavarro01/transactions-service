package com.example.transactions.infrastructuretest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import transactions.service.infrastructure.exception.GlobalExceptionHandler;

/**
 * Tests unitarios para {@link GlobalExceptionHandler}.
 */
public class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new GlobalExceptionHandler();
  }

  /**
   * Verifica manejo de errores de validación.
   */
  @Test
  public void shouldHandleValidationErrors() {
    final BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(new Object(), "testObject");
    bindingResult.addError(
        new FieldError("testObject", "field1", "must not be null"));
    bindingResult.addError(
        new FieldError("testObject", "field2", "must be positive"));

    final WebExchangeBindException ex =
        new WebExchangeBindException(null, bindingResult);

    final Mono<ResponseEntity<Map<String, Object>>> result =
        handler.handleValidationErrors(ex);

    StepVerifier.create(result)
        .assertNext(response -> {
          assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
          assertNotNull(response.getBody());

          final Map<String, Object> body = response.getBody();
          assertEquals(400, body.get("status"));
          assertEquals("Bad Request", body.get("error"));
          assertEquals("Validation failed", body.get("message"));

          @SuppressWarnings("unchecked") final Map<String, String> errors =
              (Map<String, String>) body.get("errors");
          assertNotNull(errors);
          assertEquals("must not be null", errors.get("field1"));
          assertEquals("must be positive", errors.get("field2"));
        })
        .verifyComplete();
  }

  /**
   * Verifica manejo de errores de entrada inválida del servidor.
   */
  @Test
  public void shouldHandleServerWebInputException() {
    final ServerWebInputException ex =
        new ServerWebInputException("Invalid JSON format");

    final Mono<ResponseEntity<Map<String, Object>>> result =
        handler.handleServerWebInputException(ex);

    StepVerifier.create(result)
        .assertNext(response -> {
          assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
          assertNotNull(response.getBody());

          final Map<String, Object> body = response.getBody();
          assertEquals(400, body.get("status"));
          assertEquals("Bad Request", body.get("error"));
          assertTrue(body.get("message").toString()
              .contains("Invalid input"));
        })
        .verifyComplete();
  }

  /**
   * Verifica manejo de IllegalArgumentException.
   */
  @Test
  public void shouldHandleIllegalArgumentException() {
    final IllegalArgumentException ex =
        new IllegalArgumentException("Invalid argument provided");

    final Mono<ResponseEntity<Map<String, Object>>> result =
        handler.handleIllegalArgument(ex);

    StepVerifier.create(result)
        .assertNext(response -> {
          assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
          assertNotNull(response.getBody());

          final Map<String, Object> body = response.getBody();
          assertEquals(400, body.get("status"));
          assertEquals("Bad Request", body.get("error"));
          assertEquals("Invalid argument provided", body.get("message"));
        })
        .verifyComplete();
  }

  /**
   * Verifica manejo de excepciones genéricas.
   */
  @Test
  public void shouldHandleGenericException() {
    final Exception ex = new Exception("Unexpected error occurred");

    final Mono<ResponseEntity<Map<String, Object>>> result =
        handler.handleGenericException(ex);

    StepVerifier.create(result)
        .assertNext(response -> {
          assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
              response.getStatusCode());
          assertNotNull(response.getBody());

          final Map<String, Object> body = response.getBody();
          assertEquals(500, body.get("status"));
          assertEquals("Internal Server Error", body.get("error"));
          assertTrue(body.get("message").toString()
              .contains("An unexpected error occurred"));
        })
        .verifyComplete();
  }

  /**
   * Verifica que todas las respuestas incluyan timestamp.
   */
  @Test
  public void shouldIncludeTimestampInAllResponses() {
    final Exception ex = new Exception("Test error");

    final Mono<ResponseEntity<Map<String, Object>>> result =
        handler.handleGenericException(ex);

    StepVerifier.create(result)
        .assertNext(response -> {
          assertNotNull(response.getBody());
          assertNotNull(response.getBody().get("timestamp"));
        })
        .verifyComplete();
  }
}
