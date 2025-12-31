package com.vegatrader.analytics.valuation;

import com.vegatrader.upstox.api.optionchain.model.OptionChainStrike;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Comparator;

@Service
public class OptionChainValuationService {

    public java.util.Map<Integer, com.vegatrader.upstox.api.optionchain.stream.OptionChainFeedStreamV3.StrikeNode> enrich(
            java.util.Map<Integer, com.vegatrader.upstox.api.optionchain.stream.OptionChainFeedStreamV3.StrikeNode> strikes,
            LocalDate expiry,
            ValuationSettings cfg) {

        if (!cfg.enableValuation() || strikes == null || strikes.isEmpty()) {
            return strikes;
        }

        double spot = getSpotPrice(strikes.values());
        long daysToExpiry = LocalDate.now().until(expiry, ChronoUnit.DAYS);
        double timeToExpiry = Math.max(daysToExpiry / 365.0, 0.0001);

        // 1. Calculate ATM IV
        double fairVol = calculateAtmIV(strikes.values(), spot, timeToExpiry, cfg);

        // 2. Evaluate all strikes and build new map
        java.util.Map<Integer, com.vegatrader.upstox.api.optionchain.stream.OptionChainFeedStreamV3.StrikeNode> enriched = new java.util.HashMap<>();

        for (var entry : strikes.entrySet()) {
            enriched.put(entry.getKey(), evaluateNode(entry.getValue(), spot, timeToExpiry, fairVol, cfg));
        }

        return enriched;
    }

    private double getSpotPrice(
            Collection<com.vegatrader.upstox.api.optionchain.stream.OptionChainFeedStreamV3.StrikeNode> strikes) {
        // Approximate spot from nearest ATM strike or use underlying LTP if available.
        // For now, we take the average LTP of ATM call/put or first available.
        // In a real scenario, spot should be passed in. We'll extract it from an ATM
        // option's underlying spot price (if available)
        // However, StrikeNode usually doesn't store spot directly.
        // Let's assume the calling context provides spot or we infer it.
        // Wait, the new `OptionChainStrike` has `underlyingSpotPrice`.
        // But `OptionChainFeedStreamV3.StrikeNode` does NOT have underlyingSpotPrice
        // directly.
        // We will assume `OptionChainFeedStreamV3` manages `underlyingKey` but maybe
        // not spot.
        // Let's assume we can get it from one of the option's Greeks or MarketData if
        // populated.
        // Actually, let's implement a simple ATM inference:
        // Find strike where Call and Put LTP are closest.
        return strikes.stream()
                .filter(n -> n.call() != null && n.put() != null)
                .min(Comparator
                        .comparingDouble(n -> Math.abs(n.call().marketData().ltp() - n.put().marketData().ltp())))
                .map(n -> (double) n.strikePrice()) // Approx spot is ATM strike (with parity adjustment)
                .orElse(0.0);
    }

    private double calculateAtmIV(
            Collection<com.vegatrader.upstox.api.optionchain.stream.OptionChainFeedStreamV3.StrikeNode> strikes,
            double spot, double T, ValuationSettings cfg) {
        // Find ATM strike
        var atmNode = strikes.stream()
                .min(Comparator.comparingDouble(n -> Math.abs(n.strikePrice() - spot)))
                .orElse(null);

        if (atmNode == null)
            return cfg.defaultVolatility();

        double callIV = 0;
        double putIV = 0;

        if (atmNode.call() != null) {
            callIV = ImpliedVolatilitySolver.calculateIV(
                    atmNode.call().marketData().ltp(),
                    spot,
                    atmNode.strikePrice(),
                    T,
                    cfg.riskFreeRate(),
                    true,
                    cfg.defaultVolatility(),
                    cfg.maxAllowedIV(),
                    cfg.defaultVolatility());
        }

        if (atmNode.put() != null) {
            putIV = ImpliedVolatilitySolver.calculateIV(
                    atmNode.put().marketData().ltp(),
                    spot,
                    atmNode.strikePrice(),
                    T,
                    cfg.riskFreeRate(),
                    false,
                    cfg.defaultVolatility(),
                    cfg.maxAllowedIV(),
                    cfg.defaultVolatility());
        }

        if (callIV > 0 && putIV > 0)
            return (callIV + putIV) / 2.0;
        if (callIV > 0)
            return callIV;
        if (putIV > 0)
            return putIV;
        return cfg.defaultVolatility();
    }

