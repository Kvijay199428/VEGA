package com.vegatrader.upstox.api.websocket.ratelimiter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe token bucket for rate limiting.
 * 
 * <p>
 * Uses atomic operations for lock-free concurrency.
 * 
 * @since 2.0.0
 */
public class TokenBucket {

    private final int capacity;
    private final AtomicInteger tokens;

    public TokenBucket(int capacity) {
        this.capacity = capacity;
        this.tokens = new AtomicInteger(capacity);
    }

    /**
     * Attempts to consume n tokens.
     * 
     * @param n Number of tokens to consume
     * @return true if tokens were consumed, false if insufficient tokens
     */
    public boolean consume(int n) {
        while (true) {
            int current = tokens.get();
            if (current < n) {
                return false;
            }
            if (tokens.compareAndSet(current, current - n)) {
                return true;
            }
            // CAS failed, retry
        }
    }

    /**
     * Returns tokens to the bucket.
     * 
     * @param n Number of tokens to return
     */
    public void returnTokens(int n) {
        tokens.updateAndGet(current -> Math.min(current + n, capacity));
    }

    /**
     * Gets the number of available tokens.
     */
    public int getAvailableTokens() {
        return tokens.get();
    }

    /**
     * Gets the number of used tokens.
     */
    public int getUsedTokens() {
        return capacity - tokens.get();
    }

    /**
     * Gets the bucket capacity.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Resets the bucket to full capacity.
     */
    public void reset() {
        tokens.set(capacity);
    }

    /**
     * Gets utilization percentage.
     */
    public double getUtilizationPercent() {
        return ((double) getUsedTokens() / capacity) * 100.0;
    }
}
