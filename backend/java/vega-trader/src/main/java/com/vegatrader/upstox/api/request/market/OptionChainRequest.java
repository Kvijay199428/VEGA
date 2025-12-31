package com.vegatrader.upstox.api.request.market;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO for option chain query parameters.
 *
 * @since 2.0.0
 */
public class OptionChainRequest {

    @SerializedName("instrument_key")
    private String instrumentKey;

    @SerializedName("expiry_date")
    private String expiryDate;

    public OptionChainRequest() {
    }

    public OptionChainRequest(String instrumentKey, String expiryDate) {
        this.instrumentKey = instrumentKey;
        this.expiryDate = expiryDate;
    }

    public static OptionChainRequestBuilder builder() {
        return new OptionChainRequestBuilder();
    }

    // Getters/Setters
    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void validate() {
        if (instrumentKey == null || instrumentKey.isEmpty()) {
            throw new IllegalArgumentException("Instrument key is required");
        }
        if (expiryDate == null || expiryDate.isEmpty()) {
            throw new IllegalArgumentException("Expiry date is required");
        }
        // Validate format: YYYY-MM-DD
        if (!expiryDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("Expiry date must be in YYYY-MM-DD format");
        }
    }

    public static class OptionChainRequestBuilder {
        private String instrumentKey, expiryDate;

        public OptionChainRequestBuilder instrumentKey(String instrumentKey) {
            this.instrumentKey = instrumentKey;
            return this;
        }

        public OptionChainRequestBuilder expiryDate(String expiryDate) {
            this.expiryDate = expiryDate;
            return this;
        }

        /**
         * Convenience: Set Nifty 50 as underlying.
         */
        public OptionChainRequestBuilder nifty50() {
            this.instrumentKey = "NSE_INDEX|Nifty 50";
            return this;
        }

        /**
         * Convenience: Set Bank Nifty as underlying.
         */
        public OptionChainRequestBuilder bankNifty() {
            this.instrumentKey = "NSE_INDEX|Nifty Bank";
            return this;
        }

        /**
         * Convenience: Set Fin Nifty as underlying.
         */
        public OptionChainRequestBuilder finNifty() {
            this.instrumentKey = "NSE_INDEX|Nifty Fin Service";
            return this;
        }

        public OptionChainRequest build() {
            OptionChainRequest request = new OptionChainRequest();
            request.instrumentKey = this.instrumentKey;
            request.expiryDate = this.expiryDate;
            return request;
        }
    }

    @Override
    public String toString() {
        return String.format("OptionChainRequest{instrument='%s', expiry='%s'}",
                instrumentKey, expiryDate);
    }
}
