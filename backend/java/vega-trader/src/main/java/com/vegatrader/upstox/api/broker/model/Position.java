package com.vegatrader.upstox.api.broker.model;

/**
 * Trading position model.
 * 
 * @since 4.2.0
 */
public record Position(
        String instrumentKey,
        String tradingSymbol,
        String exchange,
        String product,
        int quantity,
        int dayBuyQty,
        int daySellQty,
        double buyPrice,
        double sellPrice,
        double pnl,
        double unrealizedPnl,
        double realizedPnl,
        double ltp) {

    public boolean isLong() {
        return quantity > 0;
    }

    public boolean isShort() {
        return quantity < 0;
    }

    public boolean isFlat() {
        return quantity == 0;
    }

    public double netValue() {
        return Math.abs(quantity) * ltp;
    }
}
