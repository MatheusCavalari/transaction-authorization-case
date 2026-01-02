package io.github.matheuscavalari.accountservice.adapters.outbound.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
@Entity
@Table(name = "operations")
public class OperationEntity {
    @Id
    @Column(name = "transaction_id", nullable = false, updatable = false)
    private UUID transactionId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "type", nullable = false)
    private String type; // CREDIT | DEBIT

    @Column(name = "amount_value", nullable = false, precision = 18, scale = 2)
    private BigDecimal amountValue;

    @Column(name = "amount_currency", nullable = false, length = 3)
    private String amountCurrency;

    @Column(name = "status", nullable = false)
    private String status; // SUCCEEDED | FAILED

    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp;

    @Column(name = "resulting_balance_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal resultingBalanceAmount;

    @Column(name = "resulting_balance_currency", nullable = false, length = 3)
    private String resultingBalanceCurrency;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected OperationEntity() {
        // JPA
    }

    public OperationEntity(UUID transactionId,
                           UUID accountId,
                           String type,
                           BigDecimal amountValue,
                           String amountCurrency,
                           String status,
                           OffsetDateTime timestamp,
                           BigDecimal resultingBalanceAmount,
                           String resultingBalanceCurrency,
                           OffsetDateTime createdAt) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.type = type;
        this.amountValue = amountValue;
        this.amountCurrency = amountCurrency;
        this.status = status;
        this.timestamp = timestamp;
        this.resultingBalanceAmount = resultingBalanceAmount;
        this.resultingBalanceCurrency = resultingBalanceCurrency;
        this.createdAt = createdAt;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmountValue() {
        return amountValue;
    }

    public String getAmountCurrency() {
        return amountCurrency;
    }

    public String getStatus() {
        return status;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public BigDecimal getResultingBalanceAmount() {
        return resultingBalanceAmount;
    }

    public String getResultingBalanceCurrency() {
        return resultingBalanceCurrency;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
