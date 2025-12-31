package com.vegatrader.upstox.api.rms.eligibility;

/**
 * Product eligibility result record.
 * Returned from EligibilityResolver/Cache for order validation.
 * 
 * @since 4.1.0
 */
public record ProductEligibility(
        boolean misAllowed,
        boolean mtfAllowed,
        boolean cncAllowed,
        String reason,
        Double marginPct,
        Double leverage) {

    /**
     * Create a normal eligibility (all allowed).
     */
    public static ProductEligibility normal() {
        return new ProductEligibility(true, true, true, "NORMAL", 20.0, 5.0);
    }

    /**
     * Create CNC-only eligibility.
     */
    public static ProductEligibility cncOnly(String reason) {
        return new ProductEligibility(false, false, true, reason, 100.0, 1.0);
    }

    /**
     * Create fully blocked eligibility.
     */
    public static ProductEligibility blocked(String reason) {
        return new ProductEligibility(false, false, false, reason, 100.0, 1.0);
    }

    /**
     * Creates eligibility with custom margin.
     */
    public static ProductEligibility withMargin(boolean mis, boolean mtf, boolean cnc,
            String reason, double marginPct, double leverage) {
        return new ProductEligibility(mis, mtf, cnc, reason, marginPct, leverage);
    }

    /**
     * Check if any trading is allowed.
     */
    public boolean isTradable() {
        return misAllowed || mtfAllowed || cncAllowed;
    }

    /**
     * Check if intraday is allowed.
     */
    public boolean isIntradayAllowed() {
        return misAllowed;
    }
}
