package io.github.matheuscavalari.accountservice.adapters.inbound.web;

import io.github.matheuscavalari.accountservice.adapters.inbound.web.api.AccountOperationsApi;
import io.github.matheuscavalari.accountservice.adapters.inbound.web.dto.ApplyOperationRequest;
import io.github.matheuscavalari.accountservice.adapters.inbound.web.dto.ApplyOperationResponse;
import io.github.matheuscavalari.accountservice.application.dto.ApplyOperationCommand;
import io.github.matheuscavalari.accountservice.application.usecase.ApplyOperationUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AccountOperationsController implements AccountOperationsApi {

    private final ApplyOperationUseCase useCase;

    public AccountOperationsController(ApplyOperationUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public ResponseEntity<ApplyOperationResponse> applyOperation(
            UUID accountId,
            UUID transactionId,
            ApplyOperationRequest request
    ) {
        var result = useCase.execute(new ApplyOperationCommand(
                accountId,
                transactionId,
                request.type(),
                request.amountValue(),
                request.amountCurrency(),
                request.timestamp()
        ));

        return ResponseEntity.ok(new ApplyOperationResponse(
                result.transactionId(),
                result.accountId(),
                result.type(),
                result.amountValue(),
                result.amountCurrency(),
                result.status(),
                result.timestamp(),
                result.resultingBalanceAmount(),
                result.resultingBalanceCurrency()
        ));
    }
}
