package com.vegatrader.market.feed;

/**
 * Market data feed modes corresponding to Upstox subscription modes.
 */
public enum FeedMode {

    /**
     * Last Traded Price + Close (minimal data)
     */
    LTPC("ltpc"),

    /**
     * Full quote with L5 depth
     */
    FULL("full"),

    /**
     * Full quote with L30 depth + Greeks
     */
    FULL_D30("full_d30");

    private final String upstoxMode;

    FeedMode(String upstoxMode) {
        this.upstoxMode = upstoxMode;
    }

    /**
     * Get the Upstox API mode string.
     */
    public String toUpstox() {
        return upstoxMode;
    }

    /**
     * Parse from Upstox mode string.
     */
    public static FeedMode fromUpstox(String mode) {
        for (FeedMode m : values()) {
            if (m.upstoxMode.equalsIgnoreCase(mode)) {
                return m;
            }
        }
        return FULL; // default
    }
}
