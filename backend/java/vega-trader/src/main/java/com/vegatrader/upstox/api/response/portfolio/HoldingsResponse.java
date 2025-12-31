package com.vegatrader.upstox.api.response.portfolio;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO for holdings (delivery positions).
 *
 * @since 2.0.0
 */
public class HoldingsResponse {

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

    @SerializedName("day_change_percentage")
    private Double dayChangePercentage;

    @SerializedName("trading_symbol")
    private String tradingSymbol;

    @SerializedName("exchange")
    private String exchange;

    @SerializedName("isin")
    private String isin;

    @SerializedName("product")
    private String product;

    public HoldingsResponse() {
    }

    // Getters/Setters
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

    public Double getDayChangePercentage() {
        return dayChangePercentage;
    }

    public void setDayChangePercentage(Double dayChangePercentage) {
        this.dayChangePercentage = dayChangePercentage;
    }

    public String getTradingSymbol() {
        return tradingSymbol;
    }

    public void setTradingSymbol(String tradingSymbol) {
        this.tradingSymbol = tradingSymbol;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Double getCurrentValue() {
        if (quantity != null && lastPrice != null) {
            return quantity * lastPrice;
        }
        return null;
    }

    public Double getInvestedValue() {
        if (quantity != null && averagePrice != null) {
            return quantity * averagePrice;
        }
        return null;
    }

    public boolean isProfit() {
        return pnl != null && pnl > 0;
    }

    @Override
    public String toString() {
        return String.format("Holding{%s, qty=%d, avg=%.2f, ltp=%.2f, pnl=%.2f}",
                tradingSymbol, quantity, averagePrice, lastPrice, pnl);
    }
}
