package com.vegatrader.upstox.api.websocket.persistence;

import com.vegatrader.upstox.api.websocket.health.HealthFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Base64;

/**
 * Redis snapshot handler for hot storage.
 * 
 * <p>
 * Responsibility:
 * <ul>
 * <li>Store latest snapshot per instrument</li>
 * <li>Fast recovery after reconnect</li>
 * <li>TTL-based expiry (3:30 AM cutoff)</li>
 * </ul>
 * 
 * <p>
 * Only active if Redis is configured. Gracefully degrades if disabled.
 * 
 * @since 3.1.0
 */
@Component
@ConditionalOnProperty(name = "spring.data.redis.host")
public class RedisSnapshotHandler {

    private static final Logger logger = LoggerFactory.getLogger(RedisSnapshotHandler.class);
    private static final ZoneId INDIA_ZONE = ZoneId.of("Asia/Kolkata");
    private static final String KEY_PREFIX = "md:";

    private final StringRedisTemplate redisTemplate;

    @Autowired(required = false)
    public RedisSnapshotHandler(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        if (redisTemplate != null) {
            logger.info("RedisSnapshotHandler initialized with Redis connection");
        } else {
            logger.warn("Redis not available - RedisSnapshotHandler disabled");
        }
    }

    /**
     * Stores a market data snapshot in Redis.
     * 
     * @param instrumentKey the instrument key
     * @param payload       the serialized market data
     * @param ttlSeconds    TTL in seconds (until 3:30 AM)
     */
    public void storeSnapshot(String instrumentKey, byte[] payload, long ttlSeconds) {
        if (redisTemplate == null) {
            logger.trace("Redis disabled - skipping snapshot storage");
            return;
        }

        try {
            String key = KEY_PREFIX + instrumentKey;
            String encodedValue = Base64.getEncoder().encodeToString(payload);

            // Store with TTL
            redisTemplate.opsForValue().set(key, encodedValue, Duration.ofSeconds(ttlSeconds));
            logger.trace("Stored snapshot for {} in Redis with TTL {}s", instrumentKey, ttlSeconds);

            HealthFlags.setRedisUp();

        } catch (Exception e) {
            logger.error("Failed to store snapshot in Redis: {}", e.getMessage());
            HealthFlags.setRedisDown();
        }
    }

    /**
     * Retrieves a snapshot from Redis.
     * 
     * @param instrumentKey the instrument key
     * @return the snapshot data, or null if not found
     */
    public byte[] getSnapshot(String instrumentKey) {
        if (redisTemplate == null) {
            return null;
        }

        try {
            String key = KEY_PREFIX + instrumentKey;
            String encodedValue = redisTemplate.opsForValue().get(key);

            if (encodedValue != null) {
                logger.trace("Retrieved snapshot for {} from Redis", instrumentKey);
                HealthFlags.setRedisUp();
                return Base64.getDecoder().decode(encodedValue);
            }

            HealthFlags.setRedisUp();
            return null;

        } catch (Exception e) {
            logger.error("Failed to retrieve snapshot from Redis: {}", e.getMessage());
            HealthFlags.setRedisDown();
            return null;
        }
    }

    /**
     * Calculates TTL until 3:30 AM next day (IST).
     * 
     * @return seconds until 3:30 AM
     */
    public static long ttlUntil330AM() {
        LocalDateTime now = LocalDateTime.now(INDIA_ZONE);
        LocalDateTime cutoff = now.toLocalDate().atTime(LocalTime.of(3, 30));

        // If past 3:30 AM today, target 3:30 AM tomorrow
        if (now.isAfter(cutoff)) {
            cutoff = cutoff.plusDays(1);
        }

        return Duration.between(now, cutoff).getSeconds();
    }
}
