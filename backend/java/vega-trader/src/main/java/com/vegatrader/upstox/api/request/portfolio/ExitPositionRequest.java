package com.vegatrader.upstox.api.request.portfolio;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO for exiting/squaring off a position.
 *
 * @since 2.0.0
 */
public class ExitPositionRequest {

    @SerializedName("instrument_key")
    private String instrumentKey;

    @SerializedName("product")
    private String product;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("transaction_type")
    private String transactionType;

    public ExitPositionRequest() {
    }

    public static ExitPositionRequestBuilder builder() {
        return new ExitPositionRequestBuilder();
    }

    // Getters/Setters
    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public void validate() {
        if (instrumentKey == null || instrumentKey.isEmpty()) {
            throw new IllegalArgumentException("Instrument key is required");
        }
        if (product == null || product.isEmpty()) {
            throw new IllegalArgumentException("Product is required");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }
        if (transactionType == null || transactionType.isEmpty()) {
            throw new IllegalArgumentException("Transaction type is required");
        }
    }

    public static class ExitPositionRequestBuilder {
        private String instrumentKey, product, transactionType;
        private Integer quantity;

        public ExitPositionRequestBuilder instrumentKey(String instrumentKey) {
            this.instrumentKey = instrumentKey;
            return this;
        }

        public ExitPositionRequestBuilder product(String product) {
            this.product = product;
            return this;
        }

        public ExitPositionRequestBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public ExitPositionRequestBuilder transactionType(String transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        /**
         * Convenience: Exit long position (SELL).
         */
        public ExitPositionRequestBuilder exitLong() {
            this.transactionType = "SELL";
            return this;
        }

        /**
         * Convenience: Exit short position (BUY).
         */
        public ExitPositionRequestBuilder exitShort() {
            this.transactionType = "BUY";
            return this;
        }

        public ExitPositionRequest build() {
            ExitPositionRequest request = new ExitPositionRequest();
            request.instrumentKey = this.instrumentKey;
            request.product = this.product;
            request.quantity = this.quantity;
            request.transactionType = this.transactionType;
            return request;
        }
    }
}
