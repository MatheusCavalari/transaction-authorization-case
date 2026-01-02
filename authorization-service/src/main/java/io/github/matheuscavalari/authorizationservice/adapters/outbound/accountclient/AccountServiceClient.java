package io.github.matheuscavalari.authorizationservice.adapters.outbound.accountclient;

import io.github.matheuscavalari.authorizationservice.adapters.outbound.accountclient.dto.ApplyOperationRequest;
import io.github.matheuscavalari.authorizationservice.adapters.outbound.accountclient.dto.ApplyOperationResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class AccountServiceClient {

    private final RestClient restClient;

    public AccountServiceClient(RestClient accountServiceRestClient) {
        this.restClient = accountServiceRestClient;
    }

    public ApplyOperationResponse applyOperation(UUID accountId, UUID transactionId, ApplyOperationRequest req) {
        try {
            return restClient.post()
                    .uri("/accounts/{accountId}/operations/{transactionId}", accountId, transactionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(req)
                    .retrieve()
                    .body(ApplyOperationResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new AccountNotFoundException(accountId);
        }
    }

    public static class AccountNotFoundException extends RuntimeException {
        public AccountNotFoundException(UUID accountId) {
            super("Account not found: " + accountId);
        }
    }
}