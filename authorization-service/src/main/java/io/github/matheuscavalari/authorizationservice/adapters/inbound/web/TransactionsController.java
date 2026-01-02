package io.github.matheuscavalari.authorizationservice.adapters.inbound.web;

import io.github.matheuscavalari.authorizationservice.adapters.inbound.web.api.TransactionsApi;
import io.github.matheuscavalari.authorizationservice.adapters.inbound.web.dto.AuthorizeTransactionRequest;
import io.github.matheuscavalari.authorizationservice.adapters.inbound.web.dto.AuthorizeTransactionResponse;
import io.github.matheuscavalari.authorizationservice.adapters.outbound.accountclient.AccountServiceClient;
import io.github.matheuscavalari.authorizationservice.adapters.outbound.accountclient.dto.ApplyOperationRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class TransactionsController implements TransactionsApi {

    private final AccountServiceClient accountServiceClient;

    public TransactionsController(AccountServiceClient accountServiceClient) {
        this.accountServiceClient = accountServiceClient;
    }

    @Override
    public ResponseEntity<AuthorizeTransactionResponse> authorize(UUID transactionId, AuthorizeTransactionRequest req) {

        var applyReq = new ApplyOperationRequest(
                req.type(),
                req.amount().value(),
                req.amount().currency(),
                req.timestamp()
        );

        var applyRes = accountServiceClient.applyOperation(req.accountId(), transactionId, applyReq);

        var response = new AuthorizeTransactionResponse(
                new AuthorizeTransactionResponse.Transaction(
                        applyRes.transactionId(),
                        applyRes.type(),
                        new AuthorizeTransactionResponse.Amount(applyRes.amountValue(), applyRes.amountCurrency()),
                        applyRes.status(),
                        applyRes.timestamp()
                ),
                new AuthorizeTransactionResponse.Account(
                        applyRes.accountId(),
                        new AuthorizeTransactionResponse.Balance(applyRes.resultingBalanceAmount(), applyRes.resultingBalanceCurrency())
                )
        );

        return ResponseEntity.ok(response);
    }
}

