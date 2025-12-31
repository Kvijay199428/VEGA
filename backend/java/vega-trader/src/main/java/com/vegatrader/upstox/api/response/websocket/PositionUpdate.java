package com.vegatrader.upstox.api.response.websocket;

import com.google.gson.annotations.SerializedName;

/**
 * Position update DTO for portfolio WebSocket feed.
 * 
 * <p>
 * Represents real-time position updates from Upstox portfolio stream.
 * 
 * @since 2.0.0
 */
public class PositionUpdate {

    @SerializedName("instrument_key")
    private String instrumentKey;

    @SerializedName("product")
    private String product;

    @SerializedName("buy_quantity")
    private Integer buyQuantity;

    @SerializedName("sell_quantity")
    private Integer sellQuantity;

    @SerializedName("net_quantity")
    private Integer netQuantity;

    @SerializedName("buy_average")
    private Double buyAverage;

    @SerializedName("sell_average")
    private Double sellAverage;

    @SerializedName("realized_pnl")
    private Double realizedPnl;

    @SerializedName("unrealized_pnl")
    private Double unrealizedPnl;

    @SerializedName("last_price")
    private Double lastPrice;

    @SerializedName("timestamp")
    private Long timestamp;

    @SerializedName("exchange")
    private String exchange;

    @SerializedName("day_buy_quantity")
    private Integer dayBuyQuantity;

    @SerializedName("day_sell_quantity")
    private Integer daySellQuantity;

    public PositionUpdate() {
    }

    // Getters and Setters

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Integer getBuyQuantity() {
        return buyQuantity;
    }

    public void setBuyQuantity(Integer buyQuantity) {
        this.buyQuantity = buyQuantity;
    }

    public Integer getSellQuantity() {
        return sellQuantity;
    }

    public void setSellQuantity(Integer sellQuantity) {
        this.sellQuantity = sellQuantity;
    }

    public Integer getNetQuantity() {
        return netQuantity;
    }

    public void setNetQuantity(Integer netQuantity) {
        this.netQuantity = netQuantity;
    }

    public Double getBuyAverage() {
        return buyAverage;
    }

    public void setBuyAverage(Double buyAverage) {
        this.buyAverage = buyAverage;
    }

    public Double getSellAverage() {
        return sellAverage;
    }

    public void setSellAverage(Double sellAverage) {
        this.sellAverage = sellAverage;
    }

    public Double getRealizedPnl() {
        return realizedPnl;
    }

    public void setRealizedPnl(Double realizedPnl) {
        this.realizedPnl = realizedPnl;
    }

    public Double getUnrealizedPnl() {
        return unrealizedPnl;
    }

    public void setUnrealizedPnl(Double unrealizedPnl) {
        this.unrealizedPnl = unrealizedPnl;
    }

    public Double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(Double lastPrice) {
        this.lastPrice = lastPrice;
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

    public Integer getDayBuyQuantity() {
        return dayBuyQuantity;
    }

    public void setDayBuyQuantity(Integer dayBuyQuantity) {
        this.dayBuyQuantity = dayBuyQuantity;
    }

    public Integer getDaySellQuantity() {
        return daySellQuantity;
    }

    public void setDaySellQuantity(Integer daySellQuantity) {
        this.daySellQuantity = daySellQuantity;
    }

    // Helper methods

    /**
     * Checks if the position is long (net buy).
     * 
     * @return true if net quantity is positive
     */
    public boolean isLong() {
        return netQuantity != null && netQuantity > 0;
    }

    /**
     * Checks if the position is short (net sell).
     * 
     * @return true if net quantity is negative
     */
    public boolean isShort() {
        return netQuantity != null && netQuantity < 0;
    }

    /**
     * Checks if the position is flat (no net position).
     * 
     * @return true if net quantity is zero
     */
    public boolean isFlat() {
        return netQuantity != null && netQuantity == 0;
    }

    /**
     * Gets the total PnL (realized + unrealized).
     * 
     * @return sum of realized and unrealized PnL
     */
    public Double getNetPnl() {
        double realized = realizedPnl != null ? realizedPnl : 0.0;
        double unrealized = unrealizedPnl != null ? unrealizedPnl : 0.0;
        return realized + unrealized;
    }

    /**
     * Gets the absolute net quantity.
     * 
     * @return absolute value of net quantity
     */
    public int getAbsoluteNetQuantity() {
        return netQuantity != null ? Math.abs(netQuantity) : 0;
    }

    /**
     * Checks if the position is profitable.
     * 
     * @return true if net PnL is positive
     */
    public boolean isProfitable() {
        Double netPnl = getNetPnl();
        return netPnl != null && netPnl > 0;
    }

    @Override
    public String toString() {
        return String.format(
                "PositionUpdate{instrument='%s', product='%s', netQty=%d, realizedPnl=%.2f, unrealizedPnl=%.2f, ts=%d}",
                instrumentKey, product, netQuantity, realizedPnl, unrealizedPnl, timestamp);
    }
}
