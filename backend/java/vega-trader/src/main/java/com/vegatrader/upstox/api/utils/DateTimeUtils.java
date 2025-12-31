package com.vegatrader.upstox.api.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date and time conversions.
 *
 * @since 2.0.0
 */
public final class DateTimeUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    private DateTimeUtils() {
        // Utility class - no instantiation
    }

    /**
     * Converts epoch timestamp to LocalDateTime.
     *
     * @param epochSeconds the epoch seconds
     * @return LocalDateTime
     */
    public static LocalDateTime fromEpoch(long epochSeconds) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(epochSeconds),
                IST_ZONE);
    }

    /**
     * Converts epoch millis to LocalDateTime.
     *
     * @param epochMillis the epoch milliseconds
     * @return LocalDateTime
     */
    public static LocalDateTime fromEpochMillis(long epochMillis) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(epochMillis),
                IST_ZONE);
    }

    /**
     * Converts LocalDateTime to epoch seconds.
     *
     * @param dateTime the LocalDateTime
     * @return epoch seconds
     */
    public static long toEpoch(LocalDateTime dateTime) {
        return dateTime.atZone(IST_ZONE).toEpochSecond();
    }

    /**
     * Formats date to YYYY-MM-DD.
     *
     * @param date the date
     * @return formatted string
     */
    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    /**
     * Formats datetime.
     *
     * @param dateTime the datetime
     * @return formatted string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * Parses date from YYYY-MM-DD.
     *
     * @param dateStr the date string
     * @return LocalDate
     */
    public static LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    /**
     * Gets today's date in YYYY-MM-DD format.
     *
     * @return today's date string
     */
    public static String getTodayString() {
        return formatDate(LocalDate.now(IST_ZONE));
    }

    /**
     * Gets current datetime in IST.
     *
     * @return current LocalDateTime
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(IST_ZONE);
    }

    /**
     * Checks if date is a weekday.
     *
     * @param date the date
     * @return true if Monday-Friday
     */
    public static boolean isWeekday(LocalDate date) {
        int dayOfWeek = date.getDayOfWeek().getValue();
        return dayOfWeek >= 1 && dayOfWeek <= 5; // Monday = 1, Friday = 5
    }

    /**
     * Checks if current time is within market hours (9:15 AM - 3:30 PM IST).
     *
     * @return true if within market hours
     */
    public static boolean isMarketHours() {
        LocalDateTime now = now();
        int hour = now.getHour();
        int minute = now.getMinute();

        // Before 9:15 AM
        if (hour < 9 || (hour == 9 && minute < 15)) {
            return false;
        }

        // After 3:30 PM
        if (hour > 15 || (hour == 15 && minute > 30)) {
            return false;
        }

        return true;
    }

    /**
     * Checks if today is a weekend.
     *
     * @return true if Saturday or Sunday
     */
    public static boolean isWeekend() {
        return !isWeekday(LocalDate.now(IST_ZONE));
    }
}
