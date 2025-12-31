package com.vegatrader.upstox.api.websocket;

/**
 * Subscription modes for Upstox Market Data Feed V3.
 * 
 * Each mode has:
 * - Wire value (used in WebSocket protocol)
 * - Individual subscription limit (per connection)
 * 
 * @since 3.1.0
 */
public enum Mode {

    /**
     * Last Traded Price, Last Traded Quantity, Last Traded Time,
     * Close Price, Total Volume.
     */
    LTPC("ltpc", 5000),

    /**
     * LTPC + Option Greeks (Delta, Gamma, Vega, Theta, IV).
     * Only applicable to option instruments.
     */
    OPTION_GREEKS("option_greeks", 2000),

    /**
     * LTPC + 5 depth bid/ask + OHLC + metadata + option greeks.
     */
    FULL("full", 2000),

    /**
     * LTPC + 30 depth bid/ask + OHLC + metadata + option greeks.
     * Highest data fidelity, lowest subscription limit.
     */
    FULL_D30("full_d30", 1000);

    private final String wireValue;
    private final int individualLimit;

    Mode(String wireValue, int individualLimit) {
        this.wireValue = wireValue;
        this.individualLimit = individualLimit;
    }

    /**
     * Gets the wire protocol value.
     * 
     * @return wire value for WebSocket messages
     */
    public String getWireValue() {
        return wireValue;
    }

    /**
     * Gets the individual subscription limit for this mode.
     * 
     * @return maximum instruments per connection
     */
    public int getIndividualLimit() {
        return individualLimit;
    }

    /**
     * Parses mode from wire value.
     * 
     * @param wireValue the wire protocol value
     * @return the Mode enum
     * @throws IllegalArgumentException if invalid wire value
     */
    public static Mode fromWireValue(String wireValue) {
        for (Mode mode : values()) {
            if (mode.wireValue.equalsIgnoreCase(wireValue)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown mode: " + wireValue);
    }

    @Override
    public String toString() {
        return wireValue;
    }
}
