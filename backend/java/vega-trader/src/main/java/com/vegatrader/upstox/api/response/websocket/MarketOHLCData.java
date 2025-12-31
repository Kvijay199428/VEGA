package com.vegatrader.upstox.api.response.websocket;

import com.google.gson.annotations.SerializedName;

/**
 * Market OHLC (Open, High, Low, Close) data for Market Data Feed V3.
 * 
 * <p>
 * Contains price and volume information for the trading session.
 * 
 * @since 3.0.0
 */
public class MarketOHLCData {

    /**
     * Open price.
     */
    @SerializedName("open")
    private Double open;

    /**
     * High price.
     */
    @SerializedName("high")
    private Double high;

    /**
     * Low price.
     */
    @SerializedName("low")
    private Double low;

    /**
     * Close price.
     */
    @SerializedName("close")
    private Double close;

    /**
     * Trading volume.
     */
    @SerializedName("volume")
    private Long volume;

    /**
     * Open Interest (for derivatives).
     */
    @SerializedName("oi")
    private Long oi;

    public MarketOHLCData() {
    }

    // Getters and Setters

    public Double getOpen() {
        return open;
    }

    public void setOpen(Double open) {
        this.open = open;
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        this.high = high;
    }

    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        this.low = low;
    }

    public Double getClose() {
        return close;
    }

    public void setClose(Double close) {
        this.close = close;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public Long getOi() {
        return oi;
    }

    public void setOi(Long oi) {
        this.oi = oi;
    }

    /**
     * Calculates the price range (high - low).
     * 
     * @return the price range, or null if high or low is not available
     */
    public Double getRange() {
        if (high != null && low != null) {
            return high - low;
        }
        return null;
    }

    /**
     * Calculates the change from open to close.
     * 
     * @return the change value, or null if open or close is not available
     */
    public Double getChange() {
        if (close != null && open != null) {
            return close - open;
        }
        return null;
    }

    /**
     * Calculates the percentage change from open to close.
     * 
     * @return the percentage change, or null if open or close is not available
     */
    public Double getChangePercent() {
        if (close != null && open != null && open != 0) {
            return ((close - open) / open) * 100;
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("OHLC{open=%.2f, high=%.2f, low=%.2f, close=%.2f, vol=%d, oi=%d}",
                open, high, low, close, volume, oi);
    }
}
