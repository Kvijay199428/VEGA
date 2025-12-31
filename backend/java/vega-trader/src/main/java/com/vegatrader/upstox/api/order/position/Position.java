package com.vegatrader.upstox.api.order.position;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Position record representing a single instrument position.
 * Aggregated across all orders and trades for a user.
 * 
 * @since 5.0.0
 */
public record Position(
        String userId,
        String instrumentToken,
        String symbol,
        String exchange,
        String segment, // EQ, FO, CD
        String product, // INTRADAY, NRML, CNC
        int quantity, // Net quantity (positive = long, negative = short)
        int buyQuantity,
        int sellQuantity,
        BigDecimal averageBuyPrice,
        BigDecimal averageSellPrice,
        BigDecimal lastPrice,
        BigDecimal realizedPnl,
        BigDecimal unrealizedPnl,
        BigDecimal dayChange,
        BigDecimal dayChangePercent,
        Instant lastUpdated) {

    /**
     * Calculate net value of position.
     */
    public BigDecimal getNetValue() {
        return lastPrice.multiply(BigDecimal.valueOf(Math.abs(quantity)));
    }

    /**
     * Check if position is long.
     */
    public boolean isLong() {
        return quantity > 0;
    }

    /**
     * Check if position is short.
     */
    public boolean isShort() {
        return quantity < 0;
    }

    /**
     * Check if position is closed.
     */
    public boolean isClosed() {
        return quantity == 0;
    }

    /**
     * Get total P&L.
     */
    public BigDecimal getTotalPnl() {
        return realizedPnl.add(unrealizedPnl);
    }

    /**
     * Factory for new position from order fill.
     */
    public static Position fromFill(String userId, String instrumentToken, String symbol,
            String exchange, String segment, String product,
            int quantity, BigDecimal price) {
        boolean isBuy = quantity > 0;
        return new Position(
                userId, instrumentToken, symbol, exchange, segment, product,
                quantity,
                isBuy ? Math.abs(quantity) : 0,
                isBuy ? 0 : Math.abs(quantity),
                isBuy ? price : BigDecimal.ZERO,
                isBuy ? BigDecimal.ZERO : price,
                price,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                Instant.now());
    }
}
