package com.vegatrader.upstox.api.broker.model;

/**
 * Holdings model.
 * 
 * @since 4.2.0
 */
public record Holding(
        String instrumentKey,
        String tradingSymbol,
        String exchange,
        String isin,
        int quantity,
        int collateralQty,
        double averagePrice,
        double ltp,
        double pnl,
        double pnlPct,
        double dayChange,
        double dayChangePct) {

    public double currentValue() {
        return quantity * ltp;
    }

    public double investedValue() {
        return quantity * averagePrice;
    }
}
