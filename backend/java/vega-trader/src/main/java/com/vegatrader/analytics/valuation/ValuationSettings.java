package com.vegatrader.analytics.valuation;

/**
 * Immutable settings for the valuation engine.
 * Can be sourced from Admin Defaults or User Overrides.
 */
public record ValuationSettings(
        double riskFreeRate,
        double defaultVolatility,
        double maxAllowedIV,
        double overvaluedThresholdPct,
        double undervaluedThresholdPct,
        double confidenceSpreadThreshold,
        int lowVolumeThreshold,
        int lowOiThreshold,
        boolean enableValuation,
        boolean enableBlinking,
        boolean enableTooltip,
        boolean enableLogging) {
    /**
     * Default safe settings.
     */
    public static ValuationSettings defaults() {
        return new ValuationSettings(
                0.10, // 10% risk free rate
                0.20, // 20% default vol
                2.00, // 200% max IV
                5.0, // 5% overvalued threshold
                5.0, // 5% undervalued threshold
                0.02, // 2% spread threshold
                500, // low volume
                1000, // low OI
                true, // enable valuation
                true, // enable blinking
                true, // enable tooltip
                false // disable heavy logging
        );
    }
}
