package com.vegatrader.upstox.api.websocket.settings;

import com.vegatrader.upstox.api.websocket.Mode;

/**
 * Subscription tier type for Upstox accounts.
 * 
 * <p>
 * Determines connection and subscription limits.
 * 
 * @since 3.0.0
 */
public enum SubscriptionTier {

    /**
     * Normal Upstox account.
     * - 2 connections per user
     * - Standard subscription limits
     */
    NORMAL,

    /**
     * Upstox Plus account.
     * - 5 connections per user
     * - Enhanced subscription limits
     * - Access to FULL_D30 mode (30 market levels)
     */
    PLUS;

    /**
     * Gets the maximum connections allowed for this tier.
     * 
     * @return max connections
     */
    public int getMaxConnections() {
        return this == PLUS ? 5 : 2;
    }

    /**
     * Gets the individual subscription limit for a mode.
     * 
     * @param mode the subscription mode
     * @return individual limit
     */
    public int getIndividualLimit(Mode mode) {
        switch (mode) {
            case LTPC:
                return 5000;
            case OPTION_GREEKS:
                return 3000;
            case FULL:
                return 2000;
            case FULL_D30:
                return this == PLUS ? 50 : 0; // Only Plus subscribers
            default:
                return 0;
        }
    }

    /**
     * Gets the combined subscription limit for a mode.
     * 
     * @param mode the subscription mode
     * @return combined limit
     */
    public int getCombinedLimit(Mode mode) {
        switch (mode) {
            case LTPC:
            case OPTION_GREEKS:
                return 2000;
            case FULL:
            case FULL_D30:
                return 1500;
            default:
                return 0;
        }
    }

    /**
     * Checks if a mode is supported for this tier.
     * 
     * @param mode the mode to check
     * @return true if supported
     */
    public boolean supports(Mode mode) {
        if (mode == Mode.FULL_D30) {
            return this == PLUS;
        }
        return true;
    }
}
