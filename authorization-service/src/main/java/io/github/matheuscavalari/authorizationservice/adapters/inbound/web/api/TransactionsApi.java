package io.github.matheuscavalari.authorizationservice.adapters.inbound.web.api;

import io.github.matheuscavalari.authorizationservice.adapters.inbound.web.dto.AuthorizeTransactionRequest;
import io.github.matheuscavalari.authorizationservice.adapters.inbound.web.dto.AuthorizeTransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "Transactions Authorization", description = "API pública para autorização de transações")
@RequestMapping("/transactions")
public interface TransactionsApi {

    @Operation(
            summary = "Authorize a transaction",
            description = "Autoriza uma transação de crédito/débito e retorna o resultado com saldo atualizado."
    )
    @ApiResponse(responseCode = "200", description = "Authorized (SUCCEEDED) or declined (FAILED)")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "404", description = "Account not found")
    @PostMapping(value = "/{transactionId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseEntity<AuthorizeTransactionResponse> authorize(
            @PathVariable UUID transactionId,
            @Valid @RequestBody AuthorizeTransactionRequest request
    );
}

