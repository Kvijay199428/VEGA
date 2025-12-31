package com.vegatrader.upstox.api.websocket.manager;

import com.vegatrader.upstox.api.websocket.ratelimiter.EnterpriseRateLimiterService;
import com.vegatrader.upstox.api.websocket.settings.*;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Centralized subscription manager for MarketDataStreamerV3.
 * 
 * <p>
 * Manages:
 * <ul>
 * <li>Active subscriptions per user and category</li>
 * <li>Subscription limit enforcement</li>
 * <li>Integration with rate limiter service</li>
 * <li>Metrics for monitoring</li>
 * </ul>
 * 
 * @since 2.0.0
 */
@Service
public class SubscriptionManager {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionManager.class);

    private final EnterpriseRateLimiterService rateLimiterService;
    private final MeterRegistry meterRegistry;

    // Active subscriptions: userId -> category -> list of instrument keys
    private final Map<String, Map<SubscriptionCategory, Set<String>>> subscriptions = new ConcurrentHashMap<>();

    // User types
    private final Map<String, UserType> userTypes = new ConcurrentHashMap<>();

    // Metrics
    private final AtomicInteger totalSubscriptions = new AtomicInteger(0);
    private final Map<SubscriptionCategory, AtomicInteger> categorySubscriptions = new EnumMap<>(
            SubscriptionCategory.class);

    public SubscriptionManager(EnterpriseRateLimiterService rateLimiterService, MeterRegistry meterRegistry) {
        this.rateLimiterService = rateLimiterService;
        this.meterRegistry = meterRegistry;

        // Initialize category counters
        for (SubscriptionCategory cat : SubscriptionCategory.values()) {
            categorySubscriptions.put(cat, new AtomicInteger(0));
        }
    }

    @PostConstruct
    public void initMetrics() {
        Gauge.builder("subscriptions.total", totalSubscriptions, AtomicInteger::get)
                .description("Total active subscriptions")
                .register(meterRegistry);

        for (SubscriptionCategory cat : SubscriptionCategory.values()) {
            AtomicInteger counter = categorySubscriptions.get(cat);
            Gauge.builder("subscriptions.category", counter, AtomicInteger::get)
                    .tag("category", cat.name())
                    .description("Subscriptions per category")
                    .register(meterRegistry);
        }

        logger.info("SubscriptionManager metrics initialized");
    }

    /**
     * Checks if a user can subscribe to the requested instruments.
     * 
     * @param userId        User identifier
     * @param category      Subscription category
     * @param requestedKeys Number of keys to subscribe
     * @return true if subscription is allowed
     */
    public boolean canSubscribe(String userId, SubscriptionCategory category, int requestedKeys) {
        UserType userType = userTypes.getOrDefault(userId, UserType.NORMAL);

        if (!LimitsConfig.isCategoryAvailable(userType, category)) {
            logger.warn("Category {} not available for user type {}", category, userType);
            return false;
        }

        SubscriptionLimits limits = LimitsConfig.getSubscriptionLimit(userType, category);
        int currentKeys = getCurrentSubscriptionCount(userId, category);
        int activeCategoriesCount = getActiveCategories(userId).size();

        // Use combined limit if subscribing to multiple categories
        int limitToApply = activeCategoriesCount > 0 ? limits.getCombinedLimit() : limits.getIndividualLimit();

        boolean allowed = (currentKeys + requestedKeys) <= limitToApply;

        if (!allowed) {
            logger.warn("Subscription limit exceeded for user {} category {}. Current: {}, Requested: {}, Limit: {}",
                    userId, category, currentKeys, requestedKeys, limitToApply);
        }

        return allowed;
    }

    /**
     * Subscribes to instruments.
     * 
     * @param userId         User identifier
     * @param category       Subscription category
     * @param instrumentKeys Instrument keys to subscribe
     * @return true if subscription was successful
     */
    public boolean subscribe(String userId, SubscriptionCategory category, Collection<String> instrumentKeys) {
        if (!canSubscribe(userId, category, instrumentKeys.size())) {
            return false;
        }

        // Try to acquire from rate limiter
        if (!rateLimiterService.trySubscribe(userId, category.getMode(), instrumentKeys.size())) {
            return false;
        }

        // Add to subscriptions
        subscriptions
                .computeIfAbsent(userId, u -> new ConcurrentHashMap<>())
                .computeIfAbsent(category, c -> ConcurrentHashMap.newKeySet())
                .addAll(instrumentKeys);

        // Update metrics
        totalSubscriptions.addAndGet(instrumentKeys.size());
        categorySubscriptions.get(category).addAndGet(instrumentKeys.size());

        logger.info("Subscribed user {} to {} instruments in category {}",
                userId, instrumentKeys.size(), category);
        return true;
    }

    /**
     * Unsubscribes from instruments.
     */
    public void unsubscribe(String userId, SubscriptionCategory category, Collection<String> instrumentKeys) {
        Map<SubscriptionCategory, Set<String>> userSubs = subscriptions.get(userId);
        if (userSubs == null)
            return;

        Set<String> catSubs = userSubs.get(category);
        if (catSubs == null)
            return;

        int removed = 0;
        for (String key : instrumentKeys) {
            if (catSubs.remove(key)) {
                removed++;
            }
        }

        // Release from rate limiter
        rateLimiterService.release(userId, category.getMode(), removed);

        // Update metrics
        totalSubscriptions.addAndGet(-removed);
        categorySubscriptions.get(category).addAndGet(-removed);

        logger.info("Unsubscribed user {} from {} instruments in category {}",
                userId, removed, category);
    }

    /**
     * Unsubscribes from all instruments in a category.
     */
    public void unsubscribeAll(String userId, SubscriptionCategory category) {
        Map<SubscriptionCategory, Set<String>> userSubs = subscriptions.get(userId);
        if (userSubs == null)
            return;

        Set<String> catSubs = userSubs.remove(category);
        if (catSubs == null)
            return;

        int removed = catSubs.size();
        rateLimiterService.release(userId, category.getMode(), removed);

        totalSubscriptions.addAndGet(-removed);
        categorySubscriptions.get(category).addAndGet(-removed);

        logger.info("Unsubscribed user {} from all {} instruments in category {}",
                userId, removed, category);
    }

    /**
     * Unsubscribes user from all categories.
     */
    public void unsubscribeAll(String userId) {
        Map<SubscriptionCategory, Set<String>> userSubs = subscriptions.remove(userId);
        if (userSubs == null)
            return;

        for (Map.Entry<SubscriptionCategory, Set<String>> entry : userSubs.entrySet()) {
            int removed = entry.getValue().size();
            rateLimiterService.release(userId, entry.getKey().getMode(), removed);
            totalSubscriptions.addAndGet(-removed);
            categorySubscriptions.get(entry.getKey()).addAndGet(-removed);
        }

        logger.info("Unsubscribed user {} from all subscriptions", userId);
    }

    /**
     * Sets user type.
     */
    public void setUserType(String userId, UserType userType) {
        userTypes.put(userId, userType);
        rateLimiterService.setUserType(userId, userType);
        logger.info("Set user {} to type {}", userId, userType);
    }

    /**
     * Gets user type.
     */
    public UserType getUserType(String userId) {
        return userTypes.getOrDefault(userId, UserType.NORMAL);
    }

    /**
     * Gets current subscription count for a user and category.
     */
    public int getCurrentSubscriptionCount(String userId, SubscriptionCategory category) {
        Map<SubscriptionCategory, Set<String>> userSubs = subscriptions.get(userId);
        if (userSubs == null)
            return 0;
        Set<String> catSubs = userSubs.get(category);
        return catSubs != null ? catSubs.size() : 0;
    }

    /**
     * Gets active categories for a user.
     */
    public Set<SubscriptionCategory> getActiveCategories(String userId) {
        Map<SubscriptionCategory, Set<String>> userSubs = subscriptions.get(userId);
        if (userSubs == null)
            return Collections.emptySet();
        return userSubs.keySet();
    }

    /**
     * Gets all subscribed instrument keys for a user.
     */
    public Set<String> getSubscribedKeys(String userId, SubscriptionCategory category) {
        Map<SubscriptionCategory, Set<String>> userSubs = subscriptions.get(userId);
        if (userSubs == null)
            return Collections.emptySet();
        Set<String> catSubs = userSubs.get(category);
        return catSubs != null ? new HashSet<>(catSubs) : Collections.emptySet();
    }

    /**
     * Gets remaining capacity for a user and category.
     */
    public int getRemainingCapacity(String userId, SubscriptionCategory category) {
        UserType userType = userTypes.getOrDefault(userId, UserType.NORMAL);
        if (!LimitsConfig.isCategoryAvailable(userType, category)) {
            return 0;
        }

        SubscriptionLimits limits = LimitsConfig.getSubscriptionLimit(userType, category);
        int currentKeys = getCurrentSubscriptionCount(userId, category);
        int activeCategoriesCount = getActiveCategories(userId).size();
        int limitToApply = activeCategoriesCount > 0 ? limits.getCombinedLimit() : limits.getIndividualLimit();

        return Math.max(0, limitToApply - currentKeys);
    }

    /**
     * Gets total subscription count.
     */
    public int getTotalSubscriptions() {
        return totalSubscriptions.get();
    }

    /**
     * Gets subscription count by category.
     */
    public Map<SubscriptionCategory, Integer> getSubscriptionsByCategory() {
        Map<SubscriptionCategory, Integer> result = new EnumMap<>(SubscriptionCategory.class);
        categorySubscriptions.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }
}
