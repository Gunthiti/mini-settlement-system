package com.example.settlement.settlementworker.runner;

import com.example.settlement.settlementworker.service.SettlementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Used when this same jar is run as a Kubernetes CronJob: runs exactly one
 * settlement pass, then exits. Each scheduled firing of the CronJob starts a
 * brand new Pod that runs this and terminates -- there is no long-lived
 * process to schedule *inside*, K8s itself is the scheduler. Enabled by
 * setting SETTLEMENT_MODE=oneshot on the container (see the CronJob manifest
 * added in the Kubernetes phase).
 */
@Component
@ConditionalOnProperty(name = "settlement.mode", havingValue = "oneshot")
public class OneShotSettlementRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(OneShotSettlementRunner.class);

    private final SettlementService settlementService;
    private final ApplicationContext applicationContext;

    public OneShotSettlementRunner(SettlementService settlementService, ApplicationContext applicationContext) {
        this.settlementService = settlementService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(String... args) {
        int settled = settlementService.settlePendingTransactions();
        log.info("one-shot settlement run complete, settled={}", settled);

        // Exit cleanly so the K8s Job controller sees a successful
        // completion (exit code 0) rather than a Pod that just hangs.
        int exitCode = SpringApplication.exit(applicationContext, () -> 0);
        System.exit(exitCode);
    }
}
