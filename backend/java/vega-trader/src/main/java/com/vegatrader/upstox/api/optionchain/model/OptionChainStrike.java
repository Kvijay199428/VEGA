package com.vegatrader.upstox.api.optionchain.model;

import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * Option Chain Strike Data DTO.
 * Per optionchain/implementations/a1.md.
 * 
 * @since 4.7.0
 */
public record OptionChainStrike(
        String instrumentKey,
        LocalDate expiry,
        double strikePrice,
        double underlyingSpotPrice,
        double pcr,
        OptionData callOptions,
        OptionData putOptions,
        ZonedDateTime fetchedAt) {

    /**
     * Option data for call or put.
     */
    public record OptionData(
            String instrumentKey,
            MarketData marketData,
            OptionGreeks greeks,
            com.vegatrader.analytics.valuation.ValuationResult valuation) {
    }

    /**
     * Market data fields.
     */
    public record MarketData(
            double ltp,
            double closePrice,
            long volume,
            long oi,
            double bidPrice,
            int bidQty,
            double askPrice,
            int askQty,
            long prevOi) {
    }

    /**
     * Option Greeks.
     */
    public record OptionGreeks(
            double delta,
            double gamma,
            double theta,
            double vega,
            double iv,
            double pop) {
    }

    /**
     * Check if strike is ITM for calls.
     */
    public boolean isCallITM() {
        return strikePrice < underlyingSpotPrice;
    }

    /**
     * Check if strike is ITM for puts.
     */
    public boolean isPutITM() {
        return strikePrice > underlyingSpotPrice;
    }

    /**
     * Check if ATM (within 1% of spot).
     */
    public boolean isATM() {
        double diff = Math.abs(strikePrice - underlyingSpotPrice);
        return diff / underlyingSpotPrice < 0.01;
    }
}
