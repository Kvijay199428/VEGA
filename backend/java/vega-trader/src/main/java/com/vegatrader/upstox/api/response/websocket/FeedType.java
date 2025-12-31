package com.vegatrader.upstox.api.response.websocket;

import com.google.gson.annotations.SerializedName;

/**
 * Feed message types for Market Data Feed V3.
 * 
 * <p>
 * Defines the types of messages received from the WebSocket feed:
 * <ul>
 * <li>MARKET_INFO - First message containing market status for all
 * segments</li>
 * <li>INITIAL_FEED - Second message providing snapshot of current market
 * data</li>
 * <li>LIVE_FEED - Subsequent messages with real-time updates</li>
 * </ul>
 * 
 * @since 3.0.0
 */
public enum FeedType {

    /**
     * Market information message.
     * Contains real-time status of various market segments.
     * This is always the first message received.
     */
    @SerializedName("market_info")
    MARKET_INFO("market_info"),

    /**
     * Initial feed message.
     * Provides a snapshot of current market data at the time of connection.
     */
    @SerializedName("initial_feed")
    INITIAL_FEED("initial_feed"),

    /**
     * Live feed message.
     * Contains real-time market updates.
     */
    @SerializedName("live_feed")
    LIVE_FEED("live_feed");

    private final String value;

    FeedType(String value) {
        this.value = value;
    }

    /**
     * Gets the string value used in JSON serialization.
     * 
     * @return the feed type string value
     */
    public String getValue() {
        return value;
    }

    /**
     * Converts a string value to the corresponding enum constant.
     * 
     * @param value the string value
     * @return the FeedType enum constant
     * @throws IllegalArgumentException if no matching enum constant is found
     */
    public static FeedType fromValue(String value) {
        for (FeedType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown feed type: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
