package com.vegatrader.upstox.api.broker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.*;

/**
 * Broker Instrument Prewarm Job per arch/a4.md section 1.
 * Pre-resolves broker contracts T-1 before expiry to avoid cold-start delays.
 * 
 * @since 4.6.0
 */
@Component
public class BrokerInstrumentPrewarmJob {

    private static final Logger logger = LoggerFactory.getLogger(BrokerInstrumentPrewarmJob.class);

    private final MultiBrokerResolver brokerResolver;

    public BrokerInstrumentPrewarmJob(MultiBrokerResolver brokerResolver) {
        this.brokerResolver = brokerResolver;
    }

    /**
     * Runs daily at 6:30 PM IST (post-market).
     * Pre-warms broker instrument mappings for T+1 expiry contracts.
     */
    @Scheduled(cron = "0 30 18 * * MON-FRI", zone = "Asia/Kolkata")
    public void prewarmTomorrowExpiry() {
        logger.info("Starting T-1 broker instrument pre-warm...");

        LocalDate targetExpiry = getNextTradingDay();
        logger.info("Target expiry date: {}", targetExpiry);

        int resolved = 0;
        int failed = 0;

        // Get options expiring tomorrow
        List<OptionDescriptor> options = getOptionsExpiringOn(targetExpiry);
        logger.info("Found {} options to pre-warm", options.size());

        for (OptionDescriptor option : options) {
            try {
                brokerResolver.resolveAcrossBrokers(option);
                resolved++;
            } catch (Exception e) {
                logger.warn("Pre-warm failed for {}: {}", option, e.getMessage());
                failed++;
            }
        }

        logger.info("Pre-warm complete: resolved={}, failed={}", resolved, failed);
    }

    /**
     * Manual trigger for pre-warm (for testing/ops).
     */
    public void triggerManualPrewarm(LocalDate targetExpiry) {
        logger.info("Manual pre-warm triggered for {}", targetExpiry);

        List<OptionDescriptor> options = getOptionsExpiringOn(targetExpiry);
        options.forEach(option -> {
            try {
                brokerResolver.resolveAcrossBrokers(option);
            } catch (Exception e) {
                logger.warn("Manual pre-warm failed: {}", e.getMessage());
            }
        });
    }

    // === Private Helpers ===

    private LocalDate getNextTradingDay() {
        LocalDate today = LocalDate.now();
        LocalDate next = today.plusDays(1);

        // Skip weekends
        while (next.getDayOfWeek() == DayOfWeek.SATURDAY
                || next.getDayOfWeek() == DayOfWeek.SUNDAY) {
            next = next.plusDays(1);
        }

        // TODO: Integrate with trading holiday calendar
        return next;
    }

    private List<OptionDescriptor> getOptionsExpiringOn(LocalDate expiry) {
        // TODO: Query from fo_contract_lifecycle where expiry_date = targetExpiry
        // For now, return empty - to be implemented with InstrumentQueryService
        return List.of();
    }

    /**
     * Option descriptor for pre-warm resolution.
     */
    public record OptionDescriptor(
            String underlyingKey,
            LocalDate expiry,
            java.math.BigDecimal strike,
            String optionType) {
    }
}
