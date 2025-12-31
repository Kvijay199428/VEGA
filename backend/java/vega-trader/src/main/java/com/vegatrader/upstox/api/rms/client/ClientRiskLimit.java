package com.vegatrader.upstox.api.rms.client;

/**
 * Client risk limit configuration.
 * Immutable record representing per-client exposure and loss limits.
 * 
 * @since 4.1.0
 */
public record ClientRiskLimit(
        String clientId,
        double maxGrossExposure,
        double maxNetExposure,
        double maxOrderValue,
        double maxIntradayTurnover,
        int maxOpenPositions,
        double maxIntradayLoss,
        boolean tradingEnabled) {

    /**
     * Creates default risk limits.
     */
    public static ClientRiskLimit defaultLimits(String clientId) {
        return new ClientRiskLimit(
                clientId,
                10_000_000.0, // 1 Cr gross
                5_000_000.0, // 50 Lakh net
                500_000.0, // 5 Lakh per order
                50_000_000.0, // 5 Cr turnover
                100, // 100 positions
                100_000.0, // 1 Lakh max loss
                true);
    }

    /**
     * Checks if client is disabled.
     */
    public boolean isDisabled() {
        return !tradingEnabled;
    }
}
