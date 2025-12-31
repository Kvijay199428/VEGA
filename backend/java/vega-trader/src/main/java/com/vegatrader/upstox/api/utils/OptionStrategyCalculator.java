package com.vegatrader.upstox.api.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for option strategy calculations.
 *
 * @since 2.0.0
 */
public final class OptionStrategyCalculator {

    private OptionStrategyCalculator() {
        // Utility class - no instantiation
    }

    /**
     * Calculates max profit for long call.
     *
     * @param strikePrice the strike price
     * @param premium     the premium paid
     * @param spotPrice   the current spot price
     * @param lotSize     the lot size
     * @return max profit (unlimited, returns current profit)
     */
    public static double calculateLongCallProfit(double strikePrice, double premium,
            double spotPrice, int lotSize) {
        double intrinsicValue = Math.max(0, spotPrice - strikePrice);
        return (intrinsicValue - premium) * lotSize;
    }

    /**
     * Calculates max loss for long call.
     *
     * @param premium the premium paid
     * @param lotSize the lot size
     * @return max loss
     */
    public static double calculateLongCallMaxLoss(double premium, int lotSize) {
        return premium * lotSize;
    }

    /**
     * Calculates breakeven for long call.
     *
     * @param strikePrice the strike price
     * @param premium     the premium paid
     * @return breakeven price
     */
    public static double calculateLongCallBreakeven(double strikePrice, double premium) {
        return strikePrice + premium;
    }

    /**
     * Calculates profit for long put.
     *
     * @param strikePrice the strike price
     * @param premium     the premium paid
     * @param spotPrice   the current spot price
     * @param lotSize     the lot size
     * @return current profit/loss
     */
    public static double calculateLongPutProfit(double strikePrice, double premium,
            double spotPrice, int lotSize) {
        double intrinsicValue = Math.max(0, strikePrice - spotPrice);
        return (intrinsicValue - premium) * lotSize;
    }

    /**
     * Calculates max profit for long put.
     *
     * @param strikePrice the strike price
     * @param premium     the premium paid
     * @param lotSize     the lot size
     * @return max profit (if spot goes to 0)
     */
    public static double calculateLongPutMaxProfit(double strikePrice, double premium, int lotSize) {
        return (strikePrice - premium) * lotSize;
    }

    /**
     * Calculates breakeven for long put.
     *
     * @param strikePrice the strike price
     * @param premium     the premium paid
     * @return breakeven price
     */
    public static double calculateLongPutBreakeven(double strikePrice, double premium) {
        return strikePrice - premium;
    }

    /**
     * Calculates profit for bull call spread.
     *
     * @param lowerStrike  the lower strike (long call)
     * @param higherStrike the higher strike (short call)
     * @param netPremium   the net premium paid (long premium - short premium)
     * @param spotPrice    the current spot price
     * @param lotSize      the lot size
     * @return current profit/loss
     */
    public static double calculateBullCallSpreadProfit(double lowerStrike, double higherStrike,
            double netPremium, double spotPrice, int lotSize) {
        double longIntrinsic = Math.max(0, spotPrice - lowerStrike);
        double shortIntrinsic = Math.max(0, spotPrice - higherStrike);
        return ((longIntrinsic - shortIntrinsic) - netPremium) * lotSize;
    }

    /**
     * Calculates max profit for bull call spread.
     *
     * @param lowerStrike  the lower strike
     * @param higherStrike the higher strike
     * @param netPremium   the net premium paid
     * @param lotSize      the lot size
     * @return max profit
     */
    public static double calculateBullCallSpreadMaxProfit(double lowerStrike, double higherStrike,
            double netPremium, int lotSize) {
        return ((higherStrike - lowerStrike) - netPremium) * lotSize;
    }

    /**
     * Calculates profit for iron condor.
     *
     * @param config    the iron condor configuration
     * @param spotPrice the current spot price
     * @return current profit/loss
     */
    public static double calculateIronCondorProfit(IronCondorConfig config, double spotPrice) {
        // Put spread profit
        double putSpreadProfit = calculateBearPutSpreadProfit(
                config.putLowerStrike, config.putHigherStrike,
                config.putNetPremium, spotPrice, config.lotSize);

        // Call spread profit
        double callSpreadProfit = calculateBearCallSpreadProfit(
                config.callLowerStrike, config.callHigherStrike,
                config.callNetPremium, spotPrice, config.lotSize);

        return putSpreadProfit + callSpreadProfit;
    }

    /**
     * Calculates profit for bear call spread.
     *
     * @param lowerStrike  the lower strike (short call)
     * @param higherStrike the higher strike (long call)
     * @param netPremium   the net premium received (short premium - long premium)
     * @param spotPrice    the current spot price
     * @param lotSize      the lot size
     * @return current profit/loss
     */
    public static double calculateBearCallSpreadProfit(double lowerStrike, double higherStrike,
            double netPremium, double spotPrice, int lotSize) {
        double shortIntrinsic = Math.max(0, spotPrice - lowerStrike);
        double longIntrinsic = Math.max(0, spotPrice - higherStrike);
        return (netPremium - (shortIntrinsic - longIntrinsic)) * lotSize;
    }

    /**
     * Calculates profit for bear put spread.
     *
     * @param lowerStrike  the lower strike (short put)
     * @param higherStrike the higher strike (long put)
     * @param netPremium   the net premium paid (long premium - short premium)
     * @param spotPrice    the current spot price
     * @param lotSize      the lot size
     * @return current profit/loss
     */
    public static double calculateBearPutSpreadProfit(double lowerStrike, double higherStrike,
            double netPremium, double spotPrice, int lotSize) {
        double longIntrinsic = Math.max(0, higherStrike - spotPrice);
        double shortIntrinsic = Math.max(0, lowerStrike - spotPrice);
        return ((longIntrinsic - shortIntrinsic) - netPremium) * lotSize;
    }

    /**
     * Configuration for iron condor strategy.
     */
    public static class IronCondorConfig {
        public double putLowerStrike;
        public double putHigherStrike;
        public double callLowerStrike;
        public double callHigherStrike;
        public double putNetPremium;
        public double callNetPremium;
        public int lotSize;
    }
}
