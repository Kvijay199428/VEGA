package com.vegatrader.analytics.valuation;

/**
 * Scores the confidence level of a valuation based on market quality.
 */
public final class ConfidenceScorer {

    private ConfidenceScorer() {
    }

    public static ConfidenceLevel score(
            double bid,
            double ask,
            long volume,
            long oi,
            ValuationSettings cfg) {
        double mid = (bid + ask) > 0 ? (bid + ask) / 2 : 1;
        double spreadPct = (ask - bid) / mid;

        boolean wideSpread = spreadPct > cfg.confidenceSpreadThreshold();
        boolean lowVol = volume < cfg.lowVolumeThreshold();
        boolean lowOi = oi < cfg.lowOiThreshold();

        if (wideSpread || (lowVol && lowOi))
            return ConfidenceLevel.LOW;
        if (lowVol || lowOi)
            return ConfidenceLevel.MEDIUM;
        return ConfidenceLevel.HIGH;
    }
}
