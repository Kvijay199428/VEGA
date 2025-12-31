package com.vegatrader.upstox.api.order.charges;

import java.math.BigDecimal;

/**
 * Charge breakdown for an order.
 * Per order-mgmt/b2.md section 5.
 * 
 * @since 4.9.0
 */
public record ChargeBreakdown(
        BigDecimal brokerage,
        BigDecimal stt,
        BigDecimal exchangeFees,
        BigDecimal gst,
        BigDecimal sebiCharges,
        BigDecimal stampDuty,
        BigDecimal totalCharges,
        BigDecimal turnover) {

    /**
     * Get net value (turnover - total charges for sell, + for buy).
     */
    public BigDecimal netValue(String side) {
        if ("SELL".equalsIgnoreCase(side)) {
            return turnover.subtract(totalCharges);
        } else {
            return turnover.add(totalCharges);
        }
    }

    /**
     * Get impact on P&L.
     */
    public BigDecimal pnlImpact() {
        return totalCharges.negate();
    }

    /**
     * Zero charges (for simulation/testing).
     */
    public static ChargeBreakdown zero(BigDecimal turnover) {
        return new ChargeBreakdown(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, turnover);
    }
}
