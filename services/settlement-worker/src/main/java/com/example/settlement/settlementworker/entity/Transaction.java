package com.example.settlement.settlementworker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * settlement-worker only cares about, and only maps, the columns it needs
 * to move a transaction from PENDING to SETTLED. It never inserts rows --
 * transaction-api owns that -- so reference/amount/currency are deliberately
 * left unmapped even though they exist in the shared table.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "settled_at")
    private Instant settledAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public Instant getSettledAt() {
        return settledAt;
    }

    public void setSettledAt(Instant settledAt) {
        this.settledAt = settledAt;
    }
}
