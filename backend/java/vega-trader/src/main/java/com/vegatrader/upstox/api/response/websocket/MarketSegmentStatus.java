package com.vegatrader.upstox.api.response.websocket;

import com.google.gson.annotations.SerializedName;

/**
 * Market segment status values for Market Data Feed V3.
 * 
 * <p>
 * Represents the current trading status of market segments like NSE_EQ, BSE_EQ,
 * NSE_FO, etc.
 * 
 * @since 3.0.0
 */
public enum MarketSegmentStatus {

    /**
     * Pre-opening session has started.
     */
    @SerializedName("PRE_OPEN_START")
    PRE_OPEN_START("PRE_OPEN_START"),

    /**
     * Pre-opening session has ended.
     */
    @SerializedName("PRE_OPEN_END")
    PRE_OPEN_END("PRE_OPEN_END"),

    /**
     * Normal trading session is open.
     */
    @SerializedName("NORMAL_OPEN")
    NORMAL_OPEN("NORMAL_OPEN"),

    /**
     * Normal trading session is closed.
     */
    @SerializedName("NORMAL_CLOSE")
    NORMAL_CLOSE("NORMAL_CLOSE"),

    /**
     * Closing session has started.
     */
    @SerializedName("CLOSING_START")
    CLOSING_START("CLOSING_START"),

    /**
     * Closing session has ended.
     */
    @SerializedName("CLOSING_END")
    CLOSING_END("CLOSING_END");

    private final String value;

    MarketSegmentStatus(String value) {
        this.value = value;
    }

    /**
     * Gets the string value used in JSON serialization.
     * 
     * @return the status string value
     */
    public String getValue() {
        return value;
    }

    /**
     * Checks if the market is currently open for trading.
     * 
     * @return true if status is NORMAL_OPEN, false otherwise
     */
    public boolean isOpen() {
        return this == NORMAL_OPEN;
    }

    /**
     * Checks if the market is in pre-open phase.
     * 
     * @return true if status is PRE_OPEN_START, false otherwise
     */
    public boolean isPreOpen() {
        return this == PRE_OPEN_START;
    }

    /**
     * Checks if the market is closed.
     * 
     * @return true if status is NORMAL_CLOSE or CLOSING_END, false otherwise
     */
    public boolean isClosed() {
        return this == NORMAL_CLOSE || this == CLOSING_END;
    }

    /**
     * Converts a string value to the corresponding enum constant.
     * 
     * @param value the string value
     * @return the MarketSegmentStatus enum constant
     * @throws IllegalArgumentException if no matching enum constant is found
     */
    public static MarketSegmentStatus fromValue(String value) {
        for (MarketSegmentStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown market status: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
