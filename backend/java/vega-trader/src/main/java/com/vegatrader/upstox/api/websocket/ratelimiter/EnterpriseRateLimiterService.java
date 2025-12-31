package com.vegatrader.upstox.api.websocket.ratelimiter;

import com.vegatrader.upstox.api.websocket.settings.LimitsConfig;
import com.vegatrader.upstox.api.websocket.settings.SubscriptionCategory;
import com.vegatrader.upstox.api.websocket.settings.SubscriptionLimits;
import com.vegatrader.upstox.api.websocket.settings.UserType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Enterprise-grade rate limiter service for MarketDataStreamerV3.
 * 
 * <p>
 * Provides:
 * <ul>
 * <li>Per-user rate limiting</li>
 * <li>Category-based subscription limits</li>
 * <li>Combined limit enforcement</li>
 * <li>Micrometer metrics integration</li>
 * </ul>
 * 
 * @since 2.0.0
 */
@Service
public class EnterpriseRateLimiterService {

    private static final Logger logger = LoggerFactory.getLogger(EnterpriseRateLimiterService.class);

    private final Map<String, RateLimiter> userLimiters = new ConcurrentHashMap<>();
    private final Map<String, UserType> userTypes = new ConcurrentHashMap<>();
    private final MeterRegistry meterRegistry;

    // Metrics
    private final Counter subscriptionAttempts;
    private final Counter subscriptionRejections;
    private final AtomicInteger activeUsers = new AtomicInteger(0);

    public EnterpriseRateLimiterService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize metrics
        this.subscriptionAttempts = Counter.builder("ratelimiter.subscription.attempts")
                .description("Total subscription attempts")
                .register(meterRegistry);

        this.subscriptionRejections = Counter.builder("ratelimiter.subscription.rejections")
                .description("Rejected subscription attempts")
                .register(meterRegistry);

        Gauge.builder("ratelimiter.active.users", activeUsers, AtomicInteger::get)
                .description("Number of active users")
                .register(meterRegistry);

        logger.info("EnterpriseRateLimiterService initialized with Micrometer metrics");
    }

    /**
     * Attempts to subscribe to a category for a user.
     * 
     * @param userId        User identifier
     * @param category      Subscription category
     * @param keysRequested Number of instrument keys
     * @return true if subscription allowed
     */
    public boolean trySubscribe(String userId, String category, int keysRequested) {
        subscriptionAttempts.increment();

        RateLimiter limiter = getOrCreateLimiter(userId);
        boolean allowed = limiter.tryAcquire(category, keysRequested);

        if (!allowed) {
            subscriptionRejections.increment();
            logger.warn("Subscription rejected for user {} category {} keys {}",
                    userId, category, keysRequested);
        } else {
            logger.info("Subscription approved for user {} category {} keys {}",
                    userId, category, keysRequested);
        }

        return allowed;
    }

    /**
     * Attempts to subscribe, throwing exception if rejected.
     */
    public void subscribeOrThrow(String userId, String category, int keysRequested) {
        if (!trySubscribe(userId, category, keysRequested)) {
            RateLimiter limiter = userLimiters.get(userId);
            int available = limiter != null ? limiter.getCombinedUsage() : 0;
            throw new RateLimitExceededException(category, keysRequested, available);
        }
    }

    /**
     * Releases subscription tokens when unsubscribing.
     */
    public void release(String userId, String category, int keysUsed) {
        RateLimiter limiter = userLimiters.get(userId);
        if (limiter != null) {
            limiter.release(category, keysUsed);
            logger.info("Released {} keys for user {} category {}", keysUsed, userId, category);
        }
    }

    /**
     * Gets current usage for a user.
     */
    public Map<String, Integer> currentUsage(String userId) {
        RateLimiter limiter = userLimiters.get(userId);
        return limiter != null ? limiter.getCurrentUsage() : Map.of();
    }

    /**
     * Gets total active keys for a user.
     */
    public int getTotalActiveKeys(String userId) {
        RateLimiter limiter = userLimiters.get(userId);
        return limiter != null ? limiter.getCombinedUsage() : 0;
    }

    /**
     * Sets user type (required before subscription).
     */
    public void setUserType(String userId, UserType userType) {
        userTypes.put(userId, userType);
        // Remove existing limiter to recreate with new type
        userLimiters.remove(userId);
        logger.info("Set user {} to type {}", userId, userType);
    }

    /**
     * Gets configured limits for a user.
     */
    public Map<SubscriptionCategory, SubscriptionLimits> getUserLimits(String userId) {
        UserType type = userTypes.getOrDefault(userId, UserType.NORMAL);
        return LimitsConfig.getLimits(type);
    }

    /**
     * Removes a user from tracking.
     */
    public void removeUser(String userId) {
        userLimiters.remove(userId);
        userTypes.remove(userId);
        activeUsers.decrementAndGet();
        logger.info("Removed user {} from rate limiter", userId);
    }

    /**
     * Gets active user count.
     */
    public int getActiveUserCount() {
        return activeUsers.get();
    }

    private RateLimiter getOrCreateLimiter(String userId) {
        return userLimiters.computeIfAbsent(userId, u -> {
            activeUsers.incrementAndGet();
            UserType type = userTypes.getOrDefault(u, UserType.NORMAL);
            return createLimiterForUserType(type);
        });
    }

    private RateLimiter createLimiterForUserType(UserType type) {
        Map<SubscriptionCategory, SubscriptionLimits> limits = LimitsConfig.getLimits(type);

        Map<String, Integer> categoryLimits = new ConcurrentHashMap<>();
        int combinedLimit = 0;

        for (Map.Entry<SubscriptionCategory, SubscriptionLimits> entry : limits.entrySet()) {
            categoryLimits.put(entry.getKey().getMode(), entry.getValue().getIndividualLimit());
            combinedLimit = Math.max(combinedLimit, entry.getValue().getCombinedLimit());
        }

        return new TokenBucketRateLimiter(categoryLimits, combinedLimit);
    }
}
