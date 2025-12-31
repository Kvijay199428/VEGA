package com.vegatrader.upstox.api.response.order;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO for order placement.
 * <p>
 * This class represents the response received after placing an order
 * through the Upstox API.
 * </p>
 * <p>
 * <b>Usage Example:</b>
 * 
 * <pre>{@code
 * // Received from API
 * {
 *   "order_id": "240101000000001",
 *   "exchange_order_id": "1234567890",
 *   "placed_by": "AB1234",
 *   "order_timestamp": 1704153600,
 *   "order_state": "PENDING",
 *   "status_message": "Order placed successfully"
 * }
 * }</pre>
 * </p>
 *
 * @since 2.0.0
 */
public class OrderResponse {

    @SerializedName("order_id")
    private String orderId;

    @SerializedName("exchange_order_id")
    private String exchangeOrderId;

    @SerializedName("placed_by")
    private String placedBy;

    @SerializedName("order_timestamp")
    private Long orderTimestamp;

    @SerializedName("order_state")
    private String orderState;

    @SerializedName("status_message")
    private String statusMessage;

    @SerializedName("average_price")
    private Double averagePrice;

    @SerializedName("filled_quantity")
    private Integer filledQuantity;

    @SerializedName("pending_quantity")
    private Integer pendingQuantity;

    @SerializedName("cancelled_quantity")
    private Integer cancelledQuantity;

    /**
     * Default constructor.
     */
    public OrderResponse() {
    }

    // Getters and Setters

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

    public String getPlacedBy() {
        return placedBy;
    }

    public void setPlacedBy(String placedBy) {
        this.placedBy = placedBy;
    }

    public Long getOrderTimestamp() {
        return orderTimestamp;
    }

    public void setOrderTimestamp(Long orderTimestamp) {
        this.orderTimestamp = orderTimestamp;
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

    public Double getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(Double averagePrice) {
        this.averagePrice = averagePrice;
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

    /**
     * Returns true if the order is in a pending state.
     *
     * @return true if pending
     */
    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(orderState) ||
                "OPEN".equalsIgnoreCase(orderState);
    }

    /**
     * Returns true if the order is complete.
     *
     * @return true if complete
     */
    public boolean isComplete() {
        return "COMPLETE".equalsIgnoreCase(orderState) ||
                "EXECUTED".equalsIgnoreCase(orderState);
    }

    /**
     * Returns true if the order is cancelled.
     *
     * @return true if cancelled
     */
    public boolean isCancelled() {
        return "CANCELLED".equalsIgnoreCase(orderState) ||
                "REJECTED".equalsIgnoreCase(orderState);
    }

    /**
     * Returns true if the order is partially filled.
     *
     * @return true if partially filled
     */
    public boolean isPartiallyFilled() {
        return filledQuantity != null && filledQuantity > 0 &&
                pendingQuantity != null && pendingQuantity > 0;
    }

    @Override
    public String toString() {
        return String.format("OrderResponse{orderId='%s', state='%s', message='%s'}",
                orderId, orderState, statusMessage);
    }
}
