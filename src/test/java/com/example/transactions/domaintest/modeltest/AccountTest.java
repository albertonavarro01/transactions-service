package com.example.transactions.domaintest.modeltest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.Id;
import transactions.service.domain.model.Account;

/**
 * Tests unitarios para {@link Account}.
 */
public class AccountTest {

  /**
   * Verifica que {@link Account} tenga las anotaciones JPA esperadas.
   */
  @Test
  public void shouldHaveJpaAnnotations() {
    assertNotNull(Account.class.getAnnotation(Entity.class));

    final Table table = Account.class.getAnnotation(Table.class);
    assertNotNull(table);
    assertEquals("accounts", table.name());
  }

  /**
   * Verifica que el campo id esté anotado con {@link Id}.
   *
   * @throws NoSuchFieldException si el campo no existe
   */
  @Test
  public void shouldHaveIdFieldAnnotated() throws NoSuchFieldException {
    final Field idField = Account.class.getDeclaredField("id");
    assertNotNull(idField.getAnnotation(Id.class));
  }

  /**
   * Verifica que el builder asigne correctamente los campos.
   */
  @Test
  public void shouldBuildAccount() {
    final Account acc = Account.builder()
        .id("acc-1")
        .number("001-0001")
        .holderName("Ana Peru")
        .status("ACTIVE")
        .type("CHECKING")
        .currency("PEN")
        .balance(new BigDecimal("2000.00"))
        .build();

    assertEquals("acc-1", acc.getId());
    assertEquals("001-0001", acc.getNumber());
    assertEquals("Ana Peru", acc.getHolderName());
    assertEquals("ACTIVE", acc.getStatus());
    assertEquals("CHECKING", acc.getType());
    assertEquals("PEN", acc.getCurrency());
    assertEquals(new BigDecimal("2000.00"), acc.getBalance());
  }

  /**
   * Verifica que setters/getters funcionen (Lombok {@code @Data}).
   */
  @Test
  public void shouldAllowMutations() {
    final Account acc = new Account();
    acc.setId("acc-2");
    acc.setBalance(new BigDecimal("10"));

    assertEquals("acc-2", acc.getId());
    assertEquals(new BigDecimal("10"), acc.getBalance());
  }
}
