package com.example.settlement.transactionapi.dto;

import com.example.settlement.transactionapi.entity.Transaction;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        String reference,
        BigDecimal amount,
        String currency,
        String status,
        Instant createdAt,
        Instant settledAt
) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getReference(),
                t.getAmount(),
                t.getCurrency(),
                t.getStatus().name(),
                t.getCreatedAt(),
                t.getSettledAt()
        );
    }
}
