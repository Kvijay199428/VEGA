package com.vegatrader.upstox.api.optionchain.model;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Option Chain Response DTO.
 * 
 * @since 4.7.0
 */
public record OptionChainResponse(
        String status,
        String instrumentKey,
        LocalDate expiry,
        double spotPrice,
        int strikeCount,
        String fetchSource, // CACHE, API, FALLBACK
        ZonedDateTime fetchedAt,
        List<OptionChainStrike> data) {

    /**
     * Factory for success response.
     */
    public static OptionChainResponse success(
            String instrumentKey,
            LocalDate expiry,
            double spotPrice,
            String fetchSource,
            List<OptionChainStrike> data) {
        return new OptionChainResponse(
                "success",
                instrumentKey,
                expiry,
                spotPrice,
                data.size(),
                fetchSource,
                ZonedDateTime.now(),
                data);
    }

    /**
     * Factory for error response.
     */
    public static OptionChainResponse error(String instrumentKey, LocalDate expiry, String message) {
        return new OptionChainResponse(
                "error: " + message,
                instrumentKey,
                expiry,
                0,
                0,
                "NONE",
                ZonedDateTime.now(),
                List.of());
    }

    /**
     * Check if response has data.
     */
    public boolean hasData() {
        return data != null && !data.isEmpty();
    }

    /**
     * Get ATM strike (closest to spot).
     */
    public OptionChainStrike getATMStrike() {
        if (!hasData())
            return null;

        return data.stream()
                .min((a, b) -> Double.compare(
                        Math.abs(a.strikePrice() - spotPrice),
                        Math.abs(b.strikePrice() - spotPrice)))
                .orElse(null);
    }

    /**
     * Get total call OI.
     */
    public long getTotalCallOI() {
        if (!hasData())
            return 0;
        return data.stream()
                .mapToLong(s -> s.callOptions().marketData().oi())
                .sum();
    }

    /**
     * Get total put OI.
     */
    public long getTotalPutOI() {
        if (!hasData())
            return 0;
        return data.stream()
                .mapToLong(s -> s.putOptions().marketData().oi())
                .sum();
    }

    /**
     * Calculate overall PCR.
     */
    public double getOverallPCR() {
        long callOI = getTotalCallOI();
        if (callOI == 0)
            return 0;
        return (double) getTotalPutOI() / callOI;
    }
}
