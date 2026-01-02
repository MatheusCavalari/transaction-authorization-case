package io.github.matheuscavalari.accountservice.adapters.inbound.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.matheuscavalari.accountservice.application.dto.ApplyOperationResult;
import io.github.matheuscavalari.accountservice.application.usecase.ApplyOperationUseCase;
import io.github.matheuscavalari.accountservice.domain.model.OperationStatus;
import io.github.matheuscavalari.accountservice.domain.model.OperationType;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AccountOperationsController.class)
@Import(RestExceptionHandler.class)
class AccountOperationsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ApplyOperationUseCase useCase;

    @Test
    void shouldReturn200AndResponseBody_whenSucceeded() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        ApplyOperationResult result = new ApplyOperationResult(
                txId,
                OperationType.DEBIT,
                new BigDecimal("10.00"),
                "BRL",
                OperationStatus.SUCCEEDED,
                OffsetDateTime.parse("2025-12-30T12:00:00-03:00"),
                accountId,
                new BigDecimal("90.00"),
                "BRL"
        );

        when(useCase.execute(any())).thenReturn(result);

        String body = """
                {
                  "type": "DEBIT",
                  "amountValue": 10.00,
                  "amountCurrency": "BRL",
                  "timestamp": "2025-12-30T12:00:00-03:00"
                }
                """;

        mockMvc.perform(post("/accounts/{accountId}/operations/{transactionId}", accountId, txId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactionId").value(txId.toString()))
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.type").value("DEBIT"))
                .andExpect(jsonPath("$.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.resultingBalanceAmount").value(90.00))
                .andExpect(jsonPath("$.resultingBalanceCurrency").value("BRL"));
    }

    @Test
    void shouldReturn200EvenWhenFailedDebit_statusInBody() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        ApplyOperationResult result = new ApplyOperationResult(
                txId,
                OperationType.DEBIT,
                new BigDecimal("100.00"),
                "BRL",
                OperationStatus.FAILED,
                OffsetDateTime.parse("2025-12-30T12:00:00-03:00"),
                accountId,
                new BigDecimal("50.00"),
                "BRL"
        );

        when(useCase.execute(any())).thenReturn(result);

        String body = """
                {
                  "type": "DEBIT",
                  "amountValue": 100.00,
                  "amountCurrency": "BRL",
                  "timestamp": "2025-12-30T12:00:00-03:00"
                }
                """;

        mockMvc.perform(post("/accounts/{accountId}/operations/{transactionId}", accountId, txId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.resultingBalanceAmount").value(50.00));
    }

    @Test
    void shouldReturn400_whenRequestInvalid() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        // type null, amountValue 0, currency blank, timestamp null -> Bean Validation deve barrar
        String body = """
                {
                  "type": null,
                  "amountValue": 0,
                  "amountCurrency": "",
                  "timestamp": null
                }
                """;

        mockMvc.perform(post("/accounts/{accountId}/operations/{transactionId}", accountId, txId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404_whenAccountNotFound() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        when(useCase.execute(any())).thenThrow(new ApplyOperationUseCase.AccountNotFoundException(accountId));

        String body = """
                {
                  "type": "DEBIT",
                  "amountValue": 10.00,
                  "amountCurrency": "BRL",
                  "timestamp": "2025-12-30T12:00:00-03:00"
                }
                """;

        mockMvc.perform(post("/accounts/{accountId}/operations/{transactionId}", accountId, txId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Account not found: " + accountId));
    }

    @Test
    void shouldReturn400_whenIllegalArgumentFromUseCase() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        when(useCase.execute(any())).thenThrow(new IllegalArgumentException("Currency mismatch"));

        String body = """
                {
                  "type": "DEBIT",
                  "amountValue": 10.00,
                  "amountCurrency": "USD",
                  "timestamp": "2025-12-30T12:00:00-03:00"
                }
                """;

        mockMvc.perform(post("/accounts/{accountId}/operations/{transactionId}", accountId, txId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Currency mismatch"));
    }
}
