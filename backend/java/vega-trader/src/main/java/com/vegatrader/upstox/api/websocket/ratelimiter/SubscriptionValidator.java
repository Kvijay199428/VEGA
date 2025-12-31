package com.vegatrader.upstox.api.websocket.ratelimiter;

import com.vegatrader.upstox.api.websocket.settings.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;

/**
 * Subscription validator for MarketDataStreamerV3.
 * 
 * <p>
 * Enforces:
 * <ul>
 * <li>Connection limits per user type</li>
 * <li>Individual subscription limits per category</li>
 * <li>Combined subscription limits across categories</li>
 * </ul>
 * 
 * @since 2.0.0
 */
public class SubscriptionValidator {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionValidator.class);

    private final UserType userType;
    private int activeConnections;
    private final Map<SubscriptionCategory, Integer> subscriptions;

    public SubscriptionValidator(UserType userType) {
        this.userType = userType;
        this.activeConnections = 0;
        this.subscriptions = new EnumMap<>(SubscriptionCategory.class);
    }

    /**
     * Adds a connection. Throws if limit exceeded.
     */
    public synchronized void addConnection() {
        int limit = LimitsConfig.getConnectionLimit(userType);
        if (activeConnections >= limit) {
            throw new IllegalStateException(
                    String.format("Maximum connections exceeded for user type %s. Limit: %d",
                            userType, limit));
        }
        activeConnections++;
        logger.info("Connection added. Active: {}/{}", activeConnections, limit);
    }

    /**
     * Removes a connection.
     */
    public synchronized void removeConnection() {
        if (activeConnections > 0) {
            activeConnections--;
            logger.info("Connection removed. Active: {}/{}",
                    activeConnections, LimitsConfig.getConnectionLimit(userType));
        }
    }

    /**
     * Validates and registers a subscription.
     * 
     * @param category  The subscription category
     * @param keysCount Number of instrument keys to subscribe
     * @throws IllegalArgumentException if limits exceeded
     */
    public synchronized void subscribe(SubscriptionCategory category, int keysCount) {
        // Check if category is supported
        if (!LimitsConfig.isCategoryAvailable(userType, category)) {
            throw new IllegalArgumentException(
                    String.format("Category %s not supported for user type %s", category, userType));
        }

        SubscriptionLimits limits = LimitsConfig.getSubscriptionLimit(userType, category);

        // Determine which limit to apply
        int activeCategories = subscriptions.size();
        int limitToApply = activeCategories > 0 ? limits.getCombinedLimit() : limits.getIndividualLimit();

        if (keysCount > limitToApply) {
            throw new IllegalArgumentException(
                    String.format("Subscription keys exceed limit for category %s. Limit: %d, Requested: %d",
                            category, limitToApply, keysCount));
        }

        // Check combined total across all categories
        int currentTotal = subscriptions.values().stream().mapToInt(Integer::intValue).sum();
        int maxCombined = limits.getCombinedLimit();
        if (currentTotal + keysCount > maxCombined * (activeCategories + 1)) {
            throw new IllegalArgumentException(
                    String.format("Combined subscription limit exceeded. Total: %d, Adding: %d",
                            currentTotal, keysCount));
        }

        subscriptions.put(category, keysCount);
        logger.info("Subscribed to {} with {} keys. Total subscriptions: {}",
                category, keysCount, subscriptions);
    }

    /**
     * Unsubscribes from a category.
     */
    public synchronized void unsubscribe(SubscriptionCategory category) {
        Integer removed = subscriptions.remove(category);
        if (removed != null) {
            logger.info("Unsubscribed from {}. Released {} keys", category, removed);
        }
    }

    /**
     * Validates a subscription request without registering it.
     * 
     * @return true if the subscription would be allowed
     */
    public synchronized boolean canSubscribe(SubscriptionCategory category, int keysCount) {
        try {
            if (!LimitsConfig.isCategoryAvailable(userType, category)) {
                return false;
            }

            SubscriptionLimits limits = LimitsConfig.getSubscriptionLimit(userType, category);
            int activeCategories = subscriptions.size();
            int limitToApply = activeCategories > 0 ? limits.getCombinedLimit() : limits.getIndividualLimit();

            return keysCount <= limitToApply;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the current number of active connections.
     */
    public int getActiveConnections() {
        return activeConnections;
    }

    /**
     * Gets current subscriptions map.
     */
    public Map<SubscriptionCategory, Integer> getSubscriptions() {
        return new EnumMap<>(subscriptions);
    }

    /**
     * Gets total subscribed keys across all categories.
     */
    public int getTotalSubscribedKeys() {
        return subscriptions.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Gets the user type.
     */
    public UserType getUserType() {
        return userType;
    }

    /**
     * Gets remaining capacity for a category.
     */
    public int getRemainingCapacity(SubscriptionCategory category) {
        if (!LimitsConfig.isCategoryAvailable(userType, category)) {
            return 0;
        }

        SubscriptionLimits limits = LimitsConfig.getSubscriptionLimit(userType, category);
        int activeCategories = subscriptions.size();
        int limitToApply = activeCategories > 0 ? limits.getCombinedLimit() : limits.getIndividualLimit();
        int current = subscriptions.getOrDefault(category, 0);

        return Math.max(0, limitToApply - current);
    }
}
