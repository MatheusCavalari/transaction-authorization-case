package io.github.matheuscavalari.accountservice.application.usecase;

import io.github.matheuscavalari.accountservice.adapters.outbound.persistence.jpa.entity.AccountEntity;
import io.github.matheuscavalari.accountservice.adapters.outbound.persistence.jpa.entity.OperationEntity;
import io.github.matheuscavalari.accountservice.adapters.outbound.persistence.jpa.repository.AccountJpaRepository;
import io.github.matheuscavalari.accountservice.adapters.outbound.persistence.jpa.repository.OperationJpaRepository;
import io.github.matheuscavalari.accountservice.application.dto.ApplyOperationCommand;
import io.github.matheuscavalari.accountservice.domain.model.OperationStatus;
import io.github.matheuscavalari.accountservice.domain.model.OperationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplyOperationUseCaseTest {

    private AccountJpaRepository accountRepository;
    private OperationJpaRepository operationRepository;
    private ApplyOperationUseCase useCase;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountJpaRepository.class);
        operationRepository = mock(OperationJpaRepository.class);
        useCase = new ApplyOperationUseCase(accountRepository, operationRepository);
    }

    @Test
    void shouldReturnExistingOperation_whenTransactionIdAlreadyProcessed() {
        UUID accountId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        OperationEntity existing = new OperationEntity(
                txId,
                accountId,
                "DEBIT",
                new BigDecimal("10.00"),
                "BRL",
                "SUCCEEDED",
                OffsetDateTime.parse("2025-12-30T12:00:00-03:00"),
                new BigDecimal("90.00"),
                "BRL",
                OffsetDateTime.parse("2025-12-30T12:00:01-03:00")
        );

        when(operationRepository.findById(txId)).thenReturn(Optional.of(existing));

        ApplyOperationCommand cmd = new ApplyOperationCommand(
                accountId, txId, OperationType.DEBIT, new BigDecimal("10.00"), "BRL",
                OffsetDateTime.parse("2025-12-30T12:00:00-03:00")
        );

        var result = useCase.execute(cmd);

        assertThat(result.status()).isEqualTo(OperationStatus.SUCCEEDED);
        assertThat(result.resultingBalanceAmount()).isEqualByComparingTo("90.00");

        verify(operationRepository).findById(txId);
        verifyNoInteractions(accountRepository);
        verify(operationRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldCreditAndUpdateBalance_whenCreditOperation() {
        UUID accountId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        AccountEntity account = new AccountEntity(
                accountId, "owner-1", "ENABLED",
                new BigDecimal("100.00"), "BRL",
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().minusDays(1)
        );

        when(operationRepository.findById(txId)).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));
        when(operationRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        ApplyOperationCommand cmd = new ApplyOperationCommand(
                accountId, txId, OperationType.CREDIT,
                new BigDecimal("25.50"), "BRL",
                OffsetDateTime.now()
        );

        var result = useCase.execute(cmd);

        assertThat(result.status()).isEqualTo(OperationStatus.SUCCEEDED);
        assertThat(result.resultingBalanceAmount()).isEqualByComparingTo("125.50");

        verify(accountRepository).findByIdForUpdate(accountId);
        verify(operationRepository).saveAndFlush(any(OperationEntity.class));
        verify(accountRepository).save(account);

        assertThat(account.getBalanceAmount()).isEqualByComparingTo("125.50");
    }

    @Test
    void shouldDebitAndUpdateBalance_whenSufficientFunds() {
        UUID accountId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        AccountEntity account = new AccountEntity(
                accountId, "owner-1", "ENABLED",
                new BigDecimal("100.00"), "BRL",
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().minusDays(1)
        );

        when(operationRepository.findById(txId)).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));
        when(operationRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        ApplyOperationCommand cmd = new ApplyOperationCommand(
                accountId, txId, OperationType.DEBIT,
                new BigDecimal("70.00"), "BRL",
                OffsetDateTime.now()
        );

        var result = useCase.execute(cmd);

        assertThat(result.status()).isEqualTo(OperationStatus.SUCCEEDED);
        assertThat(result.resultingBalanceAmount()).isEqualByComparingTo("30.00");

        verify(operationRepository).saveAndFlush(any(OperationEntity.class));
        verify(accountRepository).save(account);

        assertThat(account.getBalanceAmount()).isEqualByComparingTo("30.00");
    }

    @Test
    void shouldFailDebitAndNotUpdateBalance_whenInsufficientFunds() {
        UUID accountId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        AccountEntity account = new AccountEntity(
                accountId, "owner-1", "ENABLED",
                new BigDecimal("50.00"), "BRL",
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().minusDays(1)
        );

        when(operationRepository.findById(txId)).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));
        when(operationRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        ApplyOperationCommand cmd = new ApplyOperationCommand(
                accountId, txId, OperationType.DEBIT,
                new BigDecimal("70.00"), "BRL",
                OffsetDateTime.now()
        );

        var result = useCase.execute(cmd);

        assertThat(result.status()).isEqualTo(OperationStatus.FAILED);
        assertThat(result.resultingBalanceAmount()).isEqualByComparingTo("50.00");
        assertThat(account.getBalanceAmount()).isEqualByComparingTo("50.00");

        verify(operationRepository).saveAndFlush(any(OperationEntity.class));
        verify(accountRepository, never()).save(any(AccountEntity.class));
    }

    @Test
    void shouldThrowWhenCurrencyMismatch() {
        UUID accountId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        AccountEntity account = new AccountEntity(
                accountId, "owner-1", "ENABLED",
                new BigDecimal("100.00"), "BRL",
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().minusDays(1)
        );

        when(operationRepository.findById(txId)).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));

        ApplyOperationCommand cmd = new ApplyOperationCommand(
                accountId, txId, OperationType.DEBIT,
                new BigDecimal("10.00"), "USD",
                OffsetDateTime.now()
        );

        assertThatThrownBy(() -> useCase.execute(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("currency mismatch");

        verify(operationRepository, never()).saveAndFlush(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void shouldHandleDuplicateInsertRace_byReturningExistingOperation() {
        UUID accountId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        AccountEntity account = new AccountEntity(
                accountId, "owner-1", "ENABLED",
                new BigDecimal("100.00"), "BRL",
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().minusDays(1)
        );

        when(operationRepository.findById(txId)).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));

        DataIntegrityViolationException dup = new DataIntegrityViolationException("dup key");
        when(operationRepository.saveAndFlush(any())).thenThrow(dup);

        OperationEntity persistedByOtherThread = new OperationEntity(
                txId, accountId, "CREDIT",
                new BigDecimal("10.00"), "BRL",
                "SUCCEEDED", OffsetDateTime.now(),
                new BigDecimal("110.00"), "BRL",
                OffsetDateTime.now()
        );

        when(operationRepository.findById(txId)).thenReturn(Optional.of(persistedByOtherThread));

        ApplyOperationCommand cmd = new ApplyOperationCommand(
                accountId, txId, OperationType.CREDIT,
                new BigDecimal("10.00"), "BRL",
                OffsetDateTime.now()
        );

        var result = useCase.execute(cmd);

        assertThat(result.status()).isEqualTo(OperationStatus.SUCCEEDED);
        assertThat(result.resultingBalanceAmount()).isEqualByComparingTo("110.00");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void shouldRethrowDuplicateException_whenDuplicateInsertAndOperationStillNotFound() {
        UUID accountId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        AccountEntity account = new AccountEntity(
                accountId, "owner-1", "ENABLED",
                new BigDecimal("100.00"), "BRL",
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().minusDays(1)
        );

        when(operationRepository.findById(txId)).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));

        DataIntegrityViolationException dup = new DataIntegrityViolationException("dup key");
        when(operationRepository.saveAndFlush(any())).thenThrow(dup);

        when(operationRepository.findById(txId)).thenReturn(Optional.empty());

        ApplyOperationCommand cmd = new ApplyOperationCommand(
                accountId, txId, OperationType.CREDIT,
                new BigDecimal("10.00"), "BRL",
                OffsetDateTime.now()
        );

        assertThatThrownBy(() -> useCase.execute(cmd))
                .isSameAs(dup);

        verify(operationRepository).saveAndFlush(any(OperationEntity.class));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenAmountIsZeroOrNegative() {
        UUID accountId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        AccountEntity account = new AccountEntity(
                accountId, "owner-1", "ENABLED",
                new BigDecimal("100.00"), "BRL",
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().minusDays(1)
        );

        when(operationRepository.findById(txId)).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));

        ApplyOperationCommand cmd = new ApplyOperationCommand(
                accountId, txId, OperationType.CREDIT,
                BigDecimal.ZERO, "BRL",
                OffsetDateTime.now()
        );

        assertThatThrownBy(() -> useCase.execute(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amountValue must be > 0");

        verify(operationRepository, never()).saveAndFlush(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenCurrencyIsBlank() {
        UUID accountId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        AccountEntity account = new AccountEntity(
                accountId, "owner-1", "ENABLED",
                new BigDecimal("100.00"), "BRL",
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().minusDays(1)
        );

        when(operationRepository.findById(txId)).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));

        ApplyOperationCommand cmd = new ApplyOperationCommand(
                accountId, txId, OperationType.CREDIT,
                new BigDecimal("10.00"), "  ",
                OffsetDateTime.now()
        );

        assertThatThrownBy(() -> useCase.execute(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amountCurrency is required");

        verify(operationRepository, never()).saveAndFlush(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenTimestampIsNull() {
        UUID accountId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        AccountEntity account = new AccountEntity(
                accountId, "owner-1", "ENABLED",
                new BigDecimal("100.00"), "BRL",
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().minusDays(1)
        );

        when(operationRepository.findById(txId)).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));

        ApplyOperationCommand cmd = new ApplyOperationCommand(
                accountId, txId, OperationType.CREDIT,
                new BigDecimal("10.00"), "BRL",
                null
        );

        assertThatThrownBy(() -> useCase.execute(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("timestamp is required");

        verify(operationRepository, never()).saveAndFlush(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenTypeIsNull() {
        UUID accountId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        AccountEntity account = new AccountEntity(
                accountId, "owner-1", "ENABLED",
                new BigDecimal("100.00"), "BRL",
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().minusDays(1)
        );

        when(operationRepository.findById(txId)).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));

        ApplyOperationCommand cmd = new ApplyOperationCommand(
                accountId, txId, null,
                new BigDecimal("10.00"), "BRL",
                OffsetDateTime.now()
        );

        assertThatThrownBy(() -> useCase.execute(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("type is required");

        verify(operationRepository, never()).saveAndFlush(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void shouldThrowAccountNotFoundException_withExpectedMessage() {
        UUID accountId = UUID.randomUUID();
        UUID txId = UUID.randomUUID();

        when(operationRepository.findById(txId)).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.empty());

        ApplyOperationCommand cmd = new ApplyOperationCommand(
                accountId, txId, OperationType.DEBIT,
                new BigDecimal("10.00"), "BRL",
                OffsetDateTime.now()
        );

        assertThatThrownBy(() -> useCase.execute(cmd))
                .isInstanceOf(ApplyOperationUseCase.AccountNotFoundException.class)
                .hasMessage("Account not found: " + accountId);

        verify(operationRepository, never()).saveAndFlush(any());
        verify(accountRepository, never()).save(any());
    }
}
