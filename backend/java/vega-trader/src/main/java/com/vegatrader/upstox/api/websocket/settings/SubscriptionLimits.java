package com.vegatrader.upstox.api.websocket.settings;

/**
 * Subscription limits per category.
 * 
 * <p>
 * Individual Limit: max per category if no other subscriptions active.
 * Combined Limit: max per category when subscribing to multiple categories.
 * 
 * @since 2.0.0
 */
public class SubscriptionLimits {

    private final int individualLimit;
    private final int combinedLimit;

    public SubscriptionLimits(int individualLimit, int combinedLimit) {
        this.individualLimit = individualLimit;
        this.combinedLimit = combinedLimit;
    }

    /**
     * Maximum keys allowed when subscribing to this category alone.
     */
    public int getIndividualLimit() {
        return individualLimit;
    }

    /**
     * Maximum keys allowed when subscribing to multiple categories.
     */
    public int getCombinedLimit() {
        return combinedLimit;
    }

    @Override
    public String toString() {
        return String.format("SubscriptionLimits{individual=%d, combined=%d}",
                individualLimit, combinedLimit);
    }
}
