package io.github.matheuscavalari.authorizationservice.adapters.outbound.accountclient.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ApplyOperationRequest(
        String type,          // CREDIT/DEBIT
        BigDecimal amountValue,
        String amountCurrency,
        OffsetDateTime timestamp
) {}
