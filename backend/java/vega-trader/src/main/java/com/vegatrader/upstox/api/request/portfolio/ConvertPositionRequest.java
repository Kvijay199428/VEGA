package com.vegatrader.upstox.api.request.portfolio;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO for converting position between product types.
 *
 * @since 2.0.0
 */
public class ConvertPositionRequest {

    @SerializedName("instrument_key")
    private String instrumentKey;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("from_product")
    private String fromProduct;

    @SerializedName("to_product")
    private String toProduct;

    @SerializedName("transaction_type")
    private String transactionType;

    public ConvertPositionRequest() {
    }

    public static ConvertPositionRequestBuilder builder() {
        return new ConvertPositionRequestBuilder();
    }

    // Getters/Setters
    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getFromProduct() {
        return fromProduct;
    }

    public void setFromProduct(String fromProduct) {
        this.fromProduct = fromProduct;
    }

    public String getToProduct() {
        return toProduct;
    }

    public void setToProduct(String toProduct) {
        this.toProduct = toProduct;
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
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }
        if (fromProduct == null || fromProduct.isEmpty()) {
            throw new IllegalArgumentException("From product is required");
        }
        if (toProduct == null || toProduct.isEmpty()) {
            throw new IllegalArgumentException("To product is required");
        }
        if (fromProduct.equals(toProduct)) {
            throw new IllegalArgumentException("From and To products cannot be the same");
        }
    }

    public static class ConvertPositionRequestBuilder {
        private String instrumentKey, fromProduct, toProduct, transactionType;
        private Integer quantity;

        public ConvertPositionRequestBuilder instrumentKey(String instrumentKey) {
            this.instrumentKey = instrumentKey;
            return this;
        }

        public ConvertPositionRequestBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public ConvertPositionRequestBuilder fromProduct(String fromProduct) {
            this.fromProduct = fromProduct;
            return this;
        }

        public ConvertPositionRequestBuilder toProduct(String toProduct) {
            this.toProduct = toProduct;
            return this;
        }

        public ConvertPositionRequestBuilder transactionType(String transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        /**
         * Convenience: Convert MIS to CNC (Intraday to Delivery).
         */
        public ConvertPositionRequestBuilder misToDelivery() {
            this.fromProduct = "MIS";
            this.toProduct = "D";
            return this;
        }

        /**
         * Convenience: Convert CNC to MIS (Delivery to Intraday).
         */
        public ConvertPositionRequestBuilder deliveryToMis() {
            this.fromProduct = "D";
            this.toProduct = "MIS";
            return this;
        }

        public ConvertPositionRequest build() {
            ConvertPositionRequest request = new ConvertPositionRequest();
            request.instrumentKey = this.instrumentKey;
            request.quantity = this.quantity;
            request.fromProduct = this.fromProduct;
            request.toProduct = this.toProduct;
            request.transactionType = this.transactionType;
            return request;
        }
    }
}
