package com.vegatrader.upstox.api.request.order;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO for placing a single order.
 * <p>
 * This class represents all parameters required to place an order through
 * the Upstox API. It supports all order types: MARKET, LIMIT, STOP_MARKET,
 * STOP_LIMIT.
 * </p>
 * <p>
 * <b>Usage Example:</b>
 * 
 * <pre>{@code
 * PlaceOrderRequest request = PlaceOrderRequest.builder()
 *         .quantity(1)
 *         .product("D")
 *         .validity("DAY")
 *         .instrumentKey("NSE_EQ|INE528G01035")
 *         .orderType("MARKET")
 *         .transactionType("BUY")
 *         .price(0.0)
 *         .triggerPrice(0.0)
 *         .disclosedQuantity(0)
 *         .tag("my_order")
 *         .build();
 * }</pre>
 * </p>
 *
 * @since 2.0.0
 */
public class PlaceOrderRequest {

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("product")
    private String product;

    @SerializedName("validity")
    private String validity;

    @SerializedName("price")
    private Double price;

    @SerializedName("tag")
    private String tag;

    @SerializedName("instrument_key")
    private String instrumentKey;

    @SerializedName("order_type")
    private String orderType;

    @SerializedName("transaction_type")
    private String transactionType;

    @SerializedName("disclosed_quantity")
    private Integer disclosedQuantity;

    @SerializedName("trigger_price")
    private Double triggerPrice;

    @SerializedName("is_amo")
    private Boolean isAmo;

    /**
     * Default constructor.
     */
    public PlaceOrderRequest() {
    }

    /**
     * Builder for creating place order requests.
     *
     * @return a new PlaceOrderRequestBuilder
     */
    public static PlaceOrderRequestBuilder builder() {
        return new PlaceOrderRequestBuilder();
    }

    /**
     * Validates the request parameters.
     *
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }
        if (product == null || product.isEmpty()) {
            throw new IllegalArgumentException("Product is required");
        }
        if (validity == null || validity.isEmpty()) {
            throw new IllegalArgumentException("Validity is required");
        }
        if (instrumentKey == null || instrumentKey.isEmpty()) {
            throw new IllegalArgumentException("Instrument key is required");
        }
        if (orderType == null || orderType.isEmpty()) {
            throw new IllegalArgumentException("Order type is required");
        }
        if (transactionType == null || transactionType.isEmpty()) {
            throw new IllegalArgumentException("Transaction type is required");
        }

        // Validate order type specific fields
        if ("LIMIT".equals(orderType) || "STOP_LIMIT".equals(orderType)) {
            if (price == null || price <= 0) {
                throw new IllegalArgumentException("Price is required for " + orderType + " orders");
            }
        }

        if ("STOP_MARKET".equals(orderType) || "STOP_LIMIT".equals(orderType)) {
            if (triggerPrice == null || triggerPrice <= 0) {
                throw new IllegalArgumentException("Trigger price is required for " + orderType + " orders");
            }
        }
    }

    // Getters and Setters

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

    public String getValidity() {
        return validity;
    }

    public void setValidity(String validity) {
        this.validity = validity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Integer getDisclosedQuantity() {
        return disclosedQuantity;
    }

    public void setDisclosedQuantity(Integer disclosedQuantity) {
        this.disclosedQuantity = disclosedQuantity;
    }

    public Double getTriggerPrice() {
        return triggerPrice;
    }

    public void setTriggerPrice(Double triggerPrice) {
        this.triggerPrice = triggerPrice;
    }

    public Boolean getIsAmo() {
        return isAmo;
    }

    public void setIsAmo(Boolean isAmo) {
        this.isAmo = isAmo;
    }

    @Override
    public String toString() {
        return String.format("PlaceOrderRequest{%s %s %d @ %s, instrument='%s'}",
                transactionType, orderType, quantity,
                price != null ? price : "MARKET", instrumentKey);
    }

    /**
     * Builder class for PlaceOrderRequest.
     */
    public static class PlaceOrderRequestBuilder {
        private Integer quantity;
        private String product;
        private String validity;
        private Double price;
        private String tag;
        private String instrumentKey;
        private String orderType;
        private String transactionType;
        private Integer disclosedQuantity;
        private Double triggerPrice;
        private Boolean isAmo;

        public PlaceOrderRequestBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public PlaceOrderRequestBuilder product(String product) {
            this.product = product;
            return this;
        }

        public PlaceOrderRequestBuilder validity(String validity) {
            this.validity = validity;
            return this;
        }

        public PlaceOrderRequestBuilder price(Double price) {
            this.price = price;
            return this;
        }

        public PlaceOrderRequestBuilder tag(String tag) {
            this.tag = tag;
            return this;
        }

        public PlaceOrderRequestBuilder instrumentKey(String instrumentKey) {
            this.instrumentKey = instrumentKey;
            return this;
        }

        public PlaceOrderRequestBuilder orderType(String orderType) {
            this.orderType = orderType;
            return this;
        }

        public PlaceOrderRequestBuilder transactionType(String transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        public PlaceOrderRequestBuilder disclosedQuantity(Integer disclosedQuantity) {
            this.disclosedQuantity = disclosedQuantity;
            return this;
        }

        public PlaceOrderRequestBuilder triggerPrice(Double triggerPrice) {
            this.triggerPrice = triggerPrice;
            return this;
        }

        public PlaceOrderRequestBuilder isAmo(Boolean isAmo) {
            this.isAmo = isAmo;
            return this;
        }

        /**
         * Convenience method for creating a market order.
         */
        public PlaceOrderRequestBuilder asMarketOrder() {
            this.orderType = "MARKET";
            this.price = 0.0;
            this.triggerPrice = 0.0;
            return this;
        }

        /**
         * Convenience method for creating a limit order.
         */
        public PlaceOrderRequestBuilder asLimitOrder(Double price) {
            this.orderType = "LIMIT";
            this.price = price;
            this.triggerPrice = 0.0;
            return this;
        }

        /**
         * Convenience method for creating a stop-loss order.
         */
        public PlaceOrderRequestBuilder asStopLossOrder(Double triggerPrice, Double limitPrice) {
            this.orderType = "STOP_LIMIT";
            this.triggerPrice = triggerPrice;
            this.price = limitPrice;
            return this;
        }

        public PlaceOrderRequest build() {
            PlaceOrderRequest request = new PlaceOrderRequest();
            request.quantity = this.quantity;
            request.product = this.product;
            request.validity = this.validity;
            request.price = this.price;
            request.tag = this.tag;
            request.instrumentKey = this.instrumentKey;
            request.orderType = this.orderType;
            request.transactionType = this.transactionType;
            request.disclosedQuantity = this.disclosedQuantity;
            request.triggerPrice = this.triggerPrice;
            request.isAmo = this.isAmo;
            return request;
        }
    }
}
