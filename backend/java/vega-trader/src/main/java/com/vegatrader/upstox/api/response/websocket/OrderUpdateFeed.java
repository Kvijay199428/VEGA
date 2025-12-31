package com.vegatrader.upstox.api.response.websocket;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO for WebSocket order updates.
 *
 * @since 2.0.0
 */
public class OrderUpdateFeed {

    @SerializedName("type")
    private String type;

    @SerializedName("order_id")
    private String orderId;

    @SerializedName("exchange_order_id")
    private String exchangeOrderId;

    @SerializedName("order_state")
    private String orderState;

    @SerializedName("filled_quantity")
    private Integer filledQuantity;

    @SerializedName("pending_quantity")
    private Integer pendingQuantity;

    @SerializedName("average_price")
    private Double averagePrice;

    @SerializedName("status_message")
    private String statusMessage;

    @SerializedName("timestamp")
    private Long timestamp;

    public OrderUpdateFeed() {
    }

    // Getters/Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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

    public String getOrderState() {
        return orderState;
    }

    public void setOrderState(String orderState) {
        this.orderState = orderState;
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

    public Double getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(Double averagePrice) {
        this.averagePrice = averagePrice;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isComplete() {
        return "COMPLETE".equalsIgnoreCase(orderState) ||
                "EXECUTED".equalsIgnoreCase(orderState);
    }

    public boolean isCancelled() {
        return "CANCELLED".equalsIgnoreCase(orderState) ||
                "REJECTED".equalsIgnoreCase(orderState);
    }

    @Override
    public String toString() {
        return String.format("OrderUpdate{id='%s', state='%s', filled=%d}",
                orderId, orderState, filledQuantity);
    }
}
