package io.github.matheuscavalari.accountservice.application.usecase;

import io.github.matheuscavalari.accountservice.adapters.outbound.persistence.jpa.entity.AccountEntity;
import io.github.matheuscavalari.accountservice.adapters.outbound.persistence.jpa.repository.AccountJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class CreateAccountFromEventService {

    private final AccountJpaRepository accountRepository;
    private final String defaultCurrency;

    public CreateAccountFromEventService(AccountJpaRepository accountRepository,
                                         @Value("${account.default-currency:BRL}") String defaultCurrency) {
        this.accountRepository = accountRepository;
        this.defaultCurrency = defaultCurrency;
    }

    @Transactional
    public void createIfNotExists(UUID accountId,
                                  String owner,
                                  String createdAtEpoch,
                                  String status) {

        OffsetDateTime createdAt = parseEpochSeconds(createdAtEpoch);
        OffsetDateTime now = OffsetDateTime.now();

        AccountEntity entity = new AccountEntity(
                accountId,
                owner,
                status != null ? status : "ENABLED",
                BigDecimal.ZERO,
                defaultCurrency,
                createdAt != null ? createdAt : now,
                now
        );

        try {
            accountRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            // idempotência: conta já existe → ACK normal da mensagem
            return;
        }
    }


    private static OffsetDateTime parseEpochSeconds(String epoch) {
        if (epoch == null || epoch.isBlank()) return null;
        long seconds = Long.parseLong(epoch);
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneOffset.UTC);
    }
}

