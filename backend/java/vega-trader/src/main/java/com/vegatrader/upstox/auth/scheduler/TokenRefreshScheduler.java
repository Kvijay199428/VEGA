package com.vegatrader.upstox.auth.scheduler;

import com.vegatrader.upstox.auth.provider.TokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for automatic token refresh at 2:30 AM.
 * Runs 1 hour before token expiry (3:30 AM).
 *
 * @since 2.0.0
 */
@Component
public class TokenRefreshScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TokenRefreshScheduler.class);

    private final TokenProvider tokenProvider;

    public TokenRefreshScheduler(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    /**
     * Scheduled refresh at 2:30 AM daily.
     * Cron: "0 30 2 * * *" = Every day at 2:30 AM
     */
    @Scheduled(cron = "0 30 2 * * *")
    public void refreshTokensDaily() {
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("🔄 Scheduled Token Refresh - 2:30 AM");
        logger.info("═══════════════════════════════════════════════════════");

        try {
            tokenProvider.refreshCache();
            logger.info("✓ Token cache refreshed successfully");
        } catch (Exception e) {
            logger.error("✗ Error during scheduled token refresh", e);
        }

        logger.info("═══════════════════════════════════════════════════════");
    }

    /**
     * Periodic cache refresh every 30 minutes (optional, for resilience).
     */
    @Scheduled(fixedRate = 1800000) // 30 minutes
    public void refreshCachePeriodically() {
        logger.debug("🔄 Periodic token cache refresh (30 min interval)");

        try {
            tokenProvider.refreshCache();
        } catch (Exception e) {
            logger.error("Error during periodic cache refresh", e);
        }
    }
}
