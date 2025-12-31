package com.vegatrader.upstox.auth.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility for calculating token expiry times.
 * Upstox tokens expire at 3:30 AM the next day.
 *
 * @since 2.0.0
 */
public final class TokenExpiryCalculator {

    private TokenExpiryCalculator() {
        // Utility class
    }

    /**
     * Token expiry time (3:30 AM).
     */
    public static final LocalTime EXPIRY_TIME = LocalTime.of(3, 30, 0);

    /**
     * DateTime formatter for validity_at field.
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Calculate the validity timestamp for a token.
     * Tokens expire at 3:30 AM the next day.
     *
     * @return expiry timestamp in milliseconds
     */
    public static long calculateValidityTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = calculateExpiryDateTime(now);

        return java.time.ZoneId.systemDefault()
                .getRules()
                .getOffset(expiry)
                .getTotalSeconds() * 1000L +
                expiry.atZone(java.time.ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli();
    }

    /**
     * Calculate the validity_at string for database storage.
     *
     * @return formatted date-time string "yyyy-MM-dd HH:mm:ss"
     */
    public static String calculateValidityAtString() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = calculateExpiryDateTime(now);

        return expiry.format(FORMATTER);
    }

    /**
     * Calculate expiry DateTime based on current time.
     * - If before 3:30 AM: expires at 3:30 AM today
     * - If after 3:30 AM: expires at 3:30 AM tomorrow
     *
     * @param now current date-time
     * @return expiry date-time
     */
    private static LocalDateTime calculateExpiryDateTime(LocalDateTime now) {
        LocalDate expiryDate = now.toLocalDate();

        // If current time is after 3:30 AM, token expires tomorrow at 3:30 AM
        if (now.toLocalTime().isAfter(EXPIRY_TIME)) {
            expiryDate = expiryDate.plusDays(1);
        }

        return LocalDateTime.of(expiryDate, EXPIRY_TIME);
    }

    /**
     * Calculate seconds until expiry.
     *
     * @return seconds until 3:30 AM
     */
    public static long calculateSecondsUntilExpiry() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = calculateExpiryDateTime(now);

        return java.time.Duration.between(now, expiry).getSeconds();
    }

    /**
     * Check if token is expired based on validity_at string.
     *
     * @param validityAt validity string from database
     * @return true if expired
     */
    public static boolean isExpired(String validityAt) {
        if (validityAt == null || validityAt.isEmpty()) {
            return true;
        }

        try {
            LocalDateTime expiry = LocalDateTime.parse(validityAt, FORMATTER);
            return LocalDateTime.now().isAfter(expiry);
        } catch (Exception e) {
            return true; // Assume expired if parse fails
        }
    }

    /**
     * Get refresh time (2:30 AM - 1 hour before expiry).
     *
     * @return refresh time LocalTime
     */
    public static LocalTime getRefreshTime() {
        return LocalTime.of(2, 30, 0);
    }

    /**
     * Calculate next refresh DateTime.
     *
     * @return next refresh date-time
     */
    public static LocalDateTime calculateNextRefreshTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate refreshDate = now.toLocalDate();

        LocalTime refreshTime = getRefreshTime();

        // If current time is after 2:30 AM, schedule for tomorrow
        if (now.toLocalTime().isAfter(refreshTime)) {
            refreshDate = refreshDate.plusDays(1);
        }

        return LocalDateTime.of(refreshDate, refreshTime);
    }
}
