package com.vegatrader.upstox.api.profile.service;

import com.vegatrader.upstox.api.profile.model.FundsMargin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Funds and margin service with caching and maintenance guard.
 * Per profile/a1.md section 5.
 * 
 * CRITICAL: Maintenance window 00:00-05:30 IST
 * 
 * @since 4.8.0
 */
@Service
public class FundsMarginService {

    private static final Logger logger = LoggerFactory.getLogger(FundsMarginService.class);

    private static final int DEFAULT_CACHE_TTL_SECONDS = 5; // 5 seconds
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final LocalTime MAINTENANCE_START = LocalTime.MIDNIGHT;
    private static final LocalTime MAINTENANCE_END = LocalTime.of(5, 30);

    // In-memory cache
    private final Map<String, FundsMargin> fundsCache = new ConcurrentHashMap<>();

    /**
     * Get funds/margin (from cache or fetch).
     */
    public FundsMargin getFunds(String userId) {
        // Check maintenance window first
        if (isMaintenanceWindow()) {
            logger.warn("Funds service in maintenance window");
            throw new FundsMaintenanceException("Funds service unavailable (00:00-05:30 IST)");
        }

        FundsMargin cached = fundsCache.get(userId);

        if (cached != null && !cached.isStale(DEFAULT_CACHE_TTL_SECONDS)) {
            logger.debug("Funds cache hit for user {}", userId);
            return cached;
        }

        // Fetch from broker
        FundsMargin funds = fetchFromBroker(userId);
        fundsCache.put(userId, funds);

        logger.info("Funds fetched for user {}", userId);
        return funds;
    }

    /**
     * Force refresh funds.
     */
    public FundsMargin refreshFunds(String userId) {
        fundsCache.remove(userId);
        return getFunds(userId);
    }

    /**
     * Check if sufficient margin for order.
     */
    public boolean hasSufficientMargin(String userId, double required) {
        return getFunds(userId).hasSufficientMargin(required);
    }

    /**
     * Get available margin.
     */
    public double getAvailableMargin(String userId) {
        return getFunds(userId).availableMargin();
    }

    /**
     * Get margin utilization percentage.
     */
    public double getMarginUtilization(String userId) {
        return getFunds(userId).getUtilizationPct();
    }

    /**
     * Check if in maintenance window (00:00-05:30 IST).
     * Per profile/a1.md section 5.3.
     */
    public boolean isMaintenanceWindow() {
        LocalTime now = LocalTime.now(IST);
        return !now.isBefore(MAINTENANCE_START) && now.isBefore(MAINTENANCE_END);
    }

    /**
     * Fetch funds from broker (placeholder for actual API call).
     */
    private FundsMargin fetchFromBroker(String userId) {
        // TODO: Implement actual Upstox API call via BrokerAdapter

        logger.debug("Fetching funds from broker for {}", userId);

        // Check if post July 2025 (combined margin mode)
        if (FundsMargin.isAfterCombinedMarginDate()) {
            logger.info("Using combined margin mode (post July 2025)");
            return FundsMargin.fromEquityOnly(
                    userId,
                    "UPSTOX",
                    15507.46, // available
                    1000.00, // used
                    16507.46, // total
                    500.00, // span
                    200.00, // exposure
                    0.00, // payin
                    0.00 // notional
            );
        } else {
            // Legacy mode
            return FundsMargin.fromLegacy(
                    userId,
                    "UPSTOX",
                    10000.00, // equity available
                    5507.46 // commodity available
            );
        }
    }

    /**
     * Get cache stats.
     */
    public int getCacheSize() {
        return fundsCache.size();
    }

    /**
     * Maintenance exception.
     */
    public static class FundsMaintenanceException extends RuntimeException {
        public FundsMaintenanceException(String message) {
            super(message);
        }
    }
}
