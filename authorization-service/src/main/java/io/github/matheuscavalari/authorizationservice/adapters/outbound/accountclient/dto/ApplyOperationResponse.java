package io.github.matheuscavalari.authorizationservice.adapters.outbound.accountclient.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ApplyOperationResponse(
        UUID transactionId,
        UUID accountId,
        String type,
        BigDecimal amountValue,
        String amountCurrency,
        String status,
        OffsetDateTime timestamp,
        BigDecimal resultingBalanceAmount,
        String resultingBalanceCurrency
) {}
