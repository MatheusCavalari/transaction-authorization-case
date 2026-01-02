package io.github.matheuscavalari.accountservice.adapters.inbound.web.dto;

import io.github.matheuscavalari.accountservice.domain.model.OperationStatus;
import io.github.matheuscavalari.accountservice.domain.model.OperationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ApplyOperationResponse(

        @Schema(example = "9b3f4f76-7b2d-4b31-9d9a-0b1c9e4b8b12")
        UUID transactionId,

        @Schema(example = "b8c49f2b-86b0-4c76-8fb2-9f6e9a5f2c10")
        UUID accountId,

        OperationType type,

        BigDecimal amountValue,

        String amountCurrency,

        OperationStatus status,

        OffsetDateTime timestamp,

        BigDecimal resultingBalanceAmount,

        String resultingBalanceCurrency
) {}
