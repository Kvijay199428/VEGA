package com.vegatrader.upstox.api.response.websocket;

import com.google.gson.annotations.SerializedName;

/**
 * Order update DTO for portfolio WebSocket feed.
 * 
 * <p>
 * Represents real-time order updates from Upstox portfolio stream.
 * 
 * @since 2.0.0
 */
public class OrderUpdate {

    @SerializedName("order_id")
    private String orderId;

    @SerializedName("instrument_key")
    private String instrumentKey;

    @SerializedName("status")
    private String status;

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

    @SerializedName("average_price")
    private Double averagePrice;

    @SerializedName("exchange")
    private String exchange;

    @SerializedName("product")
    private String product;

    @SerializedName("validity")
    private String validity;

    @SerializedName("disclosed_quantity")
    private Integer disclosedQuantity;

    @SerializedName("timestamp")
    private Long timestamp;

    @SerializedName("exchange_order_id")
    private String exchangeOrderId;

    @SerializedName("rejection_reason")
    private String rejectionReason;

    public OrderUpdate() {
    }

    // Getters and Setters

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Double getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(Double averagePrice) {
        this.averagePrice = averagePrice;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
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

    public Integer getDisclosedQuantity() {
        return disclosedQuantity;
    }

    public void setDisclosedQuantity(Integer disclosedQuantity) {
        this.disclosedQuantity = disclosedQuantity;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getExchangeOrderId() {
        return exchangeOrderId;
    }

    public void setExchangeOrderId(String exchangeOrderId) {
        this.exchangeOrderId = exchangeOrderId;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    // Helper methods

    /**
     * Checks if the order is fully executed.
     * 
     * @return true if status is "complete" or "filled"
     */
    public boolean isExecuted() {
        return "complete".equalsIgnoreCase(status) || "filled".equalsIgnoreCase(status);
    }

    /**
     * Checks if the order is pending.
     * 
     * @return true if status is "pending", "open", or "trigger pending"
     */
    public boolean isPending() {
        if (status == null) {
            return false;
        }
        String lower = status.toLowerCase();
        return lower.contains("pending") || "open".equals(lower);
    }

    /**
     * Checks if the order is cancelled.
     * 
     * @return true if status is "cancelled"
     */
    public boolean isCancelled() {
        return "cancelled".equalsIgnoreCase(status);
    }

    /**
     * Checks if the order is rejected.
     * 
     * @return true if status is "rejected"
     */
    public boolean isRejected() {
        return "rejected".equalsIgnoreCase(status);
    }

    /**
     * Checks if the order is a buy order.
     * 
     * @return true if transaction type is "BUY"
     */
    public boolean isBuy() {
        return "BUY".equalsIgnoreCase(transactionType);
    }

    /**
     * Checks if the order is a sell order.
     * 
     * @return true if transaction type is "SELL"
     */
    public boolean isSell() {
        return "SELL".equalsIgnoreCase(transactionType);
    }

    /**
     * Gets the unfilled quantity.
     * 
     * @return pending quantity, or 0 if null
     */
    public int getUnfilledQuantity() {
        return pendingQuantity != null ? pendingQuantity : 0;
    }

    @Override
    public String toString() {
        return String.format(
                "OrderUpdate{orderId='%s', instrument='%s', status='%s', type='%s', qty=%d, filled=%d, price=%.2f, ts=%d}",
                orderId, instrumentKey, status, transactionType, quantity, filledQuantity, price, timestamp);
    }
}
