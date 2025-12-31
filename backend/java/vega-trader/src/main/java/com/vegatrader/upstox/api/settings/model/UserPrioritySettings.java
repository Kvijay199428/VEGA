package com.vegatrader.upstox.api.settings.model;

import java.util.List;

/**
 * User Priority Settings per final-settings.md.
 * Settings affect ORDER of operations, not AUTHORITY.
 * 
 * @since 4.6.0
 */
public record UserPrioritySettings(
        List<String> instrumentLoadPriority, // INDEX, DERIVATIVES, EQUITY, SECTORAL
        List<String> preferredSectors, // BANKING, IT, PHARMA, etc.
        List<String> validationPriority, // INSTRUMENT_ELIGIBILITY, CLIENT_RISK, QUANTITY_CAP
        List<String> brokerRoutingPriority, // UPSTOX, ZERODHA, etc.
        String defaultProductType, // INTRADAY, DELIVERY, CNC, MIS
        String defaultExchange, // NSE, BSE
        boolean confirmBeforePlace) {

    /**
     * SEBI-safe default settings.
     */
    public static UserPrioritySettings defaults() {
        return new UserPrioritySettings(
                List.of("INDEX", "DERIVATIVES", "EQUITY"),
                List.of(),
                List.of("INSTRUMENT_ELIGIBILITY", "CLIENT_RISK", "QUANTITY_CAP"),
                List.of("PRIMARY"),
                "INTRADAY",
                "NSE",
                true);
    }

    /**
     * Merge user settings with defaults (user takes precedence where valid).
     */
    public UserPrioritySettings mergeWithDefaults() {
        UserPrioritySettings def = defaults();
        return new UserPrioritySettings(
                instrumentLoadPriority != null && !instrumentLoadPriority.isEmpty()
                        ? instrumentLoadPriority
                        : def.instrumentLoadPriority(),
                preferredSectors != null ? preferredSectors : def.preferredSectors(),
                validationPriority != null && !validationPriority.isEmpty()
                        ? validationPriority
                        : def.validationPriority(),
                brokerRoutingPriority != null && !brokerRoutingPriority.isEmpty()
                        ? brokerRoutingPriority
                        : def.brokerRoutingPriority(),
                defaultProductType != null ? defaultProductType : def.defaultProductType(),
                defaultExchange != null ? defaultExchange : def.defaultExchange(),
                confirmBeforePlace);
    }

    /**
     * Validate settings are within allowed bounds.
     */
    public boolean isValid() {
        // Validation priority must contain only allowed values
        List<String> allowed = List.of("INSTRUMENT_ELIGIBILITY", "CLIENT_RISK", "QUANTITY_CAP");
        if (validationPriority != null) {
            for (String v : validationPriority) {
                if (!allowed.contains(v))
                    return false;
            }
        }

        // Exchange must be NSE or BSE
        if (defaultExchange != null && !List.of("NSE", "BSE").contains(defaultExchange)) {
            return false;
        }

        // Product type validation
        if (defaultProductType != null &&
                !List.of("INTRADAY", "DELIVERY", "CNC", "MIS").contains(defaultProductType)) {
            return false;
        }

        return true;
    }
}
