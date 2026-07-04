package com.example.settlement.settlementworker.repository;

import com.example.settlement.settlementworker.entity.Transaction;
import com.example.settlement.settlementworker.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByStatus(TransactionStatus status);
}
