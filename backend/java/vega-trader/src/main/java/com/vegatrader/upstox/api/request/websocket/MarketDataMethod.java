package com.vegatrader.upstox.api.request.websocket;

import com.google.gson.annotations.SerializedName;

/**
 * WebSocket subscription method types for Market Data Feed V3.
 * 
 * <p>
 * Defines the available methods for managing WebSocket subscriptions:
 * <ul>
 * <li>SUB - Subscribe to new instrument keys</li>
 * <li>CHANGE_MODE - Change the subscription mode for existing instruments</li>
 * <li>UNSUB - Unsubscribe from instrument keys</li>
 * </ul>
 * 
 * @since 3.0.0
 */
public enum MarketDataMethod {

    /**
     * Subscribe to instrument keys.
     * Default mode is LTPC unless specified by user.
     */
    @SerializedName("sub")
    SUB("sub"),

    /**
     * Change subscription mode for instrument keys.
     * Instrument key is mandatory when using this method.
     */
    @SerializedName("change_mode")
    CHANGE_MODE("change_mode"),

    /**
     * Unsubscribe from instrument keys.
     * Removes instruments from further updates.
     */
    @SerializedName("unsub")
    UNSUB("unsub");

    private final String value;

    MarketDataMethod(String value) {
        this.value = value;
    }

    /**
     * Gets the string value used in JSON serialization.
     * 
     * @return the method string value
     */
    public String getValue() {
        return value;
    }

    /**
     * Converts a string value to the corresponding enum constant.
     * 
     * @param value the string value
     * @return the MarketDataMethod enum constant
     * @throws IllegalArgumentException if no matching enum constant is found
     */
    public static MarketDataMethod fromValue(String value) {
        for (MarketDataMethod method : values()) {
            if (method.value.equalsIgnoreCase(value)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown method: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
