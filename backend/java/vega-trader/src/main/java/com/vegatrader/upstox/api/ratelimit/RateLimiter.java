package com.vegatrader.upstox.api.ratelimit;

/**
 * Rate limiter interface for Upstox API endpoints.
 * <p>
 * All rate limiters must implement this interface to provide
 * consistent rate limiting behavior across different API categories.
 * </p>
 *
 * @since 2.0.0
 */
public interface RateLimiter {

    /**
     * Checks if a request is allowed within the current rate limits.
     *
     * @return RateLimitStatus indicating whether the request is allowed
     */
    RateLimitStatus checkLimit();

    /**
     * Records a new request timestamp.
     * <p>
     * This should be called after a request is successfully made
     * to track usage against rate limits.
     * </p>
     */
    void recordRequest();

    /**
     * Gets the current rate limit usage statistics.
     *
     * @return RateLimitUsage with current usage metrics
     */
    RateLimitUsage getCurrentUsage();

    /**
     * Waits and retries if rate limited, using exponential backoff.
     *
     * @param maxRetries maximum number of retry attempts
     * @return true if allowed after retries, false if retries exhausted
     */
    boolean waitAndRetry(int maxRetries);

    /**
     * Resets all rate limit counters.
     * <p>
     * Use with caution - typically only for testing or administrative purposes.
     * </p>
     */
    void reset();

    /**
     * Gets the rate limiter configuration.
     *
     * @return RateLimitConfig with limits and window sizes
     */
    RateLimitConfig getConfig();
}
