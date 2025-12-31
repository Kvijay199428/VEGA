package com.vegatrader.upstox.api.response.portfolio;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO for positions (intraday/open positions).
 *
 * @since 2.0.0
 */
public class PositionsResponse {

    @SerializedName("instrument_key")
    private String instrumentKey;

    @SerializedName("product")
    private String product;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("buy_quantity")
    private Integer buyQuantity;

    @SerializedName("sell_quantity")
    private Integer sellQuantity;

    @SerializedName("average_price")
    private Double averagePrice;

    @SerializedName("buy_average")
    private Double buyAverage;

    @SerializedName("sell_average")
    private Double sellAverage;

    @SerializedName("last_price")
    private Double lastPrice;

    @SerializedName("pnl")
    private Double pnl;

    @SerializedName("realized_pnl")
    private Double realizedPnl;

    @SerializedName("unrealized_pnl")
    private Double unrealizedPnl;

    @SerializedName("day_buy_quantity")
    private Integer dayBuyQuantity;

    @SerializedName("day_sell_quantity")
    private Integer daySellQuantity;

    @SerializedName("trading_symbol")
    private String tradingSymbol;

    @SerializedName("exchange")
    private String exchange;

    public PositionsResponse() {
    }

    // Getters/Setters
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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

    public Double getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(Double averagePrice) {
        this.averagePrice = averagePrice;
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

    public boolean isLongPosition() {
        return quantity != null && quantity > 0;
    }

    public boolean isShortPosition() {
        return quantity != null && quantity < 0;
    }

    public boolean hasProfit() {
        return pnl != null && pnl > 0;
    }

    @Override
    public String toString() {
        return String.format("Position{%s, %s, qty=%d, pnl=%.2f}",
                tradingSymbol, product, quantity, pnl);
    }
}
