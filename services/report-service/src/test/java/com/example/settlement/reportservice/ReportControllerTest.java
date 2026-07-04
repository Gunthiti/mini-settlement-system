package com.example.settlement.reportservice;

import com.example.settlement.reportservice.dto.DailyReportResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReportControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void summarizesSettledTransactionsPerCurrencyForTheGivenDay() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        var settledAt = today.atTime(10, 0).toInstant(ZoneOffset.UTC);
        var yesterdaySettledAt = today.minusDays(1).atTime(10, 0).toInstant(ZoneOffset.UTC);

        insertTransaction(1L, "100.00", "THB", "SETTLED", settledAt);
        insertTransaction(2L, "50.00", "THB", "SETTLED", settledAt);
        insertTransaction(3L, "20.00", "USD", "SETTLED", settledAt);
        insertTransaction(4L, "999.00", "THB", "PENDING", null);
        insertTransaction(5L, "10.00", "THB", "SETTLED", yesterdaySettledAt);

        DailyReportResponse report = restTemplate.getForObject(
                "http://localhost:" + port + "/api/reports/daily", DailyReportResponse.class);

        assertThat(report.totalSettledCount()).isEqualTo(3);
        assertThat(report.breakdown()).hasSize(2);
        assertThat(report.breakdown())
                .filteredOn(b -> b.currency().equals("THB"))
                .first()
                .satisfies(thb -> {
                    assertThat(thb.settledCount()).isEqualTo(2);
                    assertThat(thb.totalAmount()).isEqualByComparingTo("150.00");
                });
    }

    private void insertTransaction(long id, String amount, String currency, String status, java.time.Instant settledAt) {
        jdbcTemplate.update(
                "insert into transactions (id, amount, currency, status, settled_at) values (?, ?, ?, ?, ?)",
                id, new java.math.BigDecimal(amount), currency, status,
                settledAt == null ? null : java.sql.Timestamp.from(settledAt));
    }
}
