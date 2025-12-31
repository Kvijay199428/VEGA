package com.vegatrader.upstox.api.request.websocket;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Subscription data payload for Market Data Feed V3 WebSocket requests.
 * 
 * <p>
 * Contains the subscription mode and list of instrument keys.
 * 
 * @since 3.0.0
 */
public class MarketDataFeedV3SubscriptionData {

    /**
     * Subscription mode.
     */
    @SerializedName("mode")
    private String mode;

    /**
     * List of instrument keys to subscribe to.
     * Format: "EXCHANGE|TOKEN" (e.g., "NSE_INDEX|Nifty Bank")
     */
    @SerializedName("instrumentKeys")
    private List<String> instrumentKeys;

    public MarketDataFeedV3SubscriptionData() {
    }

    public MarketDataFeedV3SubscriptionData(String mode, List<String> instrumentKeys) {
        this.mode = mode;
        this.instrumentKeys = instrumentKeys;
    }

    public MarketDataFeedV3SubscriptionData(MarketDataMode mode, List<String> instrumentKeys) {
        this.mode = mode.getValue();
        this.instrumentKeys = instrumentKeys;
    }

    // Getters and Setters

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setMode(MarketDataMode mode) {
        this.mode = mode.getValue();
    }

    /**
     * Gets the mode as an enum.
     * 
     * @return the MarketDataMode enum, or null if mode is invalid
     */
    public MarketDataMode getModeEnum() {
        if (mode != null) {
            try {
                return MarketDataMode.fromValue(mode);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    public List<String> getInstrumentKeys() {
        return instrumentKeys;
    }

    public void setInstrumentKeys(List<String> instrumentKeys) {
        this.instrumentKeys = instrumentKeys;
    }

    /**
     * Validates the subscription data.
     * 
     * @param isCombinedSubscription whether this is part of a multi-mode
     *                               subscription
     * @throws IllegalArgumentException if validation fails
     */
    public void validate(boolean isCombinedSubscription) {
        if (mode == null || mode.isEmpty()) {
            throw new IllegalArgumentException("Mode is required");
        }

        if (instrumentKeys == null || instrumentKeys.isEmpty()) {
            throw new IllegalArgumentException("At least one instrument key is required");
        }

        MarketDataMode modeEnum = getModeEnum();
        if (modeEnum == null) {
            throw new IllegalArgumentException("Invalid mode: " + mode);
        }

        // Validate subscription limits
        int count = instrumentKeys.size();
        if (isCombinedSubscription) {
            modeEnum.validateCombinedLimit(count);
        } else {
            modeEnum.validateIndividualLimit(count);
        }
    }

    /**
     * Validates the subscription data assuming single-mode subscription.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        validate(false);
    }

    @Override
    public String toString() {
        return String.format("SubscriptionData{mode='%s', instruments=%d}",
                mode, instrumentKeys != null ? instrumentKeys.size() : 0);
    }
}
