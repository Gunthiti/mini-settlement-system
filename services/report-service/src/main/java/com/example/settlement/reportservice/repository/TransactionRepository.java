package com.example.settlement.reportservice.repository;

import com.example.settlement.reportservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
            select t.currency as currency, count(t) as settledCount, sum(t.amount) as totalAmount
            from Transaction t
            where t.status = com.example.settlement.reportservice.entity.TransactionStatus.SETTLED
              and t.settledAt >= :start and t.settledAt < :end
            group by t.currency
            order by t.currency
            """)
    List<CurrencySettlementSummary> summarizeSettled(@Param("start") Instant start, @Param("end") Instant end);
}
