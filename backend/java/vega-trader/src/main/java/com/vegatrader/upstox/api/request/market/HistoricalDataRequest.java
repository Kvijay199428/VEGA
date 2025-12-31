package com.vegatrader.upstox.api.request.market;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO for historical candlestick data.
 *
 * @since 2.0.0
 */
public class HistoricalDataRequest {

    @SerializedName("instrument_key")
    private String instrumentKey;

    @SerializedName("interval")
    private String interval;

    @SerializedName("from_date")
    private String fromDate;

    @SerializedName("to_date")
    private String toDate;

    public HistoricalDataRequest() {
    }

    public static HistoricalDataRequestBuilder builder() {
        return new HistoricalDataRequestBuilder();
    }

    // Getters/Setters
    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
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
        if (instrumentKey == null || instrumentKey.isEmpty()) {
            throw new IllegalArgumentException("Instrument key is required");
        }
        if (interval == null || interval.isEmpty()) {
            throw new IllegalArgumentException("Interval is required");
        }
        if (fromDate == null || fromDate.isEmpty()) {
            throw new IllegalArgumentException("From date is required");
        }
        if (toDate == null || toDate.isEmpty()) {
            throw new IllegalArgumentException("To date is required");
        }
    }

    public static class HistoricalDataRequestBuilder {
        private String instrumentKey, interval, fromDate, toDate;

        public HistoricalDataRequestBuilder instrumentKey(String instrumentKey) {
            this.instrumentKey = instrumentKey;
            return this;
        }

        public HistoricalDataRequestBuilder interval(String interval) {
            this.interval = interval;
            return this;
        }

        public HistoricalDataRequestBuilder fromDate(String fromDate) {
            this.fromDate = fromDate;
            return this;
        }

        public HistoricalDataRequestBuilder toDate(String toDate) {
            this.toDate = toDate;
            return this;
        }

        /**
         * Convenience: Set 1-minute interval.
         */
        public HistoricalDataRequestBuilder oneMinute() {
            this.interval = "1minute";
            return this;
        }

        /**
         * Convenience: Set 5-minute interval.
         */
        public HistoricalDataRequestBuilder fiveMinute() {
            this.interval = "5minute";
            return this;
        }

        /**
         * Convenience: Set 15-minute interval.
         */
        public HistoricalDataRequestBuilder fifteenMinute() {
            this.interval = "15minute";
            return this;
        }

        /**
         * Convenience: Set daily interval.
         */
        public HistoricalDataRequestBuilder daily() {
            this.interval = "1day";
            return this;
        }

        public HistoricalDataRequest build() {
            HistoricalDataRequest request = new HistoricalDataRequest();
            request.instrumentKey = this.instrumentKey;
            request.interval = this.interval;
            request.fromDate = this.fromDate;
            request.toDate = this.toDate;
            return request;
        }
    }
}
