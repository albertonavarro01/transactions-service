package transactions.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicación principal del servicio de transacciones.
 */
@SpringBootApplication
@SuppressWarnings("checkstyle:FinalClass")
public class TransactionsServiceApplication {

  /**
   * Punto de entrada de la aplicación.
   *
   * @param args argumentos de línea de comandos
   */
  public static void main(final String[] args) {
    SpringApplication.run(
        TransactionsServiceApplication.class, args);
  }
}
