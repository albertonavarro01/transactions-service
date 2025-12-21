package com.example.transactions.domaintest.dtotest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import transactions.service.domain.dto.CreateTxRequest;

@DisplayName("CreateTxRequest DTO Tests")
class CreateTxRequestTest {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  @DisplayName("Debe crear una solicitud válida con todos los campos correctos")
  void testValidCreateTxRequest() {
    CreateTxRequest request = CreateTxRequest.builder()
        .accountNumber("1234567890")
        .type("DEBIT")
        .amount(new BigDecimal("100.50"))
        .build();

    Set<ConstraintViolation<CreateTxRequest>> violations = validator.validate(request);

    assertTrue(violations.isEmpty(), "No debería haber violaciones de validación");
  }

  @Test
  @DisplayName("Debe fallar cuando accountNumber es null")
  void testAccountNumberNull() {
    CreateTxRequest request = CreateTxRequest.builder()
        .accountNumber(null)
        .type("CREDIT")
        .amount(new BigDecimal("50.00"))
        .build();

    Set<ConstraintViolation<CreateTxRequest>> violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertEquals(1, violations.size());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("accountNumber")));
  }

  @Test
  @DisplayName("Debe fallar cuando accountNumber está vacío")
  void testAccountNumberEmpty() {
    CreateTxRequest request = CreateTxRequest.builder()
        .accountNumber("")
        .type("DEBIT")
        .amount(new BigDecimal("75.25"))
        .build();

    Set<ConstraintViolation<CreateTxRequest>> violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("accountNumber")));
  }

  @Test
  @DisplayName("Debe fallar cuando accountNumber contiene solo espacios")
  void testAccountNumberBlank() {
    CreateTxRequest request = CreateTxRequest.builder()
        .accountNumber("   ")
        .type("CREDIT")
        .amount(new BigDecimal("200.00"))
        .build();

    Set<ConstraintViolation<CreateTxRequest>> violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("accountNumber")));
  }

  @Test
  @DisplayName("Debe fallar cuando type es null")
  void testTypeNull() {
    CreateTxRequest request = CreateTxRequest.builder()
        .accountNumber("9876543210")
        .type(null)
        .amount(new BigDecimal("150.00"))
        .build();

    Set<ConstraintViolation<CreateTxRequest>> violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("type")));
  }

  @Test
  @DisplayName("Debe fallar cuando type está vacío")
  void testTypeEmpty() {
    CreateTxRequest request = CreateTxRequest.builder()
        .accountNumber("1111222233")
        .type("")
        .amount(new BigDecimal("99.99"))
        .build();

    Set<ConstraintViolation<CreateTxRequest>> violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("type")));
  }

  @Test
  @DisplayName("Debe fallar cuando type contiene solo espacios")
  void testTypeBlank() {
    CreateTxRequest request = CreateTxRequest.builder()
        .accountNumber("4444555566")
        .type("  ")
        .amount(new BigDecimal("300.00"))
        .build();

    Set<ConstraintViolation<CreateTxRequest>> violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("type")));
  }

  @Test
  @DisplayName("Debe fallar cuando amount es menor que 0.01")
  void testAmountBelowMinimum() {
    CreateTxRequest request = CreateTxRequest.builder()
        .accountNumber("7777888899")
        .type("DEBIT")
        .amount(new BigDecimal("0.00"))
        .build();

    Set<ConstraintViolation<CreateTxRequest>> violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
  }

  @Test
  @DisplayName("Debe fallar cuando amount es negativo")
  void testAmountNegative() {
    CreateTxRequest request = CreateTxRequest.builder()
        .accountNumber("5555666677")
        .type("CREDIT")
        .amount(new BigDecimal("-10.00"))
        .build();

    Set<ConstraintViolation<CreateTxRequest>> violations = validator.validate(request);

    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
  }

  @Test
  @DisplayName("Debe aceptar amount igual a 0.01 (valor mínimo válido)")
  void testAmountMinimumValid() {
    CreateTxRequest request = CreateTxRequest.builder()
        .accountNumber("3333444455")
        .type("DEBIT")
        .amount(new BigDecimal("0.01"))
        .build();

    Set<ConstraintViolation<CreateTxRequest>> violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  @DisplayName("Debe aceptar montos grandes válidos")
  void testLargeAmountValid() {
    CreateTxRequest request = CreateTxRequest.builder()
        .accountNumber("6666777788")
        .type("CREDIT")
        .amount(new BigDecimal("999999.99"))
        .build();

    Set<ConstraintViolation<CreateTxRequest>> violations = validator.validate(request);

    assertTrue(violations.isEmpty());
  }

  @Test
  @DisplayName("Debe fallar con múltiples violaciones cuando varios campos son inválidos")
  void testMultipleViolations() {
    CreateTxRequest request = CreateTxRequest.builder()
        .accountNumber("")
        .type("")
        .amount(new BigDecimal("-5.00"))
        .build();

    Set<ConstraintViolation<CreateTxRequest>> violations = validator.validate(request);

    assertEquals(3, violations.size());
  }

  @Test
  @DisplayName("Debe crear instancia usando constructor sin argumentos")
  void testNoArgsConstructor() {
    CreateTxRequest request = new CreateTxRequest();

    assertNotNull(request);
    assertNull(request.getAccountNumber());
    assertNull(request.getType());
    assertNull(request.getAmount());
  }

  @Test
  @DisplayName("Debe crear instancia usando constructor con todos los argumentos")
  void testAllArgsConstructor() {
    BigDecimal amount = new BigDecimal("250.75");
    CreateTxRequest request = new CreateTxRequest("1234567890", "DEBIT", amount);

    assertEquals("1234567890", request.getAccountNumber());
    assertEquals("DEBIT", request.getType());
    assertEquals(amount, request.getAmount());
  }

  @Test
  @DisplayName("Debe crear instancia usando builder")
  void testBuilder() {
    BigDecimal amount = new BigDecimal("175.50");
    CreateTxRequest request = CreateTxRequest.builder()
        .accountNumber("9998887776")
        .type("CREDIT")
        .amount(amount)
        .build();

    assertEquals("9998887776", request.getAccountNumber());
    assertEquals("CREDIT", request.getType());
    assertEquals(amount, request.getAmount());
  }

  @Test
  @DisplayName("Debe usar correctamente getters y setters")
  void testGettersAndSetters() {
    CreateTxRequest request = new CreateTxRequest();
    BigDecimal amount = new BigDecimal("500.00");

    request.setAccountNumber("1112223334");
    request.setType("DEBIT");
    request.setAmount(amount);

    assertEquals("1112223334", request.getAccountNumber());
    assertEquals("DEBIT", request.getType());
    assertEquals(amount, request.getAmount());
  }

  @Test
  @DisplayName("Debe implementar equals y hashCode correctamente")
  void testEqualsAndHashCode() {
    BigDecimal amount = new BigDecimal("100.00");
    CreateTxRequest request1 = CreateTxRequest.builder()
        .accountNumber("1234567890")
        .type("DEBIT")
        .amount(amount)
        .build();

    CreateTxRequest request2 = CreateTxRequest.builder()
        .accountNumber("1234567890")
        .type("DEBIT")
        .amount(amount)
        .build();

    assertEquals(request1, request2);
    assertEquals(request1.hashCode(), request2.hashCode());
  }

  @Test
  @DisplayName("Debe implementar toString correctamente")
  void testToString() {
    CreateTxRequest request = CreateTxRequest.builder()
        .accountNumber("1234567890")
        .type("DEBIT")
        .amount(new BigDecimal("100.00"))
        .build();

    String toString = request.toString();

    assertNotNull(toString);
    assertTrue(toString.contains("1234567890"));
    assertTrue(toString.contains("DEBIT"));
    assertTrue(toString.contains("100"));
  }
}
