package io.github.matheuscavalari.authorizationservice.adapters.inbound.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuthorizeTransactionRequest(
        @NotNull UUID accountId,
        @NotNull String type, // "CREDIT" | "DEBIT"
        @Valid @NotNull Amount amount,
        @NotNull OffsetDateTime timestamp
) {
    public record Amount(
            @NotNull java.math.BigDecimal value,
            @NotNull String currency
    ) {}
}
