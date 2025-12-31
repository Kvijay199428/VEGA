package com.vegatrader.upstox.api.websocket.settings;

/**
 * Subscription categories for market data feeds.
 * 
 * <p>
 * Each category has different limits for Normal vs Plus users.
 * 
 * @since 2.0.0
 */
public enum SubscriptionCategory {
    /**
     * Last Traded Price and Change (LTP + change).
     * Normal: 5000 individual / 2000 combined
     */
    LTPC("ltpc"),

    /**
     * Option Greeks data.
     * Normal: 3000 individual / 2000 combined
     */
    OPTION_GREEKS("option_greeks"),

    /**
     * Full market data (all fields).
     * Normal: 2000 individual / 1500 combined
     */
    FULL("full"),

    /**
     * Full D30 - Enhanced market data (Upstox Plus only).
     * Plus: 50 individual / 1500 combined
     */
    FULL_D30("full_d30");

    private final String mode;

    SubscriptionCategory(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }

    /**
     * Parses mode string to SubscriptionCategory.
     */
    public static SubscriptionCategory fromMode(String mode) {
        for (SubscriptionCategory cat : values()) {
            if (cat.mode.equalsIgnoreCase(mode)) {
                return cat;
            }
        }
        throw new IllegalArgumentException("Unknown subscription mode: " + mode);
    }
}
