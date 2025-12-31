package com.vegatrader.upstox.api.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe rate limiter for Upstox Standard APIs.
 * <p>
 * Rate Limits:
 * <ul>
 * <li>50 requests per second</li>
 * <li>500 requests per minute</li>
 * <li>2000 requests per 30 minutes</li>
 * </ul>
 * </p>
 * <p>
 * This implementation uses a sliding window algorithm to accurately track
 * request rates across all time windows. It is thread-safe and can be used
 * in multi-threaded environments.
 * </p>
 * <p>
 * <b>Usage Example:</b>
 * 
 * <pre>{@code
 * StandardAPIRateLimiter limiter = new StandardAPIRateLimiter();
 * 
 * // Check before making request
 * if (limiter.checkLimit() == RateLimitStatus.OK) {
 *     // Make API call
 *     makeApiCall();
 *     // Record the request
 *     limiter.recordRequest();
 * } else {
 *     // Wait and retry
 *     limiter.waitAndRetry(3);
 * }
 * }</pre>
 * </p>
 *
 * @since 2.0.0
 */
public class StandardAPIRateLimiter implements RateLimiter {

    private static final Logger logger = LoggerFactory.getLogger(StandardAPIRateLimiter.class);

    private final RateLimitConfig config;
    private final Queue<Instant> requestTimes = new LinkedList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Creates a new standard API rate limiter with default limits.
     */
    public StandardAPIRateLimiter() {
        this.config = RateLimitConfig.standardApi();
    }

    /**
     * Creates a new rate limiter with custom configuration.
     *
     * @param config the rate limit configuration
     */
    public StandardAPIRateLimiter(RateLimitConfig config) {
        this.config = config;
    }

    @Override
    public RateLimitStatus checkLimit() {
        lock.writeLock().lock();
        try {
            cleanupOldRequests();
            Instant now = Instant.now();

            // Check 30-minute limit (most restrictive for long-term)
            if (requestTimes.size() >= config.getPer30MinLimit()) {
                logger.warn("Rate limit exceeded: 30-minute limit ({} requests)", config.getPer30MinLimit());
                return RateLimitStatus.LIMIT_EXCEEDED_30MIN;
            }

            // Check 1-minute limit
            Instant minuteAgo = now.minus(Duration.ofMinutes(1));
            long requestsInLastMinute = requestTimes.stream()
                    .filter(t -> t.isAfter(minuteAgo))
                    .count();

            if (requestsInLastMinute >= config.getPerMinuteLimit()) {
                logger.warn("Rate limit exceeded: 1-minute limit ({} requests)", config.getPerMinuteLimit());
                return RateLimitStatus.LIMIT_EXCEEDED_MINUTE;
            }

            // Check 1-second limit (most restrictive for bursts)
            Instant secondAgo = now.minus(Duration.ofSeconds(1));
            long requestsInLastSecond = requestTimes.stream()
                    .filter(t -> t.isAfter(secondAgo))
                    .count();

            if (requestsInLastSecond >= config.getPerSecondLimit()) {
                logger.warn("Rate limit exceeded: 1-second limit ({} requests)", config.getPerSecondLimit());
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
            requestTimes.offer(Instant.now());
            logger.debug("Request recorded. Total requests in memory: {}", requestTimes.size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public RateLimitUsage getCurrentUsage() {
        lock.readLock().lock();
        try {
            Instant now = Instant.now();
            Instant secondAgo = now.minus(Duration.ofSeconds(1));
            Instant minuteAgo = now.minus(Duration.ofMinutes(1));

            long perSecond = requestTimes.stream()
                    .filter(t -> t.isAfter(secondAgo))
                    .count();

            long perMinute = requestTimes.stream()
                    .filter(t -> t.isAfter(minuteAgo))
                    .count();

            long per30Min = requestTimes.size();

            return new RateLimitUsage(
                    (int) perSecond, config.getPerSecondLimit(),
                    (int) perMinute, config.getPerMinuteLimit(),
                    (int) per30Min, config.getPer30MinLimit());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean waitAndRetry(int maxRetries) {
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            RateLimitStatus status = checkLimit();

            if (status == RateLimitStatus.OK) {
                logger.info("Rate limit check passed on attempt {}", attempt + 1);
                return true;
            }

            if (attempt < maxRetries - 1) {
                long backoffMs = calculateBackoff(attempt, status);
                logger.info("Rate limited ({}), waiting {}ms before retry {}/{}",
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

        logger.error("Rate limit retry exhausted after {} attempts", maxRetries);
        return false;
    }

    @Override
    public void reset() {
        lock.writeLock().lock();
        try {
            requestTimes.clear();
            logger.info("Rate limiter reset - all counters cleared");
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public RateLimitConfig getConfig() {
        return config;
    }

    /**
     * Removes requests outside the 30-minute window.
     */
    private void cleanupOldRequests() {
        Instant thirtyMinutesAgo = Instant.now().minus(Duration.ofMinutes(30));
        int removed = 0;

        while (!requestTimes.isEmpty() && requestTimes.peek().isBefore(thirtyMinutesAgo)) {
            requestTimes.poll();
            removed++;
        }

        if (removed > 0) {
            logger.debug("Cleaned up {} old request records", removed);
        }
    }

    /**
     * Calculates backoff duration based on attempt and limit status.
     *
     * @param attempt the retry attempt number
     * @param status  the rate limit status
     * @return backoff duration in milliseconds
     */
    private long calculateBackoff(int attempt, RateLimitStatus status) {
        // Base backoff with exponential increase
        long baseBackoff = (long) (100 * Math.pow(2, attempt));

        // Adjust based on which limit was hit
        switch (status) {
            case LIMIT_EXCEEDED_SECOND:
                return Math.min(baseBackoff, 1000); // Max 1 second
            case LIMIT_EXCEEDED_MINUTE:
                return Math.min(baseBackoff, 5000); // Max 5 seconds
            case LIMIT_EXCEEDED_30MIN:
                return Math.min(baseBackoff, 30000); // Max 30 seconds
            default:
                return baseBackoff;
        }
    }
}
