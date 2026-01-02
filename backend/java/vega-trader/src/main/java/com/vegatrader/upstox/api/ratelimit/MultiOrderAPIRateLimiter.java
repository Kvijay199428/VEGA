package com.vegatrader.upstox.api.ratelimit;

import com.vegatrader.util.time.TimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe rate limiter for Upstox Multi-Order (Batch) APIs.
 * <p>
 * Rate Limits:
 * <ul>
 * <li>4 requests per second</li>
 * <li>40 requests per minute</li>
 * <li>160 requests per 30 minutes</li>
 * <li>Max 10 orders per batch request</li>
 * </ul>
 * </p>
 * <p>
 * Uses TimeProvider for deterministic time during Market Replay.
 * </p>
 *
 * @since 2.0.0
 */
@Component
public class MultiOrderAPIRateLimiter implements RateLimiter {

    private static final Logger logger = LoggerFactory.getLogger(MultiOrderAPIRateLimiter.class);
    private static final int MAX_ORDERS_PER_REQUEST = 10;

    private final RateLimitConfig config;
    private TimeProvider timeProvider;
    private final Queue<Instant> requestTimes = new LinkedList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Default constructor for frameworks/proxies.
     */
    protected MultiOrderAPIRateLimiter() {
        this.config = RateLimitConfig.multiOrderApi();
        this.timeProvider = null;
    }

    /**
     * Creates a new multi-order API rate limiter with default limits.
     */
    public MultiOrderAPIRateLimiter(TimeProvider timeProvider) {
        this.config = RateLimitConfig.multiOrderApi();
        this.timeProvider = timeProvider;
    }

    /**
     * Creates a new rate limiter with custom configuration.
     *
     * @param config       the rate limit configuration
     * @param timeProvider the time provider
     */
    public MultiOrderAPIRateLimiter(RateLimitConfig config, TimeProvider timeProvider) {
        this.config = config;
        this.timeProvider = timeProvider;
    }

    /**
     * Checks if a batch order with the given count is allowed.
     *
     * @param orderCount the number of orders in the batch
     * @return true if allowed, false if not
     */
    public boolean canPlaceBatch(int orderCount) {
        if (orderCount <= 0) {
            logger.warn("Order count must be > 0");
            return false;
        }

        if (orderCount > MAX_ORDERS_PER_REQUEST) {
            logger.warn("Order count {} exceeds maximum {} orders per request",
                    orderCount, MAX_ORDERS_PER_REQUEST);
            return false;
        }

        RateLimitStatus status = checkLimit();
        return status == RateLimitStatus.OK;
    }

    @Override
    public RateLimitStatus checkLimit() {
        lock.writeLock().lock();
        try {
            cleanupOldRequests();
            Instant now = timeProvider.now();

            // Check 30-minute limit
            if (requestTimes.size() >= config.getPer30MinLimit()) {
                logger.warn("Multi-order rate limit exceeded: 30-minute limit ({} requests)",
                        config.getPer30MinLimit());
                return RateLimitStatus.LIMIT_EXCEEDED_30MIN;
            }

            // Check 1-minute limit
            Instant minuteAgo = now.minus(Duration.ofMinutes(1));
            long requestsInLastMinute = requestTimes.stream()
                    .filter(t -> t.isAfter(minuteAgo))
                    .count();

            if (requestsInLastMinute >= config.getPerMinuteLimit()) {
                logger.warn("Multi-order rate limit exceeded: 1-minute limit ({} requests)",
                        config.getPerMinuteLimit());
                return RateLimitStatus.LIMIT_EXCEEDED_MINUTE;
            }

            // Check 1-second limit
            Instant secondAgo = now.minus(Duration.ofSeconds(1));
            long requestsInLastSecond = requestTimes.stream()
                    .filter(t -> t.isAfter(secondAgo))
                    .count();

            if (requestsInLastSecond >= config.getPerSecondLimit()) {
                logger.warn("Multi-order rate limit exceeded: 1-second limit ({} requests)",
                        config.getPerSecondLimit());
                return RateLimitStatus.LIMIT_EXCEEDED_SECOND;
            }

            return RateLimitStatus.OK;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void recordRequest() {
        lock.writeLock().lock();
        try {
            requestTimes.offer(timeProvider.now());
            logger.debug("Multi-order request recorded. Total: {}", requestTimes.size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public RateLimitUsage getCurrentUsage() {
        lock.readLock().lock();
        try {
            Instant now = timeProvider.now();
            Instant secondAgo = now.minus(Duration.ofSeconds(1));
            Instant minuteAgo = now.minus(Duration.ofMinutes(1));

            int perSecond = (int) requestTimes.stream()
                    .filter(t -> t.isAfter(secondAgo))
                    .count();

            int perMinute = (int) requestTimes.stream()
                    .filter(t -> t.isAfter(minuteAgo))
                    .count();

            return new RateLimitUsage(
                    perSecond, config.getPerSecondLimit(),
                    perMinute, config.getPerMinuteLimit(),
                    requestTimes.size(), config.getPer30MinLimit());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean waitAndRetry(int maxRetries) {
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            RateLimitStatus status = checkLimit();

            if (status == RateLimitStatus.OK) {
                logger.info("Multi-order rate limit check passed on attempt {}", attempt + 1);
                return true;
            }

            if (attempt < maxRetries - 1) {
                // Multi-order APIs need longer backoff due to stricter limits
                long backoffMs = calculateBackoff(attempt, status);
                logger.info("Multi-order rate limited ({}), waiting {}ms before retry {}/{}",
                        status, backoffMs, attempt + 1, maxRetries);

                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Thread interrupted during backoff", e);
                    return false;
                }
            }
        }

        logger.error("Multi-order rate limit retry exhausted after {} attempts", maxRetries);
        return false;
    }

    @Override
    public void reset() {
        lock.writeLock().lock();
        try {
            requestTimes.clear();
            logger.info("Multi-order rate limiter reset - all counters cleared");
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public RateLimitConfig getConfig() {
        return config;
    }

    /**
     * Gets the maximum orders allowed per batch request.
     *
     * @return max orders (10)
     */
    public int getMaxOrdersPerRequest() {
        return MAX_ORDERS_PER_REQUEST;
    }

    /**
     * Removes requests outside the 30-minute window.
     */
    private void cleanupOldRequests() {
        Instant thirtyMinutesAgo = timeProvider.now().minus(Duration.ofMinutes(30));
        int removed = 0;

        while (!requestTimes.isEmpty() && requestTimes.peek().isBefore(thirtyMinutesAgo)) {
            requestTimes.poll();
            removed++;
        }

        if (removed > 0) {
            logger.debug("Cleaned up {} old multi-order request records", removed);
        }
    }

    /**
     * Calculates backoff duration - longer than standard API due to stricter
     * limits.
     *
     * @param attempt the retry attempt number
     * @param status  the rate limit status
     * @return backoff duration in milliseconds
     */
    private long calculateBackoff(int attempt, RateLimitStatus status) {
        // Longer base backoff for multi-order APIs
        long baseBackoff = (long) (250 * Math.pow(2, attempt));

        switch (status) {
            case LIMIT_EXCEEDED_SECOND:
                return Math.min(baseBackoff, 2000); // Max 2 seconds
            case LIMIT_EXCEEDED_MINUTE:
                return Math.min(baseBackoff, 10000); // Max 10 seconds
            case LIMIT_EXCEEDED_30MIN:
                return Math.min(baseBackoff, 60000); // Max 60 seconds
            default:
                return baseBackoff;
        }
    }
}
