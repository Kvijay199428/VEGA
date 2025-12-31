package com.vegatrader.upstox.api.order.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Service for API abuse prevention.
 * Per order-mgmt/b2.md section 3.2 and b4.md section 10.
 * 
 * Implements sliding window rate limiting per user and per IP.
 * 
 * @since 4.9.0
 */
@Service
public class RateLimitService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitService.class);

    // Default limits (configurable via AdminSettings)
    private static final int DEFAULT_USER_LIMIT_PER_MINUTE = 120;
    private static final int DEFAULT_IP_LIMIT_PER_MINUTE = 300;

    // Buckets: userId/IP -> RequestBucket
    private final Map<String, RequestBucket> userBuckets = new ConcurrentHashMap<>();
    private final Map<String, RequestBucket> ipBuckets = new ConcurrentHashMap<>();

    /**
     * Check if request is allowed for user.
     */
    public RateLimitResult checkUser(String userId) {
        return check(userBuckets, "USER:" + userId, DEFAULT_USER_LIMIT_PER_MINUTE);
    }

    /**
     * Check if request is allowed for IP.
     */
    public RateLimitResult checkIP(String ipAddress) {
        return check(ipBuckets, "IP:" + ipAddress, DEFAULT_IP_LIMIT_PER_MINUTE);
    }

    /**
     * Check combined user + IP.
     */
    public RateLimitResult checkRequest(String userId, String ipAddress) {
        RateLimitResult userResult = checkUser(userId);
        if (!userResult.allowed()) {
            return userResult;
        }

        RateLimitResult ipResult = checkIP(ipAddress);
        if (!ipResult.allowed()) {
            return ipResult;
        }

        return RateLimitResult.allowed(
                Math.min(userResult.remainingRequests(), ipResult.remainingRequests()),
                Math.min(userResult.resetInMs(), ipResult.resetInMs()));
    }

    /**
     * Record a request.
     */
    public void recordRequest(String userId, String ipAddress) {
        increment(userBuckets, "USER:" + userId, DEFAULT_USER_LIMIT_PER_MINUTE);
        increment(ipBuckets, "IP:" + ipAddress, DEFAULT_IP_LIMIT_PER_MINUTE);
    }

    /**
     * Get current rate limit status.
     */
    public RateLimitStatus getStatus(String userId, String ipAddress) {
        RequestBucket userBucket = userBuckets.get("USER:" + userId);
        RequestBucket ipBucket = ipBuckets.get("IP:" + ipAddress);

        int userRemaining = userBucket != null ? DEFAULT_USER_LIMIT_PER_MINUTE - userBucket.getCount()
                : DEFAULT_USER_LIMIT_PER_MINUTE;
        int ipRemaining = ipBucket != null ? DEFAULT_IP_LIMIT_PER_MINUTE - ipBucket.getCount()
                : DEFAULT_IP_LIMIT_PER_MINUTE;

        return new RateLimitStatus(
                Math.min(userRemaining, ipRemaining),
                DEFAULT_USER_LIMIT_PER_MINUTE,
                DEFAULT_IP_LIMIT_PER_MINUTE,
                60000 // Reset window: 1 minute
        );
    }

    private RateLimitResult check(Map<String, RequestBucket> buckets, String key, int limit) {
        RequestBucket bucket = buckets.computeIfAbsent(key, k -> new RequestBucket());
        bucket.cleanup();

        int count = bucket.getCount();
        if (count >= limit) {
            long resetIn = bucket.getResetTimeMs();
            logger.warn("Rate limit exceeded for {}: {}/{}", key, count, limit);
            return RateLimitResult.denied(limit - count, resetIn);
        }

        return RateLimitResult.allowed(limit - count, bucket.getResetTimeMs());
    }

    private void increment(Map<String, RequestBucket> buckets, String key, int limit) {
        RequestBucket bucket = buckets.computeIfAbsent(key, k -> new RequestBucket());
        bucket.cleanup();
        bucket.increment();
    }

    /**
     * Request bucket for sliding window.
     */
    private static class RequestBucket {
        private static final long WINDOW_MS = 60000; // 1 minute
        private final java.util.LinkedList<Long> timestamps = new java.util.LinkedList<>();

        synchronized void increment() {
            timestamps.add(System.currentTimeMillis());
        }

        synchronized void cleanup() {
            long cutoff = System.currentTimeMillis() - WINDOW_MS;
            while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoff) {
                timestamps.pollFirst();
            }
        }

        synchronized int getCount() {
            cleanup();
            return timestamps.size();
        }

        synchronized long getResetTimeMs() {
            if (timestamps.isEmpty())
                return 0;
            return WINDOW_MS - (System.currentTimeMillis() - timestamps.peekFirst());
        }
    }

    /**
     * Rate limit check result.
     */
    public record RateLimitResult(
            boolean allowed,
            int remainingRequests,
            long resetInMs,
            String errorMessage) {
        public static RateLimitResult allowed(int remaining, long resetIn) {
            return new RateLimitResult(true, remaining, resetIn, null);
        }

        public static RateLimitResult denied(int remaining, long resetIn) {
            return new RateLimitResult(false, remaining, resetIn,
                    "Rate limit exceeded. Try again in " + (resetIn / 1000) + " seconds.");
        }
    }

    /**
     * Rate limit status DTO for API response.
     */
    public record RateLimitStatus(
            int remainingRequests,
            int userLimitPerMinute,
            int ipLimitPerMinute,
            long resetWindowMs) {
    }
}
