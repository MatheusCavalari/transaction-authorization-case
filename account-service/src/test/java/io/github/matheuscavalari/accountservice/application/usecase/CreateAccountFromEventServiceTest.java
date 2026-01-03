package io.github.matheuscavalari.accountservice.application.usecase;

import io.github.matheuscavalari.accountservice.adapters.outbound.persistence.jpa.entity.AccountEntity;
import io.github.matheuscavalari.accountservice.adapters.outbound.persistence.jpa.repository.AccountJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CreateAccountFromEventServiceTest {

    private AccountJpaRepository repo;
    private CreateAccountFromEventService service;

    @BeforeEach
    void setUp() {
        repo = mock(AccountJpaRepository.class);
        service = new CreateAccountFromEventService(repo, "BRL");
    }

    @Test
    void shouldCreateAccountWithZeroBalanceAndDefaultCurrency() {
        UUID accountId = UUID.randomUUID();

        when(repo.existsById(accountId)).thenReturn(false);

        service.createIfNotExists(accountId, "matheus", "1634874339", "ENABLED");

        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(repo).save(captor.capture());

        AccountEntity saved = captor.getValue();

        assertThat(saved.getId()).isEqualTo(accountId);
        assertThat(saved.getOwner()).isEqualTo("matheus");
        assertThat(saved.getStatus()).isEqualTo("ENABLED");

        // requisito do case: saldo inicial ZERO
        assertThat(saved.getBalanceAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saved.getBalanceCurrency()).isEqualTo("BRL");
    }

    @Test
    void shouldParseCreatedAtEpochSecondsAsUtc() {
        UUID accountId = UUID.randomUUID();
        when(repo.existsById(accountId)).thenReturn(false);

        // 1634874339 -> 2021-10-22T...Z (UTC)
        service.createIfNotExists(accountId, "owner", "1634874339", "ENABLED");

        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(repo).save(captor.capture());

        AccountEntity saved = captor.getValue();
        OffsetDateTime createdAt = saved.getCreatedAt();

        assertThat(createdAt.getOffset()).isEqualTo(ZoneOffset.UTC);
        assertThat(createdAt.toEpochSecond()).isEqualTo(1634874339L);
    }

    @Test
    void shouldNotFailOnDuplicateInsertRace_condition() {
        UUID accountId = UUID.randomUUID();

        when(repo.existsById(accountId)).thenReturn(false);
        when(repo.save(any(AccountEntity.class))).thenThrow(new DataIntegrityViolationException("dup key"));

        // não deve lançar
        service.createIfNotExists(accountId, "matheus", "1634874339", "ENABLED");

        verify(repo).save(any(AccountEntity.class));
    }

    @Test
    void shouldHandleNullOrBlankEpochByUsingNow() {
        UUID accountId = UUID.randomUUID();
        when(repo.existsById(accountId)).thenReturn(false);

        service.createIfNotExists(accountId, "matheus", " ", "ENABLED");

        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(repo).save(captor.capture());

        AccountEntity saved = captor.getValue();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldHandleNullEpochByUsingNow() {
        UUID accountId = UUID.randomUUID();
        when(repo.existsById(accountId)).thenReturn(false);

        service.createIfNotExists(accountId, "matheus", null, "ENABLED");

        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(repo).save(captor.capture());

        AccountEntity saved = captor.getValue();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldDefaultStatusToEnabledWhenStatusIsNull() {
        UUID accountId = UUID.randomUUID();
        when(repo.existsById(accountId)).thenReturn(false);

        service.createIfNotExists(accountId, "matheus", "1634874339", null);

        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(repo).save(captor.capture());

        AccountEntity saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo("ENABLED");
    }
}

