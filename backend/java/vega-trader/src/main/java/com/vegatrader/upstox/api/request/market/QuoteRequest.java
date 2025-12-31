package com.vegatrader.upstox.api.request.market;

import java.util.Arrays;
import java.util.List;

/**
 * Request DTO for market quote parameters.
 *
 * @since 2.0.0
 */
public class QuoteRequest {

    private List<String> instrumentKeys;

    public QuoteRequest() {
    }

    public QuoteRequest(List<String> instrumentKeys) {
        this.instrumentKeys = instrumentKeys;
    }

    public QuoteRequest(String... instrumentKeys) {
        this.instrumentKeys = Arrays.asList(instrumentKeys);
    }

    public static QuoteRequestBuilder builder() {
        return new QuoteRequestBuilder();
    }

    public List<String> getInstrumentKeys() {
        return instrumentKeys;
    }

    public void setInstrumentKeys(List<String> instrumentKeys) {
        this.instrumentKeys = instrumentKeys;
    }

    public String getInstrumentKeysParam() {
        return instrumentKeys != null ? String.join(",", instrumentKeys) : "";
    }

    public void validate() {
        if (instrumentKeys == null || instrumentKeys.isEmpty()) {
            throw new IllegalArgumentException("At least one instrument key is required");
        }
        if (instrumentKeys.size() > 500) {
            throw new IllegalArgumentException("Maximum 500 instruments per request");
        }
    }

    public static QuoteRequest of(String... instrumentKeys) {
        return new QuoteRequest(instrumentKeys);
    }

    public static class QuoteRequestBuilder {
        private List<String> instrumentKeys;

        public QuoteRequestBuilder instrumentKeys(List<String> instrumentKeys) {
            this.instrumentKeys = instrumentKeys;
            return this;
        }

        public QuoteRequestBuilder instrumentKeys(String... instrumentKeys) {
            this.instrumentKeys = Arrays.asList(instrumentKeys);
            return this;
        }

        public QuoteRequestBuilder addInstrument(String instrumentKey) {
            if (this.instrumentKeys == null) {
                this.instrumentKeys = new java.util.ArrayList<>();
            }
            this.instrumentKeys.add(instrumentKey);
            return this;
        }

        public QuoteRequest build() {
            return new QuoteRequest(instrumentKeys);
        }
    }

    @Override
    public String toString() {
        return String.format("QuoteRequest{instruments=%d}",
                instrumentKeys != null ? instrumentKeys.size() : 0);
    }
}
