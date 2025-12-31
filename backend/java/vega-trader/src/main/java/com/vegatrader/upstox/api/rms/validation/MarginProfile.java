package com.vegatrader.upstox.api.rms.validation;

/**
 * Margin profile result.
 * 
 * @since 4.1.0
 */
public record MarginProfile(
        double intradayMarginPct,
        double intradayLeverage,
        double requiredMargin) {

    /**
     * Creates margin profile from percentage.
     */
    public static MarginProfile of(double marginPct, double leverage, double price, int qty) {
        double requiredMargin = price * qty * (marginPct / 100.0);
        return new MarginProfile(marginPct, leverage, requiredMargin);
    }

    /**
     * Default margin profile (20% for EQ series).
     */
    public static MarginProfile defaultMargin(double price, int qty) {
        return of(20.0, 5.0, price, qty);
    }

    /**
     * Full margin profile (100% for T2T/CNC).
     */
    public static MarginProfile fullMargin(double price, int qty) {
        return of(100.0, 1.0, price, qty);
    }
}
