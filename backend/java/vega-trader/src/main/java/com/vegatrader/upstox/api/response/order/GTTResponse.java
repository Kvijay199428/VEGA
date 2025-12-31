package com.vegatrader.upstox.api.response.order;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response DTO for GTT order.
 *
 * @since 2.0.0
 */
public class GTTResponse {

    @SerializedName("gtt_id")
    private String gttId;

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

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("triggered_orders")
    private List<String> triggeredOrders;

    public GTTResponse() {
    }

    // Getters/Setters
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<String> getTriggeredOrders() {
        return triggeredOrders;
    }

    public void setTriggeredOrders(List<String> triggeredOrders) {
        this.triggeredOrders = triggeredOrders;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    public boolean isTriggered() {
        return "TRIGGERED".equalsIgnoreCase(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return String.format("GTT{id='%s', trigger=%.2f, status='%s'}",
                gttId, triggerPrice, status);
    }
}
