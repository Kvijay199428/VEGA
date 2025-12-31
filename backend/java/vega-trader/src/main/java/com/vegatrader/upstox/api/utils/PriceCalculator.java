package com.vegatrader.upstox.api.utils;

/**
 * Utility class for price and P&L calculations.
 *
 * @since 2.0.0
 */
public final class PriceCalculator {

    private PriceCalculator() {
        // Utility class - no instantiation
    }

    /**
     * Calculates total value.
     *
     * @param quantity the quantity
     * @param price    the price
     * @return total value
     */
    public static double calculateValue(int quantity, double price) {
        return quantity * price;
    }

    /**
     * Calculates profit/loss.
     *
     * @param buyPrice  the buy price
     * @param sellPrice the sell price
     * @param quantity  the quantity
     * @return P&L amount
     */
    public static double calculatePnL(double buyPrice, double sellPrice, int quantity) {
        return (sellPrice - buyPrice) * quantity;
    }

    /**
     * Calculates profit/loss percentage.
     *
     * @param buyPrice  the buy price
     * @param sellPrice the sell price
     * @return P&L percentage
     */
    public static double calculatePnLPercent(double buyPrice, double sellPrice) {
        if (buyPrice == 0)
            return 0;
        return ((sellPrice - buyPrice) / buyPrice) * 100;
    }

    /**
     * Calculates average price.
     *
     * @param totalValue    the total value
     * @param totalQuantity the total quantity
     * @return average price
     */
    public static double calculateAveragePrice(double totalValue, int totalQuantity) {
        if (totalQuantity == 0)
            return 0;
        return totalValue / totalQuantity;
    }

    /**
     * Calculates breakeven price for long position.
     *
     * @param entryPrice the entry price
     * @param charges    the total charges
     * @param quantity   the quantity
     * @return breakeven price
     */
    public static double calculateBreakevenLong(double entryPrice, double charges, int quantity) {
        if (quantity == 0)
            return 0;
        return entryPrice + (charges / quantity);
    }

    /**
     * Calculates breakeven price for short position.
     *
     * @param entryPrice the entry price
     * @param charges    the total charges
     * @param quantity   the quantity
     * @return breakeven price
     */
    public static double calculateBreakevenShort(double entryPrice, double charges, int quantity) {
        if (quantity == 0)
            return 0;
        return entryPrice - (charges / quantity);
    }

    /**
     * Rounds price to 2 decimal places.
     *
     * @param price the price
     * @return rounded price
     */
    public static double roundPrice(double price) {
        return Math.round(price * 100.0) / 100.0;
    }

    /**
     * Rounds to specified decimal places.
     *
     * @param value  the value
     * @param places the decimal places
     * @return rounded value
     */
    public static double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        return (double) Math.round(value * factor) / factor;
    }

    /**
     * Calculates option premium value.
     *
     * @param premium the premium per unit
     * @param lotSize the lot size
     * @return total premium value
     */
    public static double calculateOptionValue(double premium, int lotSize) {
        return premium * lotSize;
    }

    /**
     * Calculates implied price from Greeks.
     *
     * @param strike         the strike price
     * @param delta          the delta
     * @param underlyingMove the underlying price move
     * @param gamma          the gamma
     * @return estimated new option price
     */
    public static double estimateOptionPriceChange(double strike, double delta,
            double underlyingMove, double gamma) {
        // First order: delta * move
        double firstOrder = delta * underlyingMove;

        // Second order: 0.5 * gamma * move^2
        double secondOrder = 0.5 * gamma * underlyingMove * underlyingMove;

        return firstOrder + secondOrder;
    }

    /**
     * Formats price as currency string.
     *
     * @param price the price
     * @return formatted string
     */
    public static String formatPrice(double price) {
        return String.format("₹%.2f", price);
    }

    /**
     * Formats P&L with sign.
     *
     * @param pnl the P&L amount
     * @return formatted string
     */
    public static String formatPnL(double pnl) {
        String sign = pnl >= 0 ? "+" : "";
        return String.format("%s₹%.2f", sign, pnl);
    }
}
