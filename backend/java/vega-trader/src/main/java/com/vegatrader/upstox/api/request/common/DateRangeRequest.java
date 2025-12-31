package com.vegatrader.upstox.api.request.common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Common request DTO for date range filtering.
 *
 * @since 2.0.0
 */
public class DateRangeRequest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String fromDate;
    private String toDate;

    public DateRangeRequest() {
    }

    public DateRangeRequest(String fromDate, String toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public static DateRangeRequestBuilder builder() {
        return new DateRangeRequestBuilder();
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public void validate() {
        if (fromDate == null || fromDate.isEmpty()) {
            throw new IllegalArgumentException("From date is required");
        }
        if (toDate == null || toDate.isEmpty()) {
            throw new IllegalArgumentException("To date is required");
        }

        // Validate format
        try {
            LocalDate from = LocalDate.parse(fromDate, FORMATTER);
            LocalDate to = LocalDate.parse(toDate, FORMATTER);

            if (from.isAfter(to)) {
                throw new IllegalArgumentException("From date cannot be after to date");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Dates must be in YYYY-MM-DD format");
        }
    }

    public long getDaysBetween() {
        try {
            LocalDate from = LocalDate.parse(fromDate, FORMATTER);
            LocalDate to = LocalDate.parse(toDate, FORMATTER);
            return java.time.temporal.ChronoUnit.DAYS.between(from, to);
        } catch (Exception e) {
            return 0;
        }
    }

    public static class DateRangeRequestBuilder {
        private String fromDate;
        private String toDate;

        public DateRangeRequestBuilder fromDate(String fromDate) {
            this.fromDate = fromDate;
            return this;
        }

        public DateRangeRequestBuilder toDate(String toDate) {
            this.toDate = toDate;
            return this;
        }

        public DateRangeRequestBuilder fromDate(LocalDate fromDate) {
            this.fromDate = fromDate.format(FORMATTER);
            return this;
        }

        public DateRangeRequestBuilder toDate(LocalDate toDate) {
            this.toDate = toDate.format(FORMATTER);
            return this;
        }

        /**
         * Convenience: Last 7 days.
         */
        public DateRangeRequestBuilder lastWeek() {
            LocalDate today = LocalDate.now();
            this.toDate = today.format(FORMATTER);
            this.fromDate = today.minusDays(7).format(FORMATTER);
            return this;
        }

        /**
         * Convenience: Last 30 days.
         */
        public DateRangeRequestBuilder lastMonth() {
            LocalDate today = LocalDate.now();
            this.toDate = today.format(FORMATTER);
            this.fromDate = today.minusDays(30).format(FORMATTER);
            return this;
        }

        /**
         * Convenience: Current month.
         */
        public DateRangeRequestBuilder currentMonth() {
            LocalDate today = LocalDate.now();
            this.toDate = today.format(FORMATTER);
            this.fromDate = today.withDayOfMonth(1).format(FORMATTER);
            return this;
        }

        /**
         * Convenience: Today only.
         */
        public DateRangeRequestBuilder today() {
            LocalDate today = LocalDate.now();
            String todayStr = today.format(FORMATTER);
            this.fromDate = todayStr;
            this.toDate = todayStr;
            return this;
        }

        public DateRangeRequest build() {
            return new DateRangeRequest(fromDate, toDate);
        }
    }

    @Override
    public String toString() {
        return String.format("DateRange{from='%s', to='%s', days=%d}",
                fromDate, toDate, getDaysBetween());
    }
}
