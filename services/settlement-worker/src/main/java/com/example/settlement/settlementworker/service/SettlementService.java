package com.example.settlement.settlementworker.service;

import com.example.settlement.settlementworker.entity.Transaction;
import com.example.settlement.settlementworker.entity.TransactionStatus;
import com.example.settlement.settlementworker.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class SettlementService {

    private static final Logger log = LoggerFactory.getLogger(SettlementService.class);

    private final TransactionRepository repository;

    public SettlementService(TransactionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public int settlePendingTransactions() {
        List<Transaction> pending = repository.findByStatus(TransactionStatus.PENDING);
        if (pending.isEmpty()) {
            log.info("settlement pass: nothing to settle");
            return 0;
        }

        Instant now = Instant.now();
        for (Transaction transaction : pending) {
            transaction.setStatus(TransactionStatus.SETTLED);
            transaction.setSettledAt(now);
        }
        repository.saveAll(pending);

        log.info("settlement pass: settled {} transaction(s)", pending.size());
        return pending.size();
    }
}
