package com.example.settlement.settlementworker.runner;

import com.example.settlement.settlementworker.service.SettlementService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Used for local development (docker-compose): keeps the JVM alive and
 * settles transactions on a fixed interval, like a cron-in-process worker
 * you'd run on a plain VM. Disabled in K8s in favour of {@link OneShotSettlementRunner},
 * which is a better fit for CronJob (see that class for why).
 */
@Component
@ConditionalOnProperty(name = "settlement.mode", havingValue = "scheduled", matchIfMissing = true)
public class ScheduledSettlementRunner {

    private final SettlementService settlementService;

    public ScheduledSettlementRunner(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @Scheduled(fixedRateString = "${settlement.interval-ms:60000}")
    public void run() {
        settlementService.settlePendingTransactions();
    }
}
