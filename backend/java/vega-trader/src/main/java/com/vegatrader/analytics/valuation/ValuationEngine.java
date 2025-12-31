package com.vegatrader.analytics.valuation;

import com.vegatrader.upstox.api.optionchain.model.OptionChainStrike;

/**
 * Core engine for evaluating option pricing and signals.
 */
public final class ValuationEngine {

    private ValuationEngine() {
    }

    public static ValuationResult evaluate(
            OptionChainStrike.OptionData option,
            double spot,
            double strike,
            double timeToExpiry,
            boolean isCall,
            double fairVol,
            ValuationSettings cfg) {
        OptionChainStrike.MarketData md = option.marketData();
        double marketPrice = md.ltp();

        double fairPrice = isCall
                ? BlackScholesPricer.call(spot, strike, timeToExpiry, cfg.riskFreeRate(), fairVol)
                : BlackScholesPricer.put(spot, strike, timeToExpiry, cfg.riskFreeRate(), fairVol);

        double mispricingPct = fairPrice > 0
                ? ((marketPrice - fairPrice) / fairPrice) * 100
                : 0;

        ValuationStatus status;
        Action action;
        boolean blinking = false;

        if (timeToExpiry <= 0) {
            status = ValuationStatus.FAIR;
            action = Action.HOLD;
        } else if (mispricingPct > cfg.overvaluedThresholdPct()) {
            status = ValuationStatus.OVERVALUED;
            action = Action.SELL;
            blinking = cfg.enableBlinking();
        } else if (mispricingPct < -cfg.undervaluedThresholdPct()) {
            status = ValuationStatus.UNDERVALUED;
            action = Action.BUY;
            blinking = cfg.enableBlinking();
        } else {
            status = ValuationStatus.FAIR;
            action = Action.HOLD;
        }

        ConfidenceLevel confidence = ConfidenceScorer.score(
                md.bidPrice(),
                md.askPrice(),
                md.volume(),
                md.oi(),
                cfg);

        return new ValuationResult(
                status,
                fairPrice,
                marketPrice,
                mispricingPct,
                action,
                blinking,
                confidence);
    }
}
