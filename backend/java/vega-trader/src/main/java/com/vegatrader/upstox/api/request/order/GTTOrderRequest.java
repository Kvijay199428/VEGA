package com.vegatrader.upstox.api.request.order;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO for Good-Till-Triggered (GTT) orders.
 *
 * @since 2.0.0
 */
public class GTTOrderRequest {

    @SerializedName("instrument_key")
    private String instrumentKey;

    @SerializedName("trigger_price")
    private Double triggerPrice;

    @SerializedName("limit_price")
    private Double limitPrice;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("transaction_type")
    private String transactionType;

    @SerializedName("product")
    private String product;

    @SerializedName("order_type")
    private String orderType;

    public GTTOrderRequest() {
    }

    public static GTTOrderRequestBuilder builder() {
        return new GTTOrderRequestBuilder();
    }

    // Getters/Setters
    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public Double getTriggerPrice() {
        return triggerPrice;
    }

    public void setTriggerPrice(Double triggerPrice) {
        this.triggerPrice = triggerPrice;
    }

    public Double getLimitPrice() {
        return limitPrice;
    }

    public void setLimitPrice(Double limitPrice) {
        this.limitPrice = limitPrice;
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

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public void validate() {
        if (instrumentKey == null || instrumentKey.isEmpty()) {
            throw new IllegalArgumentException("Instrument key is required");
        }
        if (triggerPrice == null || triggerPrice <= 0) {
            throw new IllegalArgumentException("Trigger price must be > 0");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }
    }

    public static class GTTOrderRequestBuilder {
        private String instrumentKey, transactionType, product, orderType;
        private Double triggerPrice, limitPrice;
        private Integer quantity;

        public GTTOrderRequestBuilder instrumentKey(String instrumentKey) {
            this.instrumentKey = instrumentKey;
            return this;
        }

        public GTTOrderRequestBuilder triggerPrice(Double triggerPrice) {
            this.triggerPrice = triggerPrice;
            return this;
        }

        public GTTOrderRequestBuilder limitPrice(Double limitPrice) {
            this.limitPrice = limitPrice;
            return this;
        }

        public GTTOrderRequestBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public GTTOrderRequestBuilder transactionType(String transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        public GTTOrderRequestBuilder product(String product) {
            this.product = product;
            return this;
        }

        public GTTOrderRequestBuilder orderType(String orderType) {
            this.orderType = orderType;
            return this;
        }

        public GTTOrderRequest build() {
            GTTOrderRequest request = new GTTOrderRequest();
            request.instrumentKey = this.instrumentKey;
            request.triggerPrice = this.triggerPrice;
            request.limitPrice = this.limitPrice;
            request.quantity = this.quantity;
            request.transactionType = this.transactionType;
            request.product = this.product;
            request.orderType = this.orderType;
            return request;
        }
    }
}
