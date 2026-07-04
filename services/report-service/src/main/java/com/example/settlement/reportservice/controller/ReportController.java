package com.example.settlement.reportservice.controller;

import com.example.settlement.reportservice.dto.DailyReportResponse;
import com.example.settlement.reportservice.repository.CurrencySettlementSummary;
import com.example.settlement.reportservice.repository.TransactionRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final TransactionRepository repository;

    public ReportController(TransactionRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/daily")
    public DailyReportResponse daily(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = date != null ? date : LocalDate.now(ZoneOffset.UTC);
        var start = targetDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        var end = targetDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<CurrencySettlementSummary> rows = repository.summarizeSettled(start, end);

        List<DailyReportResponse.CurrencyBreakdown> breakdown = rows.stream()
                .map(row -> new DailyReportResponse.CurrencyBreakdown(
                        row.getCurrency(), row.getSettledCount(), row.getTotalAmount()))
                .toList();

        long totalSettledCount = breakdown.stream().mapToLong(DailyReportResponse.CurrencyBreakdown::settledCount).sum();

        return new DailyReportResponse(targetDate, totalSettledCount, breakdown);
    }
}
