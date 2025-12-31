package com.vegatrader.upstox.api.response.websocket;

import com.google.gson.annotations.SerializedName;

/**
 * Holding update DTO for portfolio WebSocket feed.
 * 
 * <p>
 * Represents real-time holding updates from Upstox portfolio stream.
 * 
 * @since 2.0.0
 */
public class HoldingUpdate {

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

    @SerializedName("exchange")
    private String exchange;

    @SerializedName("product")
    private String product;

    public HoldingUpdate() {
    }

    // Getters and Setters

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
     * Checks if the holding is profitable.
     * 
     * @return true if PnL is positive
     */
    public boolean isProfitable() {
        return pnl != null && pnl > 0;
    }

    /**
     * Gets the current market value of the holding.
     * 
     * @return quantity * last price, or null if either is null
     */
    public Double getCurrentValue() {
        if (quantity != null && lastPrice != null) {
            return quantity * lastPrice;
        }
        return null;
    }

    /**
     * Gets the invested value of the holding.
     * 
     * @return quantity * average price, or null if either is null
     */
    public Double getInvestedValue() {
        if (quantity != null && averagePrice != null) {
            return quantity * averagePrice;
        }
        return null;
    }

    /**
     * Gets the PnL percentage.
     * 
     * @return (current value - invested value) / invested value * 100
     */
    public Double getPnlPercent() {
        Double invested = getInvestedValue();
        Double current = getCurrentValue();
        if (invested != null && current != null && invested != 0) {
            return ((current - invested) / invested) * 100;
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("HoldingUpdate{instrument='%s', qty=%d, avgPrice=%.2f, lastPrice=%.2f, pnl=%.2f, ts=%d}",
                instrumentKey, quantity, averagePrice, lastPrice, pnl, timestamp);
    }
}
