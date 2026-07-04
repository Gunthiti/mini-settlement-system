package com.example.settlement.settlementworker.service;

import com.example.settlement.settlementworker.entity.Transaction;
import com.example.settlement.settlementworker.entity.TransactionStatus;
import com.example.settlement.settlementworker.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SettlementServiceTest {

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private TransactionRepository repository;

    @Test
    void settlesOnlyPendingTransactions() {
        repository.save(pending(1L));
        repository.save(pending(2L));
        repository.save(settled(3L));

        int settledCount = settlementService.settlePendingTransactions();

        assertThat(settledCount).isEqualTo(2);
        assertThat(repository.findByStatus(TransactionStatus.PENDING)).isEmpty();
        assertThat(repository.findByStatus(TransactionStatus.SETTLED)).hasSize(3);
        assertThat(repository.findById(1L).orElseThrow().getSettledAt()).isNotNull();
    }

    @Test
    void returnsZeroWhenNothingToSettle() {
        assertThat(settlementService.settlePendingTransactions()).isZero();
    }

    private Transaction pending(long id) {
        Transaction t = new Transaction();
        t.setId(id);
        t.setStatus(TransactionStatus.PENDING);
        return t;
    }

    private Transaction settled(long id) {
        Transaction t = pending(id);
        t.setStatus(TransactionStatus.SETTLED);
        return t;
    }
}
