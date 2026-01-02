package io.github.matheuscavalari.accountservice.application.usecase;

import io.github.matheuscavalari.accountservice.adapters.outbound.persistence.jpa.entity.AccountEntity;
import io.github.matheuscavalari.accountservice.adapters.outbound.persistence.jpa.entity.OperationEntity;
import io.github.matheuscavalari.accountservice.adapters.outbound.persistence.jpa.repository.AccountJpaRepository;
import io.github.matheuscavalari.accountservice.adapters.outbound.persistence.jpa.repository.OperationJpaRepository;
import io.github.matheuscavalari.accountservice.application.dto.ApplyOperationCommand;
import io.github.matheuscavalari.accountservice.application.dto.ApplyOperationResult;
import io.github.matheuscavalari.accountservice.domain.model.OperationStatus;
import io.github.matheuscavalari.accountservice.domain.model.OperationType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class ApplyOperationUseCase {

    private final AccountJpaRepository accountRepository;
    private final OperationJpaRepository operationRepository;

    public ApplyOperationUseCase(AccountJpaRepository accountRepository,
                                 OperationJpaRepository operationRepository) {
        this.accountRepository = accountRepository;
        this.operationRepository = operationRepository;
    }

    @Transactional
    public ApplyOperationResult execute(ApplyOperationCommand cmd) {
        // 1) Idempotência: se já existe operação com esse transactionId, retorna o mesmo resultado
        var existing = operationRepository.findById(cmd.transactionId());
        if (existing.isPresent()) {
            return toResult(existing.get());
        }

        // 2) Validações que NÃO dependem do banco (antes do lock)
        validateCommand(cmd);

        // 3) Lock pessimista na conta (SELECT ... FOR UPDATE)
        AccountEntity account = accountRepository.findByIdForUpdate(cmd.accountId())
                .orElseThrow(() -> new AccountNotFoundException(cmd.accountId()));

        // 4) Validações que dependem da conta (depois do lock)
        validateAgainstAccount(cmd, account);

        // 5) Regra de negócio: calcula saldo resultante e status
        BigDecimal current = account.getBalanceAmount();
        BigDecimal resulting;
        OperationStatus status;

        if (cmd.type() == OperationType.CREDIT) {
            resulting = current.add(cmd.amountValue());
            status = OperationStatus.SUCCEEDED;
        } else { // DEBIT
            BigDecimal candidate = current.subtract(cmd.amountValue());
            if (candidate.compareTo(BigDecimal.ZERO) < 0) {
                // saldo insuficiente -> FAILED, não altera saldo
                resulting = current;
                status = OperationStatus.FAILED;
            } else {
                resulting = candidate;
                status = OperationStatus.SUCCEEDED;
            }
        }

        // 6) Persistência da operação (idempotência é garantida pelo PK transaction_id)
        OffsetDateTime now = OffsetDateTime.now();
        OperationEntity op = new OperationEntity(
                cmd.transactionId(),
                cmd.accountId(),
                cmd.type().name(),
                cmd.amountValue(),
                cmd.amountCurrency(),
                status.name(),
                cmd.timestamp(),
                resulting,
                account.getBalanceCurrency(),
                now
        );

        try {
            // salva a operação primeiro para garantir idempotência antes de atualizar saldo
            operationRepository.saveAndFlush(op);
        } catch (DataIntegrityViolationException dup) {
            // outra thread/processo inseriu a mesma operação ao mesmo tempo -> retorna a já existente
            return operationRepository.findById(cmd.transactionId())
                    .map(this::toResult)
                    .orElseThrow(() -> dup);
        }

        // 7) Se SUCCEEDED, atualiza saldo (lock já está segurando a linha)
        if (status == OperationStatus.SUCCEEDED) {
            account.setBalanceAmount(resulting);
            // updatedAt será preenchido automaticamente via @PreUpdate
            accountRepository.save(account);
        }

        return new ApplyOperationResult(
                cmd.transactionId(),
                cmd.type(),
                cmd.amountValue(),
                cmd.amountCurrency(),
                status,
                cmd.timestamp(),
                cmd.accountId(),
                resulting,
                account.getBalanceCurrency()
        );
    }

    private void validateCommand(ApplyOperationCommand cmd) {
        if (cmd.type() == null) {
            throw new IllegalArgumentException("type is required");
        }
        if (cmd.amountValue() == null || cmd.amountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amountValue must be > 0");
        }
        if (cmd.amountCurrency() == null || cmd.amountCurrency().isBlank()) {
            throw new IllegalArgumentException("amountCurrency is required");
        }
        if (cmd.timestamp() == null) {
            throw new IllegalArgumentException("timestamp is required");
        }
    }

    private void validateAgainstAccount(ApplyOperationCommand cmd, AccountEntity account) {
        if (!cmd.amountCurrency().equalsIgnoreCase(account.getBalanceCurrency())) {
            throw new IllegalArgumentException(
                    "Currency mismatch: operation currency '%s' does not match account currency '%s'"
                            .formatted(cmd.amountCurrency(), account.getBalanceCurrency())
            );
        }
    }

    private ApplyOperationResult toResult(OperationEntity op) {
        return new ApplyOperationResult(
                op.getTransactionId(),
                OperationType.valueOf(op.getType()),
                op.getAmountValue(),
                op.getAmountCurrency(),
                OperationStatus.valueOf(op.getStatus()),
                op.getTimestamp(),
                op.getAccountId(),
                op.getResultingBalanceAmount(),
                op.getResultingBalanceCurrency()
        );
    }

    public static class AccountNotFoundException extends RuntimeException {
        public AccountNotFoundException(UUID accountId) {
            super("Account not found: " + accountId);
        }
    }
}
