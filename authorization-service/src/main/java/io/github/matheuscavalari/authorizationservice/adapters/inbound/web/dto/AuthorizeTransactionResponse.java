package io.github.matheuscavalari.authorizationservice.adapters.inbound.web.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AuthorizeTransactionResponse(
        Transaction transaction,
        Account account
) {
    public record Transaction(
            UUID id,
            String type, // CREDIT/DEBIT
            Amount amount,
            String status, // SUCCEEDED/FAILED
            OffsetDateTime timestamp
    ) {}

    public record Account(
            UUID id,
            Balance balance
    ) {}

    public record Amount(
            BigDecimal value,
            String currency
    ) {}

    public record Balance(
            BigDecimal amount,
            String currency
    ) {}
}