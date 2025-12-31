package com.vegatrader.upstox.api.expired.model;

import java.time.LocalDate;

/**
 * Request parameters for fetching expired instrument data.
 * Used for REST API and CLI.
 * 
 * @since 4.4.0
 */
public record ExpiredDataRequest(
        String underlyingKey,
        LocalDate expiry, // null = fetch all per settings
        String instrumentType, // options, futures, both
        String interval, // 1minute, 5minute, day, etc.
        LocalDate fromDate,
        LocalDate toDate,
        boolean includeWeekly) {

    /**
     * Builder factory for common use cases.
     */
    public static ExpiredDataRequest forUnderlying(String underlyingKey) {
        return new ExpiredDataRequest(
                underlyingKey, null, "both", "day",
                LocalDate.now().minusDays(365), LocalDate.now(), true);
    }

    public static ExpiredDataRequest forUnderlyingWithExpiry(String underlyingKey, LocalDate expiry) {
        return new ExpiredDataRequest(
                underlyingKey, expiry, "both", "day",
                expiry.minusDays(30), expiry, true);
    }

    public static ExpiredDataRequest forOptions(String underlyingKey, LocalDate expiry) {
        return new ExpiredDataRequest(
                underlyingKey, expiry, "options", "day",
                expiry.minusDays(30), expiry, true);
    }

    public static ExpiredDataRequest forFutures(String underlyingKey, LocalDate expiry) {
        return new ExpiredDataRequest(
                underlyingKey, expiry, "futures", "day",
                expiry.minusDays(30), expiry, true);
    }

    /**
     * Validation.
     */
    public boolean isValid() {
        return underlyingKey != null && !underlyingKey.isBlank()
                && interval != null
                && fromDate != null && toDate != null
                && !fromDate.isAfter(toDate);
    }
}
