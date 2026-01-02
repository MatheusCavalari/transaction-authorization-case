package io.github.matheuscavalari.authorizationservice.adapters.inbound.web;

import io.github.matheuscavalari.authorizationservice.adapters.outbound.accountclient.AccountServiceClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(AccountServiceClient.AccountNotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "timestamp", OffsetDateTime.now().toString(),
                "error", "NOT_FOUND",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", OffsetDateTime.now().toString(),
                "error", "BAD_REQUEST",
                "message", ex.getMessage()
        ));
    }
}