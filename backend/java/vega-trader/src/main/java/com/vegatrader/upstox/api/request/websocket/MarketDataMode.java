package com.vegatrader.upstox.api.request.websocket;

import com.google.gson.annotations.SerializedName;

/**
 * Data subscription modes for Market Data Feed V3.
 * 
 * <p>
 * Each mode has different subscription limits:
 * <ul>
 * <li>LTPC: Individual limit 5000, Combined limit 2000</li>
 * <li>OPTION_GREEKS: Individual limit 3000, Combined limit 2000</li>
 * <li>FULL: Individual limit 2000, Combined limit 1500</li>
 * <li>FULL_D30: Individual limit 50, Combined limit 1500 (Plus subscription
 * only)</li>
 * </ul>
 * 
 * <p>
 * Individual Limit: Maximum instrument keys when subscribing to a single mode.
 * <p>
 * Combined Limit: Maximum instrument keys per mode when subscribing to multiple
 * modes.
 * 
 * @since 3.0.0
 */
public enum MarketDataMode {

    /**
     * LTPC mode - Contains only Latest Trading Price (LTP) and Close Price (CP).
     * Individual limit: 5000, Combined limit: 2000
     */
    @SerializedName("ltpc")
    LTPC("ltpc", 5000, 2000),

    /**
     * Option Greeks mode - Contains only option greeks.
     * Individual limit: 3000, Combined limit: 2000
     */
    @SerializedName("option_greeks")
    OPTION_GREEKS("option_greeks", 3000, 2000),

    /**
     * Full mode - Includes LTPC, 5 market level quotes, extended feed metadata, and
     * option greeks.
     * Individual limit: 2000, Combined limit: 1500
     */
    @SerializedName("full")
    FULL("full", 2000, 1500),

    /**
     * Full D30 mode - Includes LTPC, 30 market level quotes, extended feed
     * metadata, and option greeks.
     * Available only with Upstox Plus subscription.
     * Individual limit: 50, Combined limit: 1500
     */
    @SerializedName("full_d30")
    FULL_D30("full_d30", 50, 1500);

    private final String value;
    private final int individualLimit;
    private final int combinedLimit;

    MarketDataMode(String value, int individualLimit, int combinedLimit) {
        this.value = value;
        this.individualLimit = individualLimit;
        this.combinedLimit = combinedLimit;
    }

    /**
     * Gets the string value used in JSON serialization.
     * 
     * @return the mode string value
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the individual subscription limit for this mode.
     * This is the maximum number of instrument keys when subscribing to only this
     * mode.
     * 
     * @return the individual limit
     */
    public int getIndividualLimit() {
        return individualLimit;
    }

    /**
     * Gets the combined subscription limit for this mode.
     * This is the maximum number of instrument keys for this mode when subscribing
     * to multiple modes.
     * 
     * @return the combined limit
     */
    public int getCombinedLimit() {
        return combinedLimit;
    }

    /**
     * Validates that the number of instrument keys does not exceed the individual
     * limit.
     * 
     * @param count the number of instrument keys
     * @throws IllegalArgumentException if count exceeds individual limit
     */
    public void validateIndividualLimit(int count) {
        if (count > individualLimit) {
            throw new IllegalArgumentException(
                    String.format("Instrument count %d exceeds individual limit %d for mode %s",
                            count, individualLimit, value));
        }
    }

    /**
     * Validates that the number of instrument keys does not exceed the combined
     * limit.
     * 
     * @param count the number of instrument keys
     * @throws IllegalArgumentException if count exceeds combined limit
     */
    public void validateCombinedLimit(int count) {
        if (count > combinedLimit) {
            throw new IllegalArgumentException(
                    String.format("Instrument count %d exceeds combined limit %d for mode %s",
                            count, combinedLimit, value));
        }
    }

    /**
     * Converts a string value to the corresponding enum constant.
     * 
     * @param value the string value
     * @return the MarketDataMode enum constant
     * @throws IllegalArgumentException if no matching enum constant is found
     */
    public static MarketDataMode fromValue(String value) {
        for (MarketDataMode mode : values()) {
            if (mode.value.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown mode: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
