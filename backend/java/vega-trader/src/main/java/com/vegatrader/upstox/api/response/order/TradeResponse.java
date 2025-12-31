package com.vegatrader.upstox.api.response.order;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO for trade execution details.
 *
 * @since 2.0.0
 */
public class TradeResponse {

    @SerializedName("order_id")
    private String orderId;

    @SerializedName("exchange_trade_id")
    private String exchangeTradeId;

    @SerializedName("traded_quantity")
    private Integer tradedQuantity;

    @SerializedName("traded_price")
    private Double tradedPrice;

    @SerializedName("trade_date")
    private String tradeDate;

    @SerializedName("trade_timestamp")
    private Long tradeTimestamp;

    @SerializedName("instrument_key")
    private String instrumentKey;

    @SerializedName("transaction_type")
    private String transactionType;

    @SerializedName("trading_symbol")
    private String tradingSymbol;

    public TradeResponse() {
    }

    // Getters/Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getExchangeTradeId() {
        return exchangeTradeId;
    }

    public void setExchangeTradeId(String exchangeTradeId) {
        this.exchangeTradeId = exchangeTradeId;
    }

    public Integer getTradedQuantity() {
        return tradedQuantity;
    }

    public void setTradedQuantity(Integer tradedQuantity) {
        this.tradedQuantity = tradedQuantity;
    }

    public Double getTradedPrice() {
        return tradedPrice;
    }

    public void setTradedPrice(Double tradedPrice) {
        this.tradedPrice = tradedPrice;
    }

    public String getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(String tradeDate) {
        this.tradeDate = tradeDate;
    }

    public Long getTradeTimestamp() {
        return tradeTimestamp;
    }

    public void setTradeTimestamp(Long tradeTimestamp) {
        this.tradeTimestamp = tradeTimestamp;
    }

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getTradingSymbol() {
        return tradingSymbol;
    }

    public void setTradingSymbol(String tradingSymbol) {
        this.tradingSymbol = tradingSymbol;
    }

    public Double getTradeValue() {
        if (tradedQuantity != null && tradedPrice != null) {
            return tradedQuantity * tradedPrice;
        }
        return null;
    }

    public boolean isBuy() {
        return "BUY".equalsIgnoreCase(transactionType);
    }

    public boolean isSell() {
        return "SELL".equalsIgnoreCase(transactionType);
    }

    @Override
    public String toString() {
        return String.format("Trade{%s %s %d @ %.2f, value=%.2f}",
                transactionType, tradingSymbol, tradedQuantity, tradedPrice, getTradeValue());
    }
}
