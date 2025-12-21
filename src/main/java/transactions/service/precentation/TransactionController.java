package transactions.service.precentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import transactions.service.domain.dto.CreateTxRequest;
import transactions.service.domain.model.Transaction;
import transactions.service.domain.service.TransactionService;

/**
 * Controlador REST para operaciones de transacciones.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public final class TransactionController {

  /**
   * Servicio de transacciones.
   */
  private final TransactionService service;

  /**
   * Crea una transacción.
   *
   * @param req solicitud de creación
   * @return transacción creada
   */
  @PostMapping("/transactions")
  public Mono<ResponseEntity<Transaction>> create(
      final @Valid @RequestBody CreateTxRequest req) {

    return service.create(req)
        .map(t -> ResponseEntity.status(HttpStatus.CREATED).body(t));
  }

  /**
   * Lista las transacciones de una cuenta.
   *
   * @param accountNumber número de cuenta
   * @return flujo de transacciones
   */
  @GetMapping("/transactions")
  public Flux<Transaction> list(final @RequestParam String accountNumber) {
    return service.byAccount(accountNumber);
  }

  /**
   * Publica transacciones en tiempo real mediante SSE.
   *
   * @return flujo de eventos SSE
   */
  @GetMapping(
      value = "/stream/transactions",
      produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<Transaction>> stream() {
    return service.stream();
  }
}
