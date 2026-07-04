package com.example.settlement.transactionapi.controller;

import com.example.settlement.transactionapi.dto.CreateTransactionRequest;
import com.example.settlement.transactionapi.dto.TransactionResponse;
import com.example.settlement.transactionapi.entity.Transaction;
import com.example.settlement.transactionapi.repository.TransactionRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository repository;

    public TransactionController(TransactionRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody CreateTransactionRequest request) {
        repository.findByReference(request.reference()).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "transaction with reference '" + request.reference() + "' already exists");
        });

        Transaction transaction = new Transaction();
        transaction.setReference(request.reference());
        transaction.setAmount(request.amount());
        transaction.setCurrency(request.currency().toUpperCase());

        Transaction saved = repository.save(transaction);
        TransactionResponse body = TransactionResponse.from(saved);
        return ResponseEntity.created(URI.create("/api/transactions/" + saved.getId())).body(body);
    }

    @GetMapping("/{id}")
    public TransactionResponse getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(TransactionResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "transaction not found"));
    }

    @GetMapping
    public List<TransactionResponse> list() {
        return repository.findAll().stream().map(TransactionResponse::from).toList();
    }
}
