package com.vegatrader.upstox.api.response.websocket;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO for WebSocket portfolio updates.
 * 
 * <p>
 * Enhanced to support multiple update types (orders, holdings, positions, GTT).
 *
 * @since 2.0.0
 */
public class PortfolioUpdateFeed {

    /**
     * Portfolio update type enum.
     */
    public enum UpdateType {
        ORDER,
        HOLDING,
        POSITION,
        GTT,
        UNKNOWN
    }

    @SerializedName("type")
    private String type;

    @SerializedName("instrument_key")
    private String instrumentKey;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("average_price")
    private Double averagePrice;

    @SerializedName("last_price")
    private Double lastPrice;

    @SerializedName("pnl")
    private Double pnl;

    @SerializedName("day_change")
    private Double dayChange;

    @SerializedName("day_change_percent")
    private Double dayChangePercent;

    @SerializedName("timestamp")
    private Long timestamp;

    // Store raw JSON for type-specific extraction
    private transient Object rawData;

    public PortfolioUpdateFeed() {
    }

    // Getters/Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the update type as enum.
     * 
     * @return UpdateType enum value
     */
    public UpdateType getUpdateType() {
        if (type == null) {
            return UpdateType.UNKNOWN;
        }
        switch (type.toLowerCase()) {
            case "order":
                return UpdateType.ORDER;
            case "holding":
                return UpdateType.HOLDING;
            case "position":
                return UpdateType.POSITION;
            case "gtt":
                return UpdateType.GTT;
            default:
                return UpdateType.UNKNOWN;
        }
    }

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

    public Double getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(Double averagePrice) {
        this.averagePrice = averagePrice;
    }

    public Double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(Double lastPrice) {
        this.lastPrice = lastPrice;
    }

    public Double getPnl() {
        return pnl;
    }

    public void setPnl(Double pnl) {
        this.pnl = pnl;
    }

    public Double getDayChange() {
        return dayChange;
    }

    public void setDayChange(Double dayChange) {
        this.dayChange = dayChange;
    }

    public Double getDayChangePercent() {
        return dayChangePercent;
    }

    public void setDayChangePercent(Double dayChangePercent) {
        this.dayChangePercent = dayChangePercent;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Object getRawData() {
        return rawData;
    }

    public void setRawData(Object rawData) {
        this.rawData = rawData;
    }

    // Helper methods

    /**
     * Checks if this is an order update.
     * 
     * @return true if update type is ORDER
     */
    public boolean isOrderUpdate() {
        return getUpdateType() == UpdateType.ORDER;
    }

    /**
     * Checks if this is a holding update.
     * 
     * @return true if update type is HOLDING
     */
    public boolean isHoldingUpdate() {
        return getUpdateType() == UpdateType.HOLDING;
    }

    /**
     * Checks if this is a position update.
     * 
     * @return true if update type is POSITION
     */
    public boolean isPositionUpdate() {
        return getUpdateType() == UpdateType.POSITION;
    }

    /**
     * Checks if this is a GTT update.
     * 
     * @return true if update type is GTT
     */
    public boolean isGttUpdate() {
        return getUpdateType() == UpdateType.GTT;
    }

    public boolean isProfitable() {
        return pnl != null && pnl > 0;
    }

    public Double getCurrentValue() {
        if (quantity != null && lastPrice != null) {
            return quantity * lastPrice;
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("PortfolioUpdate{type=%s, instrument='%s', qty=%d, pnl=%.2f}",
                type, instrumentKey, quantity, pnl);
    }
}
