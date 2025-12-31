package com.vegatrader.upstox.api.websocket.ratelimiter;

import java.util.Map;

/**
 * Rate limiter interface for subscription management.
 * 
 * @since 2.0.0
 */
public interface RateLimiter {

    /**
     * Attempts to acquire tokens for a subscription.
     * 
     * @param category      Category name
     * @param keysRequested Number of keys to subscribe
     * @return true if tokens acquired, false if limit exceeded
     */
    boolean tryAcquire(String category, int keysRequested);

    /**
     * Releases tokens when unsubscribing.
     * 
     * @param category Category name
     * @param keysUsed Number of keys to release
     */
    void release(String category, int keysUsed);

    /**
     * Gets current usage per category.
     */
    Map<String, Integer> getCurrentUsage();

    /**
     * Gets combined usage across all categories.
     */
    int getCombinedUsage();
}
