package com.example.transactions.infrastructuretest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import transactions.service.infrastructure.exception.BusinessException;
import transactions.service.infrastructure.exception.GlobalErrorHandler;

/**
 * Tests unitarios para {@link GlobalErrorHandler}.
 */
public class GlobalErrorHandlerTest {

  private GlobalErrorHandler handler;

  @BeforeEach
  public void setUp() {
    handler = new GlobalErrorHandler();
  }

  /**
   * Verifica manejo de BusinessException.
   */
  @Test
  public void shouldHandleBusinessException() {
    final BusinessException ex = new BusinessException("account_not_found");

    final Mono<ResponseEntity<Map<String, Object>>> result =
        handler.handleBiz(ex);

    StepVerifier.create(result)
        .assertNext(response -> {
          assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
          assertNotNull(response.getBody());
          assertEquals("account_not_found",
              response.getBody().get("error"));
        })
        .verifyComplete();
  }

  /**
   * Verifica manejo de BusinessException con diferentes mensajes.
   */
  @Test
  public void shouldHandleBusinessExceptionWithDifferentMessages() {
    final String[] messages = {
        "insufficient_funds",
        "risk_rejected",
        "invalid_account"
    };

    for (final String message : messages) {
      final BusinessException ex = new BusinessException(message);
      final Mono<ResponseEntity<Map<String, Object>>> result =
          handler.handleBiz(ex);

      StepVerifier.create(result)
          .assertNext(response -> {
            assertEquals(HttpStatus.BAD_REQUEST,
                response.getStatusCode());
            assertEquals(message, response.getBody().get("error"));
          })
          .verifyComplete();
    }
  }

  /**
   * Verifica manejo de excepciones genéricas.
   */
  @Test
  public void shouldHandleGenericException() {
    final Exception ex = new Exception("Unexpected database error");

    final Mono<ResponseEntity<Map<String, Object>>> result =
        handler.handleGen(ex);

    StepVerifier.create(result)
        .assertNext(response -> {
          assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
              response.getStatusCode());
          assertNotNull(response.getBody());
          assertEquals("internal_error",
              response.getBody().get("error"));
          assertEquals("Unexpected database error",
              response.getBody().get("message"));
        })
        .verifyComplete();
  }

  /**
   * Verifica manejo de NullPointerException.
   */
  @Test
  public void shouldHandleNullPointerException() {
    final NullPointerException ex =
        new NullPointerException("Null value encountered");

    final Mono<ResponseEntity<Map<String, Object>>> result =
        handler.handleGen(ex);

    StepVerifier.create(result)
        .assertNext(response -> {
          assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
              response.getStatusCode());
          assertTrue(response.getBody().containsKey("error"));
          assertTrue(response.getBody().containsKey("message"));
        })
        .verifyComplete();
  }

  /**
   * Verifica que la respuesta de error de negocio tenga la estructura correcta.
   */
  @Test
  public void shouldReturnCorrectStructureForBusinessError() {
    final BusinessException ex = new BusinessException("test_error");

    final Mono<ResponseEntity<Map<String, Object>>> result =
        handler.handleBiz(ex);

    StepVerifier.create(result)
        .assertNext(response -> {
          final Map<String, Object> body = response.getBody();
          assertNotNull(body);
          assertEquals(1, body.size());
          assertTrue(body.containsKey("error"));
        })
        .verifyComplete();
  }

  /**
   * Verifica que la respuesta de error genérico tenga la estructura correcta.
   */
  @Test
  public void shouldReturnCorrectStructureForGenericError() {
    final Exception ex = new Exception("test_error");

    final Mono<ResponseEntity<Map<String, Object>>> result =
        handler.handleGen(ex);

    StepVerifier.create(result)
        .assertNext(response -> {
          final Map<String, Object> body = response.getBody();
          assertNotNull(body);
          assertEquals(2, body.size());
          assertTrue(body.containsKey("error"));
          assertTrue(body.containsKey("message"));
        })
        .verifyComplete();
  }
}
