package com.vegatrader.upstox.api.response.websocket;

import com.google.gson.annotations.SerializedName;

/**
 * GTT (Good Till Triggered) order update DTO for portfolio WebSocket feed.
 * 
 * <p>
 * Represents real-time GTT order updates from Upstox portfolio stream.
 * 
 * @since 2.0.0
 */
public class GttUpdate {

    @SerializedName("gtt_id")
    private String gttId;

    @SerializedName("instrument_key")
    private String instrumentKey;

    @SerializedName("condition")
    private String condition;

    @SerializedName("order_type")
    private String orderType;

    @SerializedName("transaction_type")
    private String transactionType;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("price")
    private Double price;

    @SerializedName("trigger_price")
    private Double triggerPrice;

    @SerializedName("status")
    private String status;

    @SerializedName("timestamp")
    private Long timestamp;

    @SerializedName("created_at")
    private Long createdAt;

    @SerializedName("expires_at")
    private Long expiresAt;

    @SerializedName("exchange")
    private String exchange;

    @SerializedName("product")
    private String product;

    public GttUpdate() {
    }

    // Getters and Setters

    public String getGttId() {
        return gttId;
    }

    public void setGttId(String gttId) {
        this.gttId = gttId;
    }

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
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

    // Helper methods

    /**
     * Checks if the GTT order is active.
     * 
     * @return true if status is "active"
     */
    public boolean isActive() {
        return "active".equalsIgnoreCase(status);
    }

    /**
     * Checks if the GTT order has been triggered.
     * 
     * @return true if status is "triggered"
     */
    public boolean isTriggered() {
        return "triggered".equalsIgnoreCase(status);
    }

    /**
     * Checks if the GTT order has expired.
     * 
     * @return true if status is "expired" or current time > expiresAt
     */
    public boolean isExpired() {
        if ("expired".equalsIgnoreCase(status)) {
            return true;
        }
        if (expiresAt != null) {
            return System.currentTimeMillis() > expiresAt;
        }
        return false;
    }

    /**
     * Checks if the GTT order is cancelled.
     * 
     * @return true if status is "cancelled"
     */
    public boolean isCancelled() {
        return "cancelled".equalsIgnoreCase(status);
    }

    /**
     * Checks if the GTT is a buy order.
     * 
     * @return true if transaction type is "BUY"
     */
    public boolean isBuy() {
        return "BUY".equalsIgnoreCase(transactionType);
    }

    /**
     * Checks if the GTT is a sell order.
     * 
     * @return true if transaction type is "SELL"
     */
    public boolean isSell() {
        return "SELL".equalsIgnoreCase(transactionType);
    }

    @Override
    public String toString() {
        return String.format("GttUpdate{gttId='%s', instrument='%s', status='%s', type='%s', triggerPrice=%.2f, ts=%d}",
                gttId, instrumentKey, status, transactionType, triggerPrice, timestamp);
    }
}
