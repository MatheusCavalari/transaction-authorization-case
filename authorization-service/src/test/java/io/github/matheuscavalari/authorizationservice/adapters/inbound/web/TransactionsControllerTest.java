package io.github.matheuscavalari.authorizationservice.adapters.inbound.web;

import io.github.matheuscavalari.authorizationservice.adapters.outbound.accountclient.AccountServiceClient;
import io.github.matheuscavalari.authorizationservice.adapters.outbound.accountclient.dto.ApplyOperationRequest;
import io.github.matheuscavalari.authorizationservice.adapters.outbound.accountclient.dto.ApplyOperationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TransactionsController.class)
@Import(RestExceptionHandler.class)
class TransactionsControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean
    AccountServiceClient accountServiceClient;

    @Test
    void shouldReturn200_whenSucceeded() throws Exception {
        UUID txId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        when(accountServiceClient.applyOperation(eq(accountId), eq(txId), any(ApplyOperationRequest.class)))
                .thenReturn(new ApplyOperationResponse(
                        txId, accountId, "CREDIT",
                        new BigDecimal("97.07"), "BRL", "SUCCEEDED",
                        OffsetDateTime.parse("2025-07-08T15:57:55-03:00"),
                        new BigDecimal("183.12"), "BRL"
                ));

        String body = """
        {
          "accountId": "%s",
          "type": "CREDIT",
          "amount": { "value": 97.07, "currency": "BRL" },
          "timestamp": "2025-07-08T15:57:55-03:00"
        }
        """.formatted(accountId);

        mockMvc.perform(post("/transactions/{transactionId}", txId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transaction.id").value(txId.toString()))
                .andExpect(jsonPath("$.transaction.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.account.id").value(accountId.toString()))
                .andExpect(jsonPath("$.account.balance.amount").value(183.12));
    }

    @Test
    void shouldReturn404_whenAccountNotFound() throws Exception {
        UUID txId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        when(accountServiceClient.applyOperation(eq(accountId), eq(txId), any()))
                .thenThrow(new AccountServiceClient.AccountNotFoundException(accountId));

        String body = """
        {
          "accountId": "%s",
          "type": "DEBIT",
          "amount": { "value": 10.00, "currency": "BRL" },
          "timestamp": "2025-07-08T15:57:55-03:00"
        }
        """.formatted(accountId);

        mockMvc.perform(post("/transactions/{transactionId}", txId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account not found: " + accountId));
    }

    @Test
    void shouldReturn400_whenInvalidRequest() throws Exception {
        UUID txId = UUID.randomUUID();

        String body = """
        { "accountId": null, "type": null, "amount": null, "timestamp": null }
        """;

        mockMvc.perform(post("/transactions/{transactionId}", txId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400_whenIllegalArgumentFromClient() throws Exception {
        UUID txId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        when(accountServiceClient.applyOperation(eq(accountId), eq(txId), any()))
                .thenThrow(new IllegalArgumentException("Currency mismatch"));

        String body = """
        {
          "accountId": "%s",
          "type": "DEBIT",
          "amount": { "value": 10.00, "currency": "USD" },
          "timestamp": "2025-07-08T15:57:55-03:00"
        }
        """.formatted(accountId);

        mockMvc.perform(post("/transactions/{transactionId}", txId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Currency mismatch"));
    }
}

