package com.vegatrader.upstox.api.request.charges;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO for calculating order charges.
 *
 * @since 2.0.0
 */
public class ChargesRequest {

    @SerializedName("instrument_key")
    private String instrumentKey;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("product")
    private String product;

    @SerializedName("transaction_type")
    private String transactionType;

    @SerializedName("price")
    private Double price;

    public ChargesRequest() {
    }

    public static ChargesRequestBuilder builder() {
        return new ChargesRequestBuilder();
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

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void validate() {
        if (instrumentKey == null || instrumentKey.isEmpty()) {
            throw new IllegalArgumentException("Instrument key is required");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }
        if (price == null || price <= 0) {
            throw new IllegalArgumentException("Price must be > 0");
        }
    }

    public static class ChargesRequestBuilder {
        private String instrumentKey, product, transactionType;
        private Integer quantity;
        private Double price;

        public ChargesRequestBuilder instrumentKey(String instrumentKey) {
            this.instrumentKey = instrumentKey;
            return this;
        }

        public ChargesRequestBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public ChargesRequestBuilder product(String product) {
            this.product = product;
            return this;
        }

        public ChargesRequestBuilder transactionType(String transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        public ChargesRequestBuilder price(Double price) {
            this.price = price;
            return this;
        }

        public ChargesRequest build() {
            ChargesRequest request = new ChargesRequest();
            request.instrumentKey = this.instrumentKey;
            request.quantity = this.quantity;
            request.product = this.product;
            request.transactionType = this.transactionType;
            request.price = this.price;
            return request;
        }
    }
}
