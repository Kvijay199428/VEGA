package com.vegatrader.upstox.api.expired.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Response containing expired instrument data.
 * Used for REST API responses.
 * 
 * @since 4.4.0
 */
public record ExpiredDataResponse(
        String underlyingKey,
        LocalDate expiry,
        String instrumentType,
        String interval,
        int candleCount,
        List<Candle> candles,
        List<ExpiredOptionContract> options,
        List<ExpiredFutureContract> futures,
        long fetchedAt) {

    /**
     * Factory for candles-only response.
     */
    public static ExpiredDataResponse ofCandles(
            String underlyingKey, LocalDate expiry, String interval, List<Candle> candles) {
        return new ExpiredDataResponse(
                underlyingKey, expiry, "both", interval,
                candles.size(), candles, List.of(), List.of(),
                System.currentTimeMillis());
    }

    /**
     * Factory for options contracts response.
     */
    public static ExpiredDataResponse ofOptions(
            String underlyingKey, LocalDate expiry, List<ExpiredOptionContract> options) {
        return new ExpiredDataResponse(
                underlyingKey, expiry, "options", null,
                0, List.of(), options, List.of(),
                System.currentTimeMillis());
    }

    /**
     * Factory for futures contracts response.
     */
    public static ExpiredDataResponse ofFutures(
            String underlyingKey, LocalDate expiry, List<ExpiredFutureContract> futures) {
        return new ExpiredDataResponse(
                underlyingKey, expiry, "futures", null,
                0, List.of(), List.of(), futures,
                System.currentTimeMillis());
    }

    /**
     * Factory for full response.
     */
    public static ExpiredDataResponse full(
            String underlyingKey, LocalDate expiry, String interval,
            List<Candle> candles, List<ExpiredOptionContract> options,
            List<ExpiredFutureContract> futures) {
        return new ExpiredDataResponse(
                underlyingKey, expiry, "both", interval,
                candles.size(), candles, options, futures,
                System.currentTimeMillis());
    }

    /**
     * Check if response has data.
     */
    public boolean hasData() {
        return !candles.isEmpty() || !options.isEmpty() || !futures.isEmpty();
    }
}
