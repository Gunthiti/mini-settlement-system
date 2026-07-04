package com.example.settlement.reportservice.repository;

import java.math.BigDecimal;

public interface CurrencySettlementSummary {
    String getCurrency();

    Long getSettledCount();

    BigDecimal getTotalAmount();
}
