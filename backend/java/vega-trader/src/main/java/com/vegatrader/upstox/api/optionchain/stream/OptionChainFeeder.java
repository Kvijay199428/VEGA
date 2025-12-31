package com.vegatrader.upstox.api.optionchain.stream;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Option Chain Feeder - frontend-optimized data transformation.
 * Per websocket/a1.md section 6.
 * 
 * Frontend should never mutate core stream.
 * 
 * @since 4.8.0
 */
public class OptionChainFeeder {

    private final OptionChainFeedStreamV3 stream;
    private final double spotPrice;

    public OptionChainFeeder(OptionChainFeedStreamV3 stream, double spotPrice) {
        this.stream = stream;
        this.spotPrice = spotPrice;
    }

    /**
     * Transform stream into frontend-friendly rows.
     */
    public java.util.List<FeederRow> toFeederRows() {
        return stream.getStrikes().entrySet().stream()
                .map(e -> toFeederRow(e.getKey(), e.getValue()))
                .sorted((a, b) -> Integer.compare(a.strike(), b.strike()))
                .toList();
    }

    /**
     * Convert strike node to feeder row.
     */
    private FeederRow toFeederRow(int strike, OptionChainFeedStreamV3.StrikeNode node) {
        return new FeederRow(
                strike,
                extractLegData(node.call()),
                extractLegData(node.put()),
                calculateMoneyness(strike),
                calculatePCR(node));
    }

    /**
     * Extract leg data for UI.
     */
    private LegData extractLegData(OptionChainFeedStreamV3.OptionLeg leg) {
        if (leg == null)
            return null;

        var md = leg.marketData();
        var g = leg.greeks();

        return new LegData(
                md != null ? md.ltp() : 0,
                md != null ? md.oi() : 0,
                md != null ? md.volume() : 0,
                g != null ? g.delta() : 0,
                g != null ? g.iv() : 0);
    }

    /**
     * Calculate moneyness (ATM/ITM/OTM).
     */
    private String calculateMoneyness(int strike) {
        double diff = Math.abs(strike - spotPrice);
        double pct = diff / spotPrice;

        if (pct < 0.01)
            return "ATM";
        return strike < spotPrice ? "ITM_CALL" : "OTM_CALL";
    }

    /**
     * Calculate PCR for strike.
     */
    private double calculatePCR(OptionChainFeedStreamV3.StrikeNode node) {
        if (node.call() == null || node.put() == null)
            return 0;

        long callOI = node.call().marketData() != null ? node.call().marketData().oi() : 0;
        long putOI = node.put().marketData() != null ? node.put().marketData().oi() : 0;

        if (callOI == 0)
            return 0;
        return (double) putOI / callOI;
    }

    /**
     * Feeder row for frontend consumption.
     */
    public record FeederRow(
            int strike,
            LegData call,
            LegData put,
            String moneyness,
            double pcr) {
    }

    /**
     * Leg data for UI rendering.
     */
    public record LegData(
            double ltp,
            long oi,
            long volume,
            double delta,
            double iv) {
    }
}
