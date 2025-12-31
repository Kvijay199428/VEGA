package com.vegatrader.upstox.api.broker.model;

/**
 * Order request model.
 * 
 * @since 4.2.0
 */
public record OrderRequest(
        String instrumentKey,
        String brokerSymbol,
        String product, // CNC, MIS, MTF
        String orderType, // MARKET, LIMIT, SL, SL-M
        String transactionType, // BUY, SELL
        int qty,
        double price,
        double triggerPrice,
        String validity, // DAY, IOC
        int disclosedQty) {

    /**
     * Creates a market buy order.
     */
    public static OrderRequest marketBuy(String instrumentKey, String brokerSymbol, String product, int qty) {
        return new OrderRequest(instrumentKey, brokerSymbol, product, "MARKET", "BUY", qty, 0, 0, "DAY", 0);
    }

    /**
     * Creates a market sell order.
     */
    public static OrderRequest marketSell(String instrumentKey, String brokerSymbol, String product, int qty) {
        return new OrderRequest(instrumentKey, brokerSymbol, product, "MARKET", "SELL", qty, 0, 0, "DAY", 0);
    }

    /**
     * Creates a limit order.
     */
    public static OrderRequest limit(String instrumentKey, String brokerSymbol, String product,
            String side, int qty, double price) {
        return new OrderRequest(instrumentKey, brokerSymbol, product, "LIMIT", side, qty, price, 0, "DAY", 0);
    }

    /**
     * Check if buy order.
     */
    public boolean isBuy() {
        return "BUY".equalsIgnoreCase(transactionType);
    }

    /**
     * Order value.
     */
    public double orderValue() {
        return qty * price;
    }
}
