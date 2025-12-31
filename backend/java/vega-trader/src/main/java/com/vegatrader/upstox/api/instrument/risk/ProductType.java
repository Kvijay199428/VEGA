package com.vegatrader.upstox.api.instrument.risk;

/**
 * Enum defining product types for trading.
 * 
 * @since 4.0.0
 */
public enum ProductType {

    /**
     * Cash and Carry - Delivery trades with full margin.
     */
    CNC("CNC", "Cash and Carry", 1.0, false, true, null, 100.0),

    /**
     * Margin Intraday Square-off - Auto square-off at 3:20 PM.
     */
    MIS("MIS", "Margin Intraday Square-off", 5.0, true, false, "15:20", 20.0),

    /**
     * Margin Trading Facility - Carry forward with margin.
     */
    MTF("MTF", "Margin Trading Facility", 3.0, false, true, null, 33.33);

    private final String code;
    private final String description;
    private final double leverage;
    private final boolean intraday;
    private final boolean carryForward;
    private final String squareoffTime;
    private final double marginPct;

    ProductType(String code, String description, double leverage, boolean intraday,
            boolean carryForward, String squareoffTime, double marginPct) {
        this.code = code;
        this.description = description;
        this.leverage = leverage;
        this.intraday = intraday;
        this.carryForward = carryForward;
        this.squareoffTime = squareoffTime;
        this.marginPct = marginPct;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public double getLeverage() {
        return leverage;
    }

    public boolean isIntraday() {
        return intraday;
    }

    public boolean isCarryForward() {
        return carryForward;
    }

    public String getSquareoffTime() {
        return squareoffTime;
    }

    public double getMarginPct() {
        return marginPct;
    }

    /**
     * Calculates required margin.
     */
    public double calculateMargin(double ltp, int qty) {
        return ltp * qty * (marginPct / 100.0);
    }

    /**
     * Finds by code.
     */
    public static ProductType fromCode(String code) {
        for (ProductType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown product type: " + code);
    }
}