    private com.vegatrader.upstox.api.optionchain.stream.OptionChainFeedStreamV3.StrikeNode evaluateNode(
            com.vegatrader.upstox.api.optionchain.stream.OptionChainFeedStreamV3.StrikeNode node,
            double spot,
            double T,
            double fairVol,
            ValuationSettings cfg) {
        var call = node.call();
        var put = node.put();

        if (call != null) {
            ValuationResult res = ValuationEngine.evaluate(
                    toOptionData(call), // Adapter needed or use overloaded evaluate
                    spot,
                    node.strikePrice(),
                    T,
                    true,
                    fairVol,
                    cfg);
            call = new com.vegatrader.upstox.api.optionchain.stream.OptionChainFeedStreamV3.OptionLeg(
                    call.instrumentKey(), call.marketData(), call.greeks(), res);
        }

        if (put != null) {
            ValuationResult res = ValuationEngine.evaluate(
                    toOptionData(put),
                    spot,
                    node.strikePrice(),
                    T,
                    false,
                    fairVol,
                    cfg);
            put = new com.vegatrader.upstox.api.optionchain.stream.OptionChainFeedStreamV3.OptionLeg(
                    put.instrumentKey(), put.marketData(), put.greeks(), res);
        }

        return new com.vegatrader.upstox.api.optionchain.stream.OptionChainFeedStreamV3.StrikeNode(
                node.strikePrice(), call, put);
    }

    // Adapt OptionLeg to OptionData for ValuationEngine compatibility
    // Actually ValuationEngine expects OptionChainStrike.OptionData which has
    // strictly types
    // But logic only needs market data. Let's overload ValuationEngine or verify
    // structure compatibility.
    // OptionChainStrike.OptionData structure: (key, marketData, greeks, valuation)
    // OptionChainFeedStreamV3.OptionLeg structure: (key, marketData, greeks,
    // valuation)
    // They are almost identical but different classes.
    // I can map them.
    private OptionChainStrike.OptionData toOptionData(
            com.vegatrader.upstox.api.optionchain.stream.OptionChainFeedStreamV3.OptionLeg leg) {
        // We need to map inner records too (MarketData, OptionGreeks) if they are
        // different classes
        // In OptionChainStrike and OptionChainFeedStreamV3 they are defined as inner
        // records independently.
        // So yes, mapping is required.

        return new OptionChainStrike.OptionData(
                leg.instrumentKey(),
                mapMarketData(leg.marketData()),
                mapGreeks(leg.greeks()),
                leg.valuation());
    }

    private OptionChainStrike.MarketData mapMarketData(
            com.vegatrader.upstox.api.optionchain.stream.OptionChainFeedStreamV3.MarketData md) {
        return new OptionChainStrike.MarketData(
                md.ltp(), md.closePrice(), md.volume(), md.oi(),
                md.bidPrice(), md.bidQty(), md.askPrice(), md.askQty(), md.prevOi());
    }

    private OptionChainStrike.OptionGreeks mapGreeks(
            com.vegatrader.upstox.api.optionchain.stream.OptionChainFeedStreamV3.OptionGreeks g) {
        return new OptionChainStrike.OptionGreeks(
                g != null ? g.delta() : 0,
                g != null ? g.gamma() : 0,
                g != null ? g.theta() : 0,
                g != null ? g.vega() : 0,
                g != null ? g.iv() : 0,
                g != null ? g.pop() : 0);
    }
}
