package com.vegatrader.upstox.auth.selenium.v2;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Token expiry calculator implementing Upstox token validity rules.
 * 
 * Rules:
 * - Tokens expire at 3:30 AM the following day
 * - If generated before 3:30 AM: expires same day at 3:30 AM
 * - If generated after 3:30 AM: expires next day at 3:30 AM
 *
 * @since 2.1.0
 */
public final class TokenExpiryCalculatorV2 {

    /**
     * Token expiry time: 3:30 AM
     */
    public static final LocalTime EXPIRY_TIME = LocalTime.of(3, 30, 0);

    /**
     * Recommended refresh time: 2:30 AM (1 hour before expiry)
     */
    public static final LocalTime REFRESH_TIME = LocalTime.of(2, 30, 0);

    /**
     * Date-time format for database storage
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private TokenExpiryCalculatorV2() {
        // Utility class, no instantiation
    }

    /**
     * Calculate validity timestamp string for database storage.
     * 
     * @return formatted expiry timestamp (e.g., "2025-12-29 03:30:00")
     */
    public static String calculateValidityAtString() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = calculateExpiryDateTime(now);
        return expiry.format(FORMATTER);
    }

    /**
     * Calculate expiry DateTime based on current time.
     * 
     * @param now current date-time
     * @return expiry date-time
     */
    public static LocalDateTime calculateExpiryDateTime(LocalDateTime now) {
        LocalDate expiryDate = now.toLocalDate();

        // If current time is after 3:30 AM, token expires tomorrow
        if (now.toLocalTime().isAfter(EXPIRY_TIME)) {
            expiryDate = expiryDate.plusDays(1);
        }

        return LocalDateTime.of(expiryDate, EXPIRY_TIME);
    }

    /**
     * Calculate seconds until expiry based on given validity_at string.
     * 
     * @param validityAt validity timestamp string from database
     * @return seconds until expiry, or 0 if expired/invalid
     */
    public static long calculateSecondsUntilExpiry(String validityAt) {
        if (validityAt == null || validityAt.isEmpty()) {
            return 0;
        }

        try {
            LocalDateTime expiry = LocalDateTime.parse(validityAt, FORMATTER);
            long seconds = Duration.between(LocalDateTime.now(), expiry).getSeconds();
            return Math.max(0, seconds);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Calculate seconds until expiry from current time.
     * 
     * @return seconds until expiry
     */
    public static long calculateSecondsUntilExpiry() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = calculateExpiryDateTime(now);
        return Duration.between(now, expiry).getSeconds();
    }

    /**
     * Check if token is expired based on validity_at string.
     * 
     * @param validityAt validity timestamp string from database
     * @return true if expired, false if still valid
     */
    public static boolean isExpired(String validityAt) {
        if (validityAt == null || validityAt.isEmpty()) {
            return true; // Assume expired if no validity info
        }

        try {
            LocalDateTime expiry = LocalDateTime.parse(validityAt, FORMATTER);
            return LocalDateTime.now().isAfter(expiry);
        } catch (Exception e) {
            return true; // Assume expired if parse fails
        }
    }

    /**
     * Check if token needs refresh (within 1 hour of expiry).
     * 
     * @param validityAt validity timestamp string from database
     * @return true if token should be refreshed
     */
    public static boolean needsRefresh(String validityAt) {
        long secondsUntilExpiry = calculateSecondsUntilExpiry(validityAt);
        return secondsUntilExpiry > 0 && secondsUntilExpiry < 3600; // 1 hour
    }

    /**
     * Get human-readable time until expiry.
     * 
     * @param validityAt validity timestamp string from database
     * @return formatted string like "7h 23m" or "Expired"
     */
    public static String getTimeUntilExpiryString(String validityAt) {
        long seconds = calculateSecondsUntilExpiry(validityAt);

        if (seconds <= 0) {
            return "Expired";
        }

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }

    /**
     * Get recommended refresh time.
     * 
     * @return recommended refresh time (2:30 AM)
     */
    public static LocalTime getRefreshTime() {
        return REFRESH_TIME;
    }

    /**
     * Get expiry time.
     * 
     * @return expiry time (3:30 AM)
     */
    public static LocalTime getExpiryTime() {
        return EXPIRY_TIME;
    }
}
