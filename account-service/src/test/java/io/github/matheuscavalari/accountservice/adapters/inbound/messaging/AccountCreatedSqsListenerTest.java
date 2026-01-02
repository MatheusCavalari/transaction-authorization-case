package io.github.matheuscavalari.accountservice.adapters.inbound.messaging;

import io.github.matheuscavalari.accountservice.adapters.inbound.messaging.dto.AccountCreatedMessage;
import io.github.matheuscavalari.accountservice.application.usecase.CreateAccountFromEventService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.*;

class AccountCreatedSqsListenerTest {

    @Test
    void shouldCallServiceWithPayloadFields() {
        var service = mock(CreateAccountFromEventService.class);
        var listener = new AccountCreatedSqsListener(service);

        UUID accountId = UUID.randomUUID();
        var msg = new AccountCreatedMessage(
                new AccountCreatedMessage.AccountPayload(
                        accountId,
                        "owner-123",
                        "1634874339",
                        "ENABLED"
                )
        );

        listener.onMessage(msg);

        verify(service).createIfNotExists(accountId, "owner-123", "1634874339", "ENABLED");
    }
}

