package io.github.matheuscavalari.accountservice.adapters.inbound.messaging.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record AccountCreatedMessage(
        @JsonProperty("account") AccountPayload account
) {
    public record AccountPayload(
            @JsonProperty("id") UUID id,
            @JsonProperty("owner") String owner,
            @JsonProperty("created_at") String createdAtEpoch,
            @JsonProperty("status") String status
    ) {}
}
