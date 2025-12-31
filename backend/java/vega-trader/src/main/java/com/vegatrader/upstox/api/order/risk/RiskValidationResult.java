package com.vegatrader.upstox.api.order.risk;

import java.util.List;

/**
 * Result of risk validation.
 * 
 * @since 4.9.0
 */
public record RiskValidationResult(
        boolean isPassed,
        List<String> violations,
        String riskScore) {

    public static RiskValidationResult passed() {
        return new RiskValidationResult(true, List.of(), "LOW");
    }

    public static RiskValidationResult failed(List<String> violations) {
        return new RiskValidationResult(false, violations, "HIGH");
    }

    public static RiskValidationResult warning(List<String> warnings) {
        return new RiskValidationResult(true, warnings, "MEDIUM");
    }
}
