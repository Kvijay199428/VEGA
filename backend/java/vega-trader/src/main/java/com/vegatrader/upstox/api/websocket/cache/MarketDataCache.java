package com.vegatrader.upstox.api.websocket.cache;

import com.vegatrader.upstox.api.websocket.MarketUpdateV3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * In-memory cache for market data updates.
 * 
 * <p>
 * Provides TTL-based caching with automatic cleanup.
 * 
 * @since 3.0.0
 */
public class MarketDataCache {

    private final Map<String, CacheEntry> cache;
    private final int ttlSeconds;
    private final int maxSize;
    private final ScheduledExecutorService cleanupScheduler;
    private boolean enabled;

    public MarketDataCache(int ttlSeconds, int maxSize, boolean enabled) {
        this.cache = new ConcurrentHashMap<>();
        this.ttlSeconds = ttlSeconds;
        this.maxSize = maxSize;
        this.enabled = enabled;

        // Schedule cleanup every minute
        this.cleanupScheduler = Executors.newSingleThreadScheduledExecutor();
        this.cleanupScheduler.scheduleAtFixedRate(
                this::cleanup, 60, 60, TimeUnit.SECONDS);
    }

    public MarketDataCache() {
        this(60, 10000, true);
    }

    /**
     * Puts an update in the cache.
     * 
     * @param instrumentKey the instrument key
     * @param update        the market update
     */
    public void put(String instrumentKey, MarketUpdateV3 update) {
        if (!enabled) {
            return;
        }

        // Check size limit
        if (cache.size() >= maxSize) {
            // Remove oldest entry
            removeOldest();
        }

        cache.put(instrumentKey, new CacheEntry(update, System.currentTimeMillis()));
    }

    /**
     * Gets an update from the cache.
     * 
     * @param instrumentKey the instrument key
     * @return the cached update, or null if not found or expired
     */
    public MarketUpdateV3 get(String instrumentKey) {
        if (!enabled) {
            return null;
        }

        CacheEntry entry = cache.get(instrumentKey);
        if (entry == null) {
            return null;
        }

        // Check if expired
        if (isExpired(entry)) {
            cache.remove(instrumentKey);
            return null;
        }

        return entry.update;
    }

    /**
     * Checks if cache contains a key.
     * 
     * @param instrumentKey the instrument key
     * @return true if cached and not expired
     */
    public boolean contains(String instrumentKey) {
        return get(instrumentKey) != null;
    }

    /**
     * Removes an entry from the cache.
     * 
     * @param instrumentKey the instrument key
     */
    public void remove(String instrumentKey) {
        cache.remove(instrumentKey);
    }

    /**
     * Clears the entire cache.
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Gets the current cache size.
     * 
     * @return number of cached entries
     */
    public int size() {
        return cache.size();
    }

    /**
     * Enables or disables the cache.
     * 
     * @param enabled true to enable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            clear();
        }
    }

    /**
     * Checks if cache is enabled.
     * 
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Shuts down the cache and cleanup scheduler.
     */
    public void shutdown() {
        cleanupScheduler.shutdownNow();
        clear();
    }

    // Private helper methods

    private boolean isExpired(CacheEntry entry) {
        long age = System.currentTimeMillis() - entry.timestamp;
        return age > (ttlSeconds * 1000L);
    }

    private void removeOldest() {
        String oldestKey = null;
        long oldestTime = Long.MAX_VALUE;

        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            if (entry.getValue().timestamp < oldestTime) {
                oldestTime = entry.getValue().timestamp;
                oldestKey = entry.getKey();
            }
        }

        if (oldestKey != null) {
            cache.remove(oldestKey);
        }
    }

    private void cleanup() {
        cache.entrySet().removeIf(entry -> isExpired(entry.getValue()));
    }

    /**
     * Cache entry with timestamp.
     */
    private static class CacheEntry {
        final MarketUpdateV3 update;
        final long timestamp;

        CacheEntry(MarketUpdateV3 update, long timestamp) {
            this.update = update;
            this.timestamp = timestamp;
        }
    }
}
