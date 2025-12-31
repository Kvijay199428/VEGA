package com.vegatrader.upstox.api.profile.model;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Funds and margin domain model.
 * Per profile/a1.md section 5.2.
 * 
 * CRITICAL NOTE (July 2025):
 * Post-July 19, 2025: All margin logic must use equity values only.
 * Commodity object becomes dummy (zeros).
 * This is enforced in code, not docs.
 * 
 * @since 4.8.0
 */
public record FundsMargin(
        String userId,
        String broker,

        // Equity margin (combined after July 2025)
        double availableMargin,
        double usedMargin,
        double totalMargin,

        // Margin components
        double spanMargin,
        double exposureMargin,
        double payinAmount,
        double notionalCash,

        // Metadata
        boolean combinedMargin, // TRUE after July 2025
        Instant fetchedAt) {

    /**
     * July 2025 API change effective date.
     */
    public static final LocalDate COMBINED_MARGIN_DATE = LocalDate.of(2025, 7, 19);

    /**
     * Factory: Create from equity-only data (post July 2025).
     */
    public static FundsMargin fromEquityOnly(
            String userId,
            String broker,
            double available,
            double used,
            double total,
            double span,
            double exposure,
            double payin,
            double notional) {
        return new FundsMargin(
                userId, broker,
                available, used, total,
                span, exposure, payin, notional,
                true,
                Instant.now());
    }

    /**
     * Factory: Create from legacy response (pre July 2025).
     */
    public static FundsMargin fromLegacy(
            String userId,
            String broker,
            double equityAvailable,
            double commodityAvailable) {
        double total = equityAvailable + commodityAvailable;
        return new FundsMargin(
                userId, broker,
                total, 0, total,
                0, 0, 0, 0,
                false,
                Instant.now());
    }

    /**
     * Check if sufficient margin for an order.
     */
    public boolean hasSufficientMargin(double required) {
        return availableMargin >= required;
    }

    /**
     * Get margin utilization percentage.
     */
    public double getUtilizationPct() {
        if (totalMargin == 0)
            return 0;
        return (usedMargin / totalMargin) * 100;
    }

    /**
     * Check if funds data is stale.
     */
    public boolean isStale(int ttlSeconds) {
        if (fetchedAt == null)
            return true;
        return Instant.now().minusSeconds(ttlSeconds).isAfter(fetchedAt);
    }

    /**
     * Check if using combined margin mode (post July 2025).
     */
    public static boolean isAfterCombinedMarginDate() {
        return !LocalDate.now().isBefore(COMBINED_MARGIN_DATE);
    }
}
