package com.vegatrader.upstox.api.websocket.ratelimiter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token bucket based rate limiter implementation.
 * 
 * <p>
 * Supports per-category limits with combined limit enforcement.
 * 
 * @since 2.0.0
 */
public class TokenBucketRateLimiter implements RateLimiter {

    private static final Logger logger = LoggerFactory.getLogger(TokenBucketRateLimiter.class);

    private final Map<String, TokenBucket> categoryBuckets = new ConcurrentHashMap<>();
    private final int combinedLimit;

    /**
     * Creates a rate limiter with specified category limits.
     * 
     * @param categoryLimits Map of category name to max tokens
     * @param combinedLimit  Maximum combined usage across all categories
     */
    public TokenBucketRateLimiter(Map<String, Integer> categoryLimits, int combinedLimit) {
        categoryLimits.forEach((k, v) -> categoryBuckets.put(k, new TokenBucket(v)));
        this.combinedLimit = combinedLimit;
        logger.info("TokenBucketRateLimiter initialized with {} categories, combined limit: {}",
                categoryLimits.size(), combinedLimit);
    }

    @Override
    public boolean tryAcquire(String category, int keysRequested) {
        TokenBucket bucket = categoryBuckets.get(category);
        if (bucket == null) {
            logger.warn("Unknown category: {}", category);
            return false;
        }

        // Check combined limit first
        int totalUsed = getCombinedUsage();
        if (totalUsed + keysRequested > combinedLimit) {
            logger.warn("Combined limit exceeded. Current: {}, Requested: {}, Limit: {}",
                    totalUsed, keysRequested, combinedLimit);
            return false;
        }

        // Check category limit
        if (bucket.getAvailableTokens() < keysRequested) {
            logger.warn("Category {} limit exceeded. Available: {}, Requested: {}",
                    category, bucket.getAvailableTokens(), keysRequested);
            return false;
        }

        boolean acquired = bucket.consume(keysRequested);
        if (acquired) {
            logger.debug("Acquired {} tokens for category {}. Remaining: {}",
                    keysRequested, category, bucket.getAvailableTokens());
        }
        return acquired;
    }

    @Override
    public void release(String category, int keysUsed) {
        TokenBucket bucket = categoryBuckets.get(category);
        if (bucket != null) {
            bucket.returnTokens(keysUsed);
            logger.debug("Released {} tokens for category {}. Available: {}",
                    keysUsed, category, bucket.getAvailableTokens());
        }
    }

    @Override
    public Map<String, Integer> getCurrentUsage() {
        Map<String, Integer> usage = new ConcurrentHashMap<>();
        categoryBuckets.forEach((k, v) -> usage.put(k, v.getUsedTokens()));
        return usage;
    }

    @Override
    public int getCombinedUsage() {
        return categoryBuckets.values().stream()
                .mapToInt(TokenBucket::getUsedTokens)
                .sum();
    }

    /**
     * Gets the combined limit.
     */
    public int getCombinedLimit() {
        return combinedLimit;
    }

    /**
     * Gets remaining combined capacity.
     */
    public int getRemainingCombinedCapacity() {
        return Math.max(0, combinedLimit - getCombinedUsage());
    }

    /**
     * Resets all buckets to full capacity.
     */
    public void reset() {
        categoryBuckets.values().forEach(TokenBucket::reset);
        logger.info("All token buckets reset");
    }
}
