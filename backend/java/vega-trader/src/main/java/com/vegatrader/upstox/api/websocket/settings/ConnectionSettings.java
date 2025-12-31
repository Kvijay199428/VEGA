package com.vegatrader.upstox.api.websocket.settings;

import com.vegatrader.upstox.api.websocket.Mode;

import java.util.HashMap;
import java.util.Map;

/**
 * Connection settings for Market Data WebSocket.
 * 
 * <p>
 * Manages subscription tier, connection limits, and validation.
 * 
 * @since 3.0.0
 */
public class ConnectionSettings {

    private final SubscriptionTier tier;
    private final Map<Mode, Integer> currentSubscriptionCounts;
    private int activeConnections;
    private boolean hasMultipleModes;

    public ConnectionSettings(SubscriptionTier tier) {
        this.tier = tier;
        this.currentSubscriptionCounts = new HashMap<>();
        this.activeConnections = 0;
        this.hasMultipleModes = false;
    }

    public ConnectionSettings() {
        this(SubscriptionTier.NORMAL);
    }

    /**
     * Gets the subscription tier.
     * 
     * @return the tier
     */
    public SubscriptionTier getTier() {
        return tier;
    }

    /**
     * Gets the current count for a mode.
     * 
     * @param mode the mode
     * @return current subscription count
     */
    public int getCurrentCount(Mode mode) {
        return currentSubscriptionCounts.getOrDefault(mode, 0);
    }

    /**
     * Gets total subscriptions across all modes.
     * 
     * @return total count
     */
    public int getTotalSubscriptions() {
        return currentSubscriptionCounts.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    /**
     * Checks if multiple modes are being used.
     * 
     * @return true if multiple modes
     */
    public boolean hasMultipleModes() {
        return currentSubscriptionCounts.size() > 1 || hasMultipleModes;
    }

    /**
     * Validates if a subscription can be added.
     * 
     * @param mode  the mode
     * @param count number of instruments to add
     * @throws SubscriptionLimitExceededException if limit would be exceeded
     */
    public void validateSubscription(Mode mode, int count) {
        // Check if mode is supported for this tier
        if (!tier.supports(mode)) {
            throw new SubscriptionLimitExceededException(
                    String.format("Mode %s requires Upstox Plus subscription", mode));
        }

        int currentCount = getCurrentCount(mode);
        int newCount = currentCount + count;

        // Determine if this is combined mode scenario
        boolean isCombined = hasMultipleModes() || currentSubscriptionCounts.size() > 0;

        int limit = isCombined ? tier.getCombinedLimit(mode) : tier.getIndividualLimit(mode);

        if (newCount > limit) {
            String limitType = isCombined ? "combined" : "individual";
            throw new SubscriptionLimitExceededException(
                    String.format("Subscription limit exceeded for mode %s. " +
                            "Attempting to subscribe %d instruments would result in %d, " +
                            "but %s limit is %d",
                            mode, count, newCount, limitType, limit));
        }
    }

    /**
     * Adds subscriptions for a mode.
     * 
     * @param mode  the mode
     * @param count number to add
     */
    public void addSubscriptions(Mode mode, int count) {
        int current = getCurrentCount(mode);
        currentSubscriptionCounts.put(mode, current + count);

        if (currentSubscriptionCounts.size() > 1) {
            hasMultipleModes = true;
        }
    }

    /**
     * Removes subscriptions for a mode.
     * 
     * @param mode  the mode
     * @param count number to remove
     */
    public void removeSubscriptions(Mode mode, int count) {
        int current = getCurrentCount(mode);
        int newCount = Math.max(0, current - count);

        if (newCount == 0) {
            currentSubscriptionCounts.remove(mode);
        } else {
            currentSubscriptionCounts.put(mode, newCount);
        }

        // Update multiple modes flag
        hasMultipleModes = currentSubscriptionCounts.size() > 1;
    }

    /**
     * Changes subscriptions from one mode to another.
     * 
     * @param mode  the new mode
     * @param count number of instruments
     */
    public void changeMode(Mode mode, int count) {
        // This is essentially changing mode, so just validate and update
        validateSubscription(mode, count);
        currentSubscriptionCounts.put(mode, count);
    }

    /**
     * Checks if connection limit is reached.
     * 
     * @return true if can add connection
     */
    public boolean canAddConnection() {
        return activeConnections < tier.getMaxConnections();
    }

    /**
     * Increments active connection count.
     * 
     * @throws SubscriptionLimitExceededException if max connections reached
     */
    public void incrementConnections() {
        if (!canAddConnection()) {
            throw new SubscriptionLimitExceededException(
                    String.format("Maximum connections (%d) reached for %s tier",
                            tier.getMaxConnections(), tier));
        }
        activeConnections++;
    }

    /**
     * Decrements active connection count.
     */
    public void decrementConnections() {
        activeConnections = Math.max(0, activeConnections - 1);
    }

    /**
     * Gets active connection count.
     * 
     * @return count
     */
    public int getActiveConnections() {
        return activeConnections;
    }

    /**
     * Resets all subscription counts.
     */
    public void reset() {
        currentSubscriptionCounts.clear();
        hasMultipleModes = false;
    }

    @Override
    public String toString() {
        return String.format("ConnectionSettings{tier=%s, connections=%d/%d, subscriptions=%s}",
                tier, activeConnections, tier.getMaxConnections(), currentSubscriptionCounts);
    }

    /**
     * Exception thrown when subscription limits are exceeded.
     */
    public static class SubscriptionLimitExceededException extends RuntimeException {
        public SubscriptionLimitExceededException(String message) {
            super(message);
        }
    }
}
