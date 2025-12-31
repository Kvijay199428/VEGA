package com.vegatrader.upstox.api.rms.validation;

import com.vegatrader.upstox.api.rms.eligibility.ProductEligibility;

/**
 * RMS validation result.
 * 
 * @since 4.1.0
 */
public record RmsValidationResult(
        boolean approved,
        String code,
        String message,
        Double requiredMargin,
        ProductEligibility eligibility) {

    public static RmsValidationResult approved(double margin, ProductEligibility eligibility) {
        return new RmsValidationResult(true, "APPROVED", "Order validated", margin, eligibility);
    }

    public static RmsValidationResult rejected(String code, String message) {
        return new RmsValidationResult(false, code, message, null, null);
    }

    public boolean isRejected() {
        return !approved;
    }
}
