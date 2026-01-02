package io.github.matheuscavalari.accountservice.adapters.inbound.web.api;

import io.github.matheuscavalari.accountservice.adapters.inbound.web.dto.ApplyOperationRequest;
import io.github.matheuscavalari.accountservice.adapters.inbound.web.dto.ApplyOperationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(
        name = "Account Operations",
        description = "Endpoints internos para aplicar operações de crédito e débito em contas"
)
@RequestMapping("/accounts")
public interface AccountOperationsApi {

    @Operation(
            summary = "Apply an operation to an account (idempotent by transactionId)",
            description = """
                    Aplica uma operação de CRÉDITO ou DÉBITO em uma conta.
                    
                    Regras:
                    - Idempotente por transactionId
                    - Débito não permite saldo negativo (status=FAILED)
                    - Consistência garantida com lock pessimista (FOR UPDATE)
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Operation processed",
            content = @Content(
                    mediaType = APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApplyOperationResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid request or business validation error"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Account not found"
    )
    @PostMapping(
            value = "/{accountId}/operations/{transactionId}",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    ResponseEntity<ApplyOperationResponse> applyOperation(
            @PathVariable UUID accountId,
            @PathVariable UUID transactionId,
            @Valid @RequestBody ApplyOperationRequest request
    );
}
