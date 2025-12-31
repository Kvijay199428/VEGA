package com.vegatrader.upstox.api.instrument.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Scheduler for daily instrument data refresh.
 * 
 * <p>
 * Refresh sequence (runs at 6 AM IST):
 * <ol>
 * <li>Load BOD instruments (NSE)</li>
 * <li>Load Suspended overlay</li>
 * <li>Load MIS overlay</li>
 * <li>Load MTF overlay</li>
 * </ol>
 * 
 * @since 4.0.0
 */
@Component
public class DailyRefreshScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DailyRefreshScheduler.class);
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final InstrumentLoaderService loaderService;

    @Value("${instrument.refresh.enabled:true}")
    private boolean refreshEnabled;

    @Value("${instrument.refresh.auto-start:false}")
    private boolean autoStart;

    private volatile boolean lastRefreshSuccess = false;
    private volatile LocalDateTime lastRefreshTime = null;

    public DailyRefreshScheduler(InstrumentLoaderService loaderService) {
        this.loaderService = loaderService;
    }

    /**
     * Scheduled job for daily instrument refresh at 6:00 AM IST.
     * 
     * Cron: Second Minute Hour DayOfMonth Month DayOfWeek
     * "0 0 6 * * *" = At 06:00:00 every day
     */
    @Scheduled(cron = "0 0 6 * * *", zone = "Asia/Kolkata")
    public void scheduledDailyRefresh() {
        if (!refreshEnabled) {
            logger.info("Instrument refresh is disabled, skipping scheduled run");
            return;
        }

        logger.info("=== Scheduled Daily Instrument Refresh Starting ===");
        performRefresh();
    }

    /**
     * Performs the actual refresh operation.
     */
    public void performRefresh() {
        long start = System.currentTimeMillis();

        try {
            loaderService.performDailyRefresh();

            lastRefreshSuccess = true;
            lastRefreshTime = LocalDateTime.now(IST);

            long elapsed = System.currentTimeMillis() - start;
            logger.info("=== Daily Instrument Refresh Completed in {}ms ===", elapsed);

        } catch (Exception e) {
            lastRefreshSuccess = false;
            lastRefreshTime = LocalDateTime.now(IST);

            logger.error("Daily instrument refresh failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Manually triggers a refresh (called from controller or CLI).
     */
    public void triggerManualRefresh() {
        logger.info("Manual instrument refresh triggered");
        performRefresh();
    }

    /**
     * Gets last refresh status.
     */
    public boolean isLastRefreshSuccess() {
        return lastRefreshSuccess;
    }

    /**
     * Gets last refresh time.
     */
    public LocalDateTime getLastRefreshTime() {
        return lastRefreshTime;
    }

    /**
     * Checks if refresh is enabled.
     */
    public boolean isRefreshEnabled() {
        return refreshEnabled;
    }

    /**
     * Enables or disables automated refresh.
     */
    public void setRefreshEnabled(boolean enabled) {
        this.refreshEnabled = enabled;
        logger.info("Instrument refresh enabled: {}", enabled);
    }
}
