package io.github.matheuscavalari.authorizationservice.adapters.outbound.accountclient;

import io.github.matheuscavalari.authorizationservice.adapters.outbound.accountclient.dto.ApplyOperationRequest;
import io.github.matheuscavalari.authorizationservice.adapters.outbound.accountclient.dto.ApplyOperationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private AccountServiceClient client;

    @BeforeEach
    void setUp() {
        client = new AccountServiceClient(restClient);
    }

    @Test
    void applyOperation_whenSuccess_shouldReturnResponseAndBuildRequestProperly() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        ApplyOperationRequest req = mock(ApplyOperationRequest.class);
        ApplyOperationResponse expected = mock(ApplyOperationResponse.class);

        when(restClient.post()).thenReturn(requestBodyUriSpec);

        // uri(...) retorna um RequestBodySpec
        when(requestBodyUriSpec.uri(eq("/accounts/{accountId}/operations/{transactionId}"), eq(accountId), eq(txId)))
                .thenReturn(requestBodySpec);

        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(req)).thenReturn(requestBodySpec);

        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ApplyOperationResponse.class)).thenReturn(expected);

        // Act
        ApplyOperationResponse actual = client.applyOperation(accountId, txId, req);

        // Assert
        assertSame(expected, actual);

        verify(restClient).post();
        verify(requestBodyUriSpec).uri("/accounts/{accountId}/operations/{transactionId}", accountId, txId);
        verify(requestBodySpec).contentType(MediaType.APPLICATION_JSON);
        verify(requestBodySpec).body(req);
        verify(requestBodySpec).retrieve();
        verify(responseSpec).body(ApplyOperationResponse.class);

        verifyNoMoreInteractions(restClient, requestBodyUriSpec, requestBodySpec, responseSpec);
    }

    @Test
    void applyOperation_whenAccountNotFound_shouldThrowDomainExceptionWithMessage() {
        UUID accountId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        ApplyOperationRequest req = mock(ApplyOperationRequest.class);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(eq("/accounts/{accountId}/operations/{transactionId}"), eq(accountId), eq(txId)))
                .thenReturn(requestBodySpec);

        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(req)).thenReturn(requestBodySpec);

        HttpClientErrorException notFound = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                HttpHeaders.EMPTY,
                new byte[0],
                StandardCharsets.UTF_8
        );

        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ApplyOperationResponse.class)).thenThrow(notFound);

        AccountServiceClient.AccountNotFoundException ex =
                assertThrows(AccountServiceClient.AccountNotFoundException.class,
                        () -> client.applyOperation(accountId, txId, req));

        assertEquals("Account not found: " + accountId, ex.getMessage());
    }

}

