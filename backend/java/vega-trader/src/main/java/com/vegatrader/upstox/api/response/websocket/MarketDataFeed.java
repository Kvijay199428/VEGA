package com.vegatrader.upstox.api.response.websocket;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO for WebSocket market data feed.
 *
 * @since 2.0.0
 */
public class MarketDataFeed {

    @SerializedName("type")
    private String type;

    @SerializedName("instrument_key")
    private String instrumentKey;

    @SerializedName("ltp")
    private Double lastPrice;

    @SerializedName("ltq")
    private Integer lastTradedQuantity;

    @SerializedName("volume")
    private Long volume;

    @SerializedName("bid_price")
    private Double bidPrice;

    @SerializedName("bid_qty")
    private Integer bidQty;

    @SerializedName("ask_price")
    private Double askPrice;

    @SerializedName("ask_qty")
    private Integer askQty;

    @SerializedName("oi")
    private Long openInterest;

    @SerializedName("prev_close")
    private Double previousClose;

    @SerializedName("timestamp")
    private Long timestamp;

    public MarketDataFeed() {
    }

    // Getters/Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public Double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(Double lastPrice) {
        this.lastPrice = lastPrice;
    }

    public Integer getLastTradedQuantity() {
        return lastTradedQuantity;
    }

    public void setLastTradedQuantity(Integer lastTradedQuantity) {
        this.lastTradedQuantity = lastTradedQuantity;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public Double getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(Double bidPrice) {
        this.bidPrice = bidPrice;
    }

    public Integer getBidQty() {
        return bidQty;
    }

    public void setBidQty(Integer bidQty) {
        this.bidQty = bidQty;
    }

    public Double getAskPrice() {
        return askPrice;
    }

    public void setAskPrice(Double askPrice) {
        this.askPrice = askPrice;
    }

    public Integer getAskQty() {
        return askQty;
    }

    public void setAskQty(Integer askQty) {
        this.askQty = askQty;
    }

    public Long getOpenInterest() {
        return openInterest;
    }

    public void setOpenInterest(Long openInterest) {
        this.openInterest = openInterest;
    }

    public Double getPreviousClose() {
        return previousClose;
    }

    public void setPreviousClose(Double previousClose) {
        this.previousClose = previousClose;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Double getSpread() {
        if (askPrice != null && bidPrice != null) {
            return askPrice - bidPrice;
        }
        return null;
    }

    public Double getChange() {
        if (lastPrice != null && previousClose != null) {
            return lastPrice - previousClose;
        }
        return null;
    }

    public Double getChangePercent() {
        if (lastPrice != null && previousClose != null && previousClose != 0) {
            return ((lastPrice - previousClose) / previousClose) * 100;
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("MarketData{instrument='%s', ltp=%.2f, change=%.2f%%}",
                instrumentKey, lastPrice, getChangePercent());
    }
}
