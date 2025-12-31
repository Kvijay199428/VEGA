package com.vegatrader.analytics.valuation;

/**
 * Result of a valuation assessment.
 */
public record ValuationResult(
        ValuationStatus status,
        double fairPrice,
        double marketPrice,
        double mispricingPct,
        Action action,
        boolean blinking,
        ConfidenceLevel confidence) {
}
