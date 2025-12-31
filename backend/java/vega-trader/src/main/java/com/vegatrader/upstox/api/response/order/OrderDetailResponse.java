package com.vegatrader.upstox.api.response.order;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response DTO for detailed order information.
 *
 * @since 2.0.0
 */
public class OrderDetailResponse {

    @SerializedName("order_id")
    private String orderId;

    @SerializedName("exchange_order_id")
    private String exchangeOrderId;

    @SerializedName("instrument_key")
    private String instrumentKey;

    @SerializedName("trading_symbol")
    private String tradingSymbol;

    @SerializedName("product")
    private String product;

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

    @SerializedName("cancelled_quantity")
    private Integer cancelledQuantity;

    @SerializedName("price")
    private Double price;

    @SerializedName("trigger_price")
    private Double triggerPrice;

    @SerializedName("average_price")
    private Double averagePrice;

    @SerializedName("disclosed_quantity")
    private Integer disclosedQuantity;

    @SerializedName("validity")
    private String validity;

    @SerializedName("order_state")
    private String orderState;

    @SerializedName("status_message")
    private String statusMessage;

    @SerializedName("order_timestamp")
    private Long orderTimestamp;

    @SerializedName("exchange_timestamp")
    private Long exchangeTimestamp;

    @SerializedName("placed_by")
    private String placedBy;

    @SerializedName("tag")
    private String tag;

    @SerializedName("is_amo")
    private Boolean isAmo;

    @SerializedName("order_history")
    private List<OrderHistoryEntry> orderHistory;

    public OrderDetailResponse() {
    }

    // Getters/Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getExchangeOrderId() {
        return exchangeOrderId;
    }

    public void setExchangeOrderId(String exchangeOrderId) {
        this.exchangeOrderId = exchangeOrderId;
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

    public Integer getCancelledQuantity() {
        return cancelledQuantity;
    }

    public void setCancelledQuantity(Integer cancelledQuantity) {
        this.cancelledQuantity = cancelledQuantity;
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

    public Double getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(Double averagePrice) {
        this.averagePrice = averagePrice;
    }

    public Integer getDisclosedQuantity() {
        return disclosedQuantity;
    }

    public void setDisclosedQuantity(Integer disclosedQuantity) {
        this.disclosedQuantity = disclosedQuantity;
    }

    public String getValidity() {
        return validity;
    }

    public void setValidity(String validity) {
        this.validity = validity;
    }

    public String getOrderState() {
        return orderState;
    }

    public void setOrderState(String orderState) {
        this.orderState = orderState;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Long getOrderTimestamp() {
        return orderTimestamp;
    }

    public void setOrderTimestamp(Long orderTimestamp) {
        this.orderTimestamp = orderTimestamp;
    }

    public Long getExchangeTimestamp() {
        return exchangeTimestamp;
    }

    public void setExchangeTimestamp(Long exchangeTimestamp) {
        this.exchangeTimestamp = exchangeTimestamp;
    }

    public String getPlacedBy() {
        return placedBy;
    }

    public void setPlacedBy(String placedBy) {
        this.placedBy = placedBy;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Boolean getIsAmo() {
        return isAmo;
    }

    public void setIsAmo(Boolean isAmo) {
        this.isAmo = isAmo;
    }

    public List<OrderHistoryEntry> getOrderHistory() {
        return orderHistory;
    }

    public void setOrderHistory(List<OrderHistoryEntry> orderHistory) {
        this.orderHistory = orderHistory;
    }

    // Helper methods
    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(orderState) || "OPEN".equalsIgnoreCase(orderState);
    }

    public boolean isComplete() {
        return "COMPLETE".equalsIgnoreCase(orderState) || "EXECUTED".equalsIgnoreCase(orderState);
    }

    public boolean isCancelled() {
        return "CANCELLED".equalsIgnoreCase(orderState) || "REJECTED".equalsIgnoreCase(orderState);
    }

    public boolean isPartiallyFilled() {
        return filledQuantity != null && filledQuantity > 0 &&
                pendingQuantity != null && pendingQuantity > 0;
    }

    public Double getFilledValue() {
        if (filledQuantity != null && averagePrice != null) {
            return filledQuantity * averagePrice;
        }
        return null;
    }

    public static class OrderHistoryEntry {
        @SerializedName("timestamp")
        private Long timestamp;

        @SerializedName("state")
        private String state;

        @SerializedName("message")
        private String message;

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return String.format("%s: %s", state, message);
        }
    }

    @Override
    public String toString() {
        return String.format("OrderDetail{id='%s', %s %s %d, filled=%d, state='%s'}",
                orderId, transactionType, tradingSymbol, quantity, filledQuantity, orderState);
    }
}
