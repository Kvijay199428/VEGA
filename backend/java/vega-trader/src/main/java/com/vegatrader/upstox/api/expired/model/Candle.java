package com.vegatrader.upstox.api.expired.model;

import java.time.ZonedDateTime;

/**
 * Historical candle data for expired instruments.
 * 
 * @since 4.4.0
 */
public record Candle(
        ZonedDateTime timestamp,
        double open,
        double high,
        double low,
        double close,
        long volume,
        long openInterest) {

    /**
     * Validate candle data.
     */
    public boolean isValid() {
        return timestamp != null
                && high >= low
                && high >= open
                && high >= close
                && low <= open
                && low <= close
                && volume >= 0;
    }

    /**
     * Calculate typical price (HLC/3).
     */
    public double typicalPrice() {
        return (high + low + close) / 3.0;
    }

    /**
     * Calculate VWAP approximation.
     */
    public double vwap() {
        return volume > 0 ? typicalPrice() : 0;
    }

    /**
     * Check if bullish candle.
     */
    public boolean isBullish() {
        return close > open;
    }

    /**
     * Check if bearish candle.
     */
    public boolean isBearish() {
        return close < open;
    }

    /**
     * Calculate candle body size.
     */
    public double bodySize() {
        return Math.abs(close - open);
    }

    /**
     * Calculate candle range.
     */
    public double range() {
        return high - low;
    }
}
