package com.vegatrader.upstox.api.request.instrument;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO for expired instruments query.
 *
 * @since 2.0.0
 */
public class ExpiredInstrumentRequest {

    @SerializedName("underlying_key")
    private String underlyingKey;

    @SerializedName("instrument_type")
    private String instrumentType;

    @SerializedName("expiry_date")
    private String expiryDate;

    public ExpiredInstrumentRequest() {
    }

    public static ExpiredInstrumentRequestBuilder builder() {
        return new ExpiredInstrumentRequestBuilder();
    }

    // Getters/Setters
    public String getUnderlyingKey() {
        return underlyingKey;
    }

    public void setUnderlyingKey(String underlyingKey) {
        this.underlyingKey = underlyingKey;
    }

    public String getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(String instrumentType) {
        this.instrumentType = instrumentType;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void validate() {
        if (underlyingKey == null || underlyingKey.isEmpty()) {
            throw new IllegalArgumentException("Underlying key is required");
        }
        if (instrumentType == null || instrumentType.isEmpty()) {
            throw new IllegalArgumentException("Instrument type is required");
        }
        if (!"OPTION".equalsIgnoreCase(instrumentType) && !"FUTURE".equalsIgnoreCase(instrumentType)) {
            throw new IllegalArgumentException("Instrument type must be OPTION or FUTURE");
        }
    }

    public static class ExpiredInstrumentRequestBuilder {
        private String underlyingKey, instrumentType, expiryDate;

        public ExpiredInstrumentRequestBuilder underlyingKey(String underlyingKey) {
            this.underlyingKey = underlyingKey;
            return this;
        }

        public ExpiredInstrumentRequestBuilder instrumentType(String instrumentType) {
            this.instrumentType = instrumentType;
            return this;
        }

        public ExpiredInstrumentRequestBuilder option() {
            this.instrumentType = "OPTION";
            return this;
        }

        public ExpiredInstrumentRequestBuilder future() {
            this.instrumentType = "FUTURE";
            return this;
        }

        public ExpiredInstrumentRequestBuilder expiryDate(String expiryDate) {
            this.expiryDate = expiryDate;
            return this;
        }

        public ExpiredInstrumentRequest build() {
            ExpiredInstrumentRequest request = new ExpiredInstrumentRequest();
            request.underlyingKey = this.underlyingKey;
            request.instrumentType = this.instrumentType;
            request.expiryDate = this.expiryDate;
            return request;
        }
    }

    @Override
    public String toString() {
        return String.format("ExpiredInstrument{underlying='%s', type='%s', expiry='%s'}",
                underlyingKey, instrumentType, expiryDate);
    }
}
