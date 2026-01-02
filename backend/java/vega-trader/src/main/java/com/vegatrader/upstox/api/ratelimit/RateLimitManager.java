package com.vegatrader.upstox.api.ratelimit;

import com.vegatrader.upstox.api.endpoints.UpstoxEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central manager for all rate limiters.
 * <p>
 * This class automatically selects the appropriate rate limiter based on
 * the endpoint being accessed. It maintains separate rate limiter instances
 * for different API categories.
 * </p>
 * <p>
 * Uses Spring DI for rate limiter injection (deterministic for replay).
 * </p>
 *
 * @since 2.0.0
 */
@Service
public class RateLimitManager {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitManager.class);

    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    private final RateLimiter standardLimiter;
    private final RateLimiter multiOrderLimiter;

    /**
     * Constructor for Spring DI.
     */
    public RateLimitManager(StandardAPIRateLimiter standardLimiter,
            MultiOrderAPIRateLimiter multiOrderLimiter) {
        this.standardLimiter = standardLimiter;
        this.multiOrderLimiter = multiOrderLimiter;

        // Register limiters
        limiters.put("STANDARD", standardLimiter);
        limiters.put("MULTI_ORDER", multiOrderLimiter);

        logger.info("RateLimitManager initialized with {} limiter categories", limiters.size());
    }

    /**
     * Checks rate limit for the given endpoint.
     *
     * @param endpoint the endpoint to check
     * @return RateLimitStatus
     */
    public RateLimitStatus checkLimit(UpstoxEndpoint endpoint) {
        RateLimiter limiter = getLimiterForEndpoint(endpoint);
        return limiter.checkLimit();
    }

    /**
     * Records a request for the given endpoint.
     *
     * @param endpoint the endpoint
     */
    public void recordRequest(UpstoxEndpoint endpoint) {
        RateLimiter limiter = getLimiterForEndpoint(endpoint);
        limiter.recordRequest();
    }

    /**
     * Gets current usage statistics for the given endpoint.
     *
     * @param endpoint the endpoint
     * @return RateLimitUsage
     */
    public RateLimitUsage getCurrentUsage(UpstoxEndpoint endpoint) {
        RateLimiter limiter = getLimiterForEndpoint(endpoint);
        return limiter.getCurrentUsage();
    }

    /**
     * Waits and retries if rate limited for the given endpoint.
     *
     * @param endpoint   the endpoint
     * @param maxRetries maximum retry attempts
     * @return true if successful, false if exhausted
     */
    public boolean waitAndRetry(UpstoxEndpoint endpoint, int maxRetries) {
        RateLimiter limiter = getLimiterForEndpoint(endpoint);
        return limiter.waitAndRetry(maxRetries);
    }

    /**
     * Gets the standard API rate limiter.
     *
     * @return the standard rate limiter
     */
    public RateLimiter getStandardLimiter() {
        return standardLimiter;
    }

    /**
     * Gets the multi-order API rate limiter.
     *
     * @return the multi-order rate limiter
     */
    public RateLimiter getMultiOrderLimiter() {
        return multiOrderLimiter;
    }

    /**
     * Resets all rate limiters.
     * <p>
     * Use with caution - typically only for testing.
     * </p>
     */
    public void resetAll() {
        logger.warn("Resetting all rate limiters");
        limiters.values().forEach(RateLimiter::reset);
    }

    /**
     * Prints usage statistics for all limiters.
     */
    public void printAllUsage() {
        System.out.println("=== Rate Limit Usage Statistics ===");
        limiters.forEach((category, limiter) -> {
            RateLimitUsage usage = limiter.getCurrentUsage();
            System.out.printf("%s: %s%n", category, usage);
        });
        System.out.println("===================================");
    }

    /**
     * Selects the appropriate rate limiter for an endpoint.
     *
     * @param endpoint the endpoint
     * @return the appropriate RateLimiter
     */
    private RateLimiter getLimiterForEndpoint(UpstoxEndpoint endpoint) {
        // Determine which limiter to use based on endpoint path
        String path = endpoint.getPath();

        // Multi-order endpoints
        if (path.contains("/multi/") || path.contains("/positions/exit")) {
            logger.debug("Using multi-order limiter for endpoint: {}", path);
            return multiOrderLimiter;
        }

        // Default to standard limiter
        logger.debug("Using standard limiter for endpoint: {}", path);
        return standardLimiter;
    }

    /**
     * Gets rate limiter by category name.
     *
     * @param category the category ("STANDARD" or "MULTI_ORDER")
     * @return the rate limiter
     * @throws IllegalArgumentException if category not found
     */
    public RateLimiter getLimiterByCategory(String category) {
        RateLimiter limiter = limiters.get(category.toUpperCase());
        if (limiter == null) {
            throw new IllegalArgumentException("Unknown rate limiter category: " + category);
        }
        return limiter;
    }
}
