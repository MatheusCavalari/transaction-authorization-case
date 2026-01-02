package io.github.matheuscavalari.accountservice.application.dto;

import io.github.matheuscavalari.accountservice.domain.model.OperationType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ApplyOperationCommand(
        UUID accountId,
        UUID transactionId,
        OperationType type,
        BigDecimal amountValue,
        String amountCurrency,
        OffsetDateTime timestamp
) {}
