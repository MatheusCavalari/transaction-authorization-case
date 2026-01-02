package io.github.matheuscavalari.accountservice.application.dto;

import io.github.matheuscavalari.accountservice.domain.model.OperationStatus;
import io.github.matheuscavalari.accountservice.domain.model.OperationType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ApplyOperationResult(
        UUID transactionId,
        OperationType type,
        BigDecimal amountValue,
        String amountCurrency,
        OperationStatus status,
        OffsetDateTime timestamp,
        UUID accountId,
        BigDecimal resultingBalanceAmount,
        String resultingBalanceCurrency
) {}
