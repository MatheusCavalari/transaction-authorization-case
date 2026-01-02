package io.github.matheuscavalari.accountservice.adapters.outbound.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "owner", nullable = false)
    private String owner;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "balance_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal balanceAmount;

    @Column(name = "balance_currency", nullable = false, length = 3)
    private String balanceCurrency;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected AccountEntity() {
        // JPA
    }

    public AccountEntity(UUID id,
                         String owner,
                         String status,
                         BigDecimal balanceAmount,
                         String balanceCurrency,
                         OffsetDateTime createdAt,
                         OffsetDateTime updatedAt) {
        this.id = id;
        this.owner = owner;
        this.status = status;
        this.balanceAmount = balanceAmount;
        this.balanceCurrency = balanceCurrency;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    void prePersist() {
        var now = OffsetDateTime.now();
        if (this.createdAt == null) this.createdAt = now;
        if (this.updatedAt == null) this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getBalanceAmount() {
        return balanceAmount;
    }

    public String getBalanceCurrency() {
        return balanceCurrency;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setBalanceAmount(BigDecimal balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public void setBalanceCurrency(String balanceCurrency) {
        this.balanceCurrency = balanceCurrency;
    }
}
