package com.example.settlement.reportservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DailyReportResponse(
        LocalDate date,
        long totalSettledCount,
        List<CurrencyBreakdown> breakdown
) {
    public record CurrencyBreakdown(String currency, long settledCount, BigDecimal totalAmount) {
    }
}
