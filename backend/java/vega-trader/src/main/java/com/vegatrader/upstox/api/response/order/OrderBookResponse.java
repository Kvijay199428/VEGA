package com.vegatrader.upstox.api.response.order;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response DTO for order book (all pending orders).
 *
 * @since 2.0.0
 */
public class OrderBookResponse {

    @SerializedName("orders")
    private List<OrderDetail> orders;

    public OrderBookResponse() {
    }

    public List<OrderDetail> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderDetail> orders) {
        this.orders = orders;
    }

    public int getOrderCount() {
        return orders != null ? orders.size() : 0;
    }

    public long getPendingCount() {
        if (orders == null)
            return 0;
        return orders.stream()
                .filter(o -> "PENDING".equalsIgnoreCase(o.getOrderState()) ||
                        "OPEN".equalsIgnoreCase(o.getOrderState()))
                .count();
    }

    public static class OrderDetail {
        @SerializedName("order_id")
        private String orderId;

        @SerializedName("instrument_key")
        private String instrumentKey;

        @SerializedName("trading_symbol")
        private String tradingSymbol;

        @SerializedName("order_type")
        private String orderType;

        @SerializedName("transaction_type")
        private String transactionType;

        @SerializedName("quantity")
        private Integer quantity;

        @SerializedName("filled_quantity")
        private Integer filledQuantity;

        @SerializedName("pending_quantity")
        private Integer pendingQuantity;

        @SerializedName("price")
        private Double price;

        @SerializedName("trigger_price")
        private Double triggerPrice;

        @SerializedName("order_state")
        private String orderState;

        @SerializedName("order_timestamp")
        private Long orderTimestamp;

        @SerializedName("product")
        private String product;

        @SerializedName("validity")
        private String validity;

        @SerializedName("tag")
        private String tag;

        // Getters/Setters
        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getInstrumentKey() {
            return instrumentKey;
        }

        public void setInstrumentKey(String instrumentKey) {
            this.instrumentKey = instrumentKey;
        }

        public String getTradingSymbol() {
            return tradingSymbol;
        }

        public void setTradingSymbol(String tradingSymbol) {
            this.tradingSymbol = tradingSymbol;
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

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public Integer getFilledQuantity() {
            return filledQuantity;
        }

        public void setFilledQuantity(Integer filledQuantity) {
            this.filledQuantity = filledQuantity;
        }

        public Integer getPendingQuantity() {
            return pendingQuantity;
        }

        public void setPendingQuantity(Integer pendingQuantity) {
            this.pendingQuantity = pendingQuantity;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public Double getTriggerPrice() {
            return triggerPrice;
        }

        public void setTriggerPrice(Double triggerPrice) {
            this.triggerPrice = triggerPrice;
        }

        public String getOrderState() {
            return orderState;
        }

        public void setOrderState(String orderState) {
            this.orderState = orderState;
        }

        public Long getOrderTimestamp() {
            return orderTimestamp;
        }

        public void setOrderTimestamp(Long orderTimestamp) {
            this.orderTimestamp = orderTimestamp;
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

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public boolean isPending() {
            return "PENDING".equalsIgnoreCase(orderState) || "OPEN".equalsIgnoreCase(orderState);
        }

        public boolean isFilled() {
            return "COMPLETE".equalsIgnoreCase(orderState) || "EXECUTED".equalsIgnoreCase(orderState);
        }

        public boolean isPartiallyFilled() {
            return filledQuantity != null && filledQuantity > 0 &&
                    pendingQuantity != null && pendingQuantity > 0;
        }

        @Override
        public String toString() {
            return String.format("Order{id='%s', %s %s %d @ %.2f, state='%s'}",
                    orderId, transactionType, tradingSymbol, quantity, price, orderState);
        }
    }
}
