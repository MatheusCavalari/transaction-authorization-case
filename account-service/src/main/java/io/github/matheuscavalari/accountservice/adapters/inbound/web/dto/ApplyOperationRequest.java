package io.github.matheuscavalari.accountservice.adapters.inbound.web.dto;

import io.github.matheuscavalari.accountservice.domain.model.OperationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ApplyOperationRequest(

        @Schema(example = "DEBIT")
        @NotNull
        OperationType type,

        @Schema(example = "10.00")
        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amountValue,

        @Schema(example = "BRL")
        @NotBlank
        @Size(min = 3, max = 3)
        String amountCurrency,

        @Schema(example = "2025-12-30T12:00:00-03:00")
        @NotNull
        OffsetDateTime timestamp
) {}
