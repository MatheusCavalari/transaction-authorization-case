package io.github.matheuscavalari.accountservice.adapters.inbound.messaging;

import io.awspring.cloud.sqs.annotation.SqsListener;
import io.github.matheuscavalari.accountservice.adapters.inbound.messaging.dto.AccountCreatedMessage;
import io.github.matheuscavalari.accountservice.application.usecase.CreateAccountFromEventService;
import org.springframework.stereotype.Component;

@Component
public class AccountCreatedSqsListener {

    private final CreateAccountFromEventService service;

    public AccountCreatedSqsListener(CreateAccountFromEventService service) {
        this.service = service;
    }

    @SqsListener("conta-bancaria-criada")
    public void onMessage(AccountCreatedMessage message) {
        var a = message.account();
        service.createIfNotExists(a.id(), a.owner(), a.createdAtEpoch(), a.status());
    }
}

