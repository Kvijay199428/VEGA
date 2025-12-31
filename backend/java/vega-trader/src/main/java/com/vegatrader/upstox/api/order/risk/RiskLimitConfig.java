package com.vegatrader.upstox.api.order.risk;

import java.util.List;

/**
 * Risk Limit Configuration.
 * Defines limits and their classification (SOFT vs HARD).
 * 
 * @since 5.0.0
 */
public record RiskLimitConfig(
        String limitName,
        LimitType limitType, // SOFT or HARD
        String metric, // QUANTITY, VALUE, EXPOSURE, DELTA
        double threshold,
        String action, // WARN, BLOCK, SLICE
        String scope, // USER, INSTRUMENT, GLOBAL
        boolean enabled) {

    public enum LimitType {
        SOFT, // Warn but allow
        HARD // Block execution
    }

    /**
     * Default limits for derivatives trading.
     */
    public static List<RiskLimitConfig> getDefaults() {
        return List.of(
                // Hard limits
                new RiskLimitConfig("MAX_ORDER_VALUE", LimitType.HARD, "VALUE", 10000000, "BLOCK", "USER", true),
                new RiskLimitConfig("MAX_QUANTITY_PER_ORDER", LimitType.HARD, "QUANTITY", 50000, "BLOCK", "USER", true),
                new RiskLimitConfig("MAX_POSITION_PER_INSTRUMENT", LimitType.HARD, "QUANTITY", 100000, "BLOCK",
                        "INSTRUMENT", true),
                new RiskLimitConfig("MAX_EXPOSURE", LimitType.HARD, "EXPOSURE", 20000000, "BLOCK", "USER", true),
                new RiskLimitConfig("BLOCKED_SYMBOL", LimitType.HARD, "SYMBOL", 0, "BLOCK", "INSTRUMENT", true),

                // Soft limits (warnings)
                new RiskLimitConfig("POSITION_CONCENTRATION_WARN", LimitType.SOFT, "QUANTITY", 80000, "WARN",
                        "INSTRUMENT", true),
                new RiskLimitConfig("EXPOSURE_WARN", LimitType.SOFT, "EXPOSURE", 16000000, "WARN", "USER", true),
                new RiskLimitConfig("HIGH_VALUE_ORDER_WARN", LimitType.SOFT, "VALUE", 5000000, "WARN", "USER", true),

                // Exchange freeze limits (triggers slicing)
                new RiskLimitConfig("NSE_FO_FREEZE", LimitType.HARD, "QUANTITY", 1800, "SLICE", "INSTRUMENT", true),
                new RiskLimitConfig("MCX_FREEZE", LimitType.HARD, "QUANTITY", 500, "SLICE", "INSTRUMENT", true));
    }

    /**
     * Check if this is a hard limit.
     */
    public boolean isHard() {
        return limitType == LimitType.HARD;
    }

    /**
     * Check if this is a soft limit.
     */
    public boolean isSoft() {
        return limitType == LimitType.SOFT;
    }

    /**
     * Check if requires slicing.
     */
    public boolean requiresSlicing() {
        return "SLICE".equals(action);
    }
}
