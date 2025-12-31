package com.vegatrader.upstox.api.websocket.health;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Health flags for tracking service availability.
 * 
 * <p>
 * Used to implement fallback chain:
 * 
 * <pre>
 * Redis (HOT) → DB (COLD) → Filesystem (FALLBACK)
 * </pre>
 * 
 * <p>
 * Thread-safe singleton for global health tracking.
 * 
 * @since 3.1.0
 */
public class HealthFlags {

    private static final AtomicBoolean redisUp = new AtomicBoolean(true);
    private static final AtomicBoolean dbUp = new AtomicBoolean(true);

    // Private constructor - utility class
    private HealthFlags() {
    }

    /**
     * Marks Redis as down.
     */
    public static void setRedisDown() {
        redisUp.set(false);
    }

    /**
     * Marks Redis as up.
     */
    public static void setRedisUp() {
        redisUp.set(true);
    }

    /**
     * Checks if Redis is up.
     * 
     * @return true if Redis is available
     */
    public static boolean redisUp() {
        return redisUp.get();
    }

    /**
     * Marks DB as down.
     */
    public static void setDbDown() {
        dbUp.set(false);
    }

    /**
     * Marks DB as up.
     */
    public static void setDbUp() {
        dbUp.set(true);
    }

    /**
     * Checks if DB is up.
     * 
     * @return true if DB is available
     */
    public static boolean dbUp() {
        return dbUp.get();
    }

    /**
     * Checks if both Redis and DB are down (filesystem fallback needed).
     * 
     * @return true if both persistence layers are unavailable
     */
    public static boolean filesystemFallbackNeeded() {
        return !redisUp.get() && !dbUp.get();
    }

    /**
     * Resets all health flags to UP (for testing/recovery).
     */
    public static void resetAll() {
        redisUp.set(true);
        dbUp.set(true);
    }

    /**
     * Gets current health status summary.
     * 
     * @return health status string
     */
    public static String getStatus() {
        return String.format("Redis: %s, DB: %s, Fallback: %s",
                redisUp.get() ? "UP" : "DOWN",
                dbUp.get() ? "UP" : "DOWN",
                filesystemFallbackNeeded() ? "ACTIVE" : "INACTIVE");
    }
}
