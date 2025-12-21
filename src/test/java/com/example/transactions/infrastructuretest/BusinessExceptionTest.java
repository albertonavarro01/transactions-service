package com.example.transactions.infrastructuretest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import transactions.service.infrastructure.exception.BusinessException;

/**
 * Tests unitarios para {@link BusinessException}.
 */
public class BusinessExceptionTest {

  /**
   * Verifica que la excepción se cree con el mensaje correcto.
   */
  @Test
  public void shouldCreateExceptionWithMessage() {
    final String message = "account_not_found";
    final BusinessException ex = new BusinessException(message);

    assertEquals(message, ex.getMessage());
  }

  /**
   * Verifica que la excepción tenga la anotación ResponseStatus.
   */
  @Test
  public void shouldHaveResponseStatusAnnotation() {
    final ResponseStatus annotation =
        BusinessException.class.getAnnotation(ResponseStatus.class);

    assertNotNull(annotation);
    assertEquals(HttpStatus.BAD_REQUEST, annotation.value());
  }

  /**
   * Verifica que sea una RuntimeException.
   */
  @Test
  public void shouldBeRuntimeException() {
    final BusinessException ex = new BusinessException("test");

    assertTrue(ex instanceof RuntimeException);
  }

  /**
   * Verifica diferentes mensajes de error de negocio.
   */
  @Test
  public void shouldHandleDifferentBusinessMessages() {
    final String[] messages = {
        "account_not_found",
        "insufficient_funds",
        "risk_rejected",
        "invalid_transaction_type"
    };

    for (String msg : messages) {
      final BusinessException ex = new BusinessException(msg);
      assertEquals(msg, ex.getMessage());
    }
  }
}
