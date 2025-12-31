package com.vegatrader.upstox.api.websocket.cache;

import com.vegatrader.upstox.api.response.websocket.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Timestamp-aware cache for portfolio data with last-write-wins semantics.
 * 
 * <p>
 * ⚠️ CRITICAL: This cache uses timestamp-based correctness, NOT TTL-based.
 * 
 * <p>
 * Key characteristics:
 * <ul>
 * <li>Separate caches for orders, holdings, positions, GTT</li>
 * <li>Last-write-wins based on timestamp comparison</li>
 * <li>Out-of-order updates are rejected and logged</li>
 * <li>TTL is for cleanup only, not correctness</li>
 * <li>Thread-safe</li>
 * </ul>
 * 
 * @since 2.0.0
 */
public class PortfolioDataCache {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioDataCache.class);

    private final Map<String, TimestampedValue<OrderUpdate>> orders;
    private final Map<String, TimestampedValue<HoldingUpdate>> holdings;
    private final Map<String, TimestampedValue<PositionUpdate>> positions;
    private final Map<String, TimestampedValue<GttUpdate>> gtt;

    private final int ttlSeconds;
    private final int maxSize;
    private final boolean enabled;
    private final ScheduledExecutorService cleanupExecutor;

    // Metrics
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    private final AtomicLong outOfOrderRejections = new AtomicLong(0);

    /**
     * Creates cache with default settings (enabled, 3600s TTL, 10000 max size).
     */
    public PortfolioDataCache() {
        this(3600, 10000, true);
    }

    /**
     * Creates cache with specified settings.
     * 
     * @param ttlSeconds time-to-live for cleanup (NOT correctness)
     * @param maxSize    maximum entries per cache type
     * @param enabled    whether caching is enabled
     */
    public PortfolioDataCache(int ttlSeconds, int maxSize, boolean enabled) {
        this.ttlSeconds = ttlSeconds;
        this.maxSize = maxSize;
        this.enabled = enabled;

        this.orders = new ConcurrentHashMap<>();
        this.holdings = new ConcurrentHashMap<>();
        this.positions = new ConcurrentHashMap<>();
        this.gtt = new ConcurrentHashMap<>();

        if (enabled) {
            // Start cleanup thread - runs every TTL/2 seconds
            this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r);
                t.setName("PortfolioCache-Cleanup");
                t.setDaemon(true);
                return t;
            });

            cleanupExecutor.scheduleAtFixedRate(
                    this::cleanupExpired,
                    ttlSeconds / 2,
                    ttlSeconds / 2,
                    TimeUnit.SECONDS);

            logger.info("PortfolioDataCache initialized: ttl={}s, maxSize={}", ttlSeconds, maxSize);
        } else {
            this.cleanupExecutor = null;
            logger.info("PortfolioDataCache disabled");
        }
    }

    // Order cache methods

    /**
     * Puts an order update in cache with timestamp validation.
     * 
     * <p>
     * ⚠️ CRITICAL: Rejects out-of-order updates based on timestamp.
     * 
     * @param update the order update
     */
    public void putOrder(OrderUpdate update) {
        if (!enabled || update == null || update.getOrderId() == null) {
            return;
        }

        String key = update.getOrderId();
        long timestamp = update.getTimestamp() != null ? update.getTimestamp() : System.currentTimeMillis();

        TimestampedValue<OrderUpdate> cached = orders.get(key);

        // Reject out-of-order updates
        if (cached != null && timestamp < cached.timestamp) {
            long rejections = outOfOrderRejections.incrementAndGet();
            if (rejections % 100 == 0) {
                logger.warn(
                        "Rejecting out-of-order order update: orderId={}, incoming={}, cached={} (total rejections: {})",
                        key, timestamp, cached.timestamp, rejections);
            }
            return; // IGNORE
        }

        // Last-write-wins based on timestamp
        orders.put(key, new TimestampedValue<>(update, timestamp));
        enforceSizeLimit(orders, maxSize);
    }

    /**
     * Gets an order update from cache.
     * 
     * @param orderId the order ID
     * @return the order update, or null if not found
     */
    public OrderUpdate getOrder(String orderId) {
        if (!enabled || orderId == null) {
            return null;
        }

        TimestampedValue<OrderUpdate> cached = orders.get(orderId);
        if (cached != null) {
            hitCount.incrementAndGet();
            return cached.value;
        }

        missCount.incrementAndGet();
        return null;
    }

    // Holding cache methods

    /**
     * Puts a holding update in cache with timestamp validation.
     * 
     * @param update the holding update
     */
    public void putHolding(HoldingUpdate update) {
        if (!enabled || update == null || update.getInstrumentKey() == null) {
            return;
        }

        String key = update.getInstrumentKey();
        long timestamp = update.getTimestamp() != null ? update.getTimestamp() : System.currentTimeMillis();

        TimestampedValue<HoldingUpdate> cached = holdings.get(key);

        if (cached != null && timestamp < cached.timestamp) {
            outOfOrderRejections.incrementAndGet();
            return; // IGNORE
        }

        holdings.put(key, new TimestampedValue<>(update, timestamp));
        enforceSizeLimit(holdings, maxSize);
    }

    /**
     * Gets a holding update from cache.
     * 
     * @param instrumentKey the instrument key
     * @return the holding update, or null if not found
     */
    public HoldingUpdate getHolding(String instrumentKey) {
        if (!enabled || instrumentKey == null) {
            return null;
        }

        TimestampedValue<HoldingUpdate> cached = holdings.get(instrumentKey);
        if (cached != null) {
            hitCount.incrementAndGet();
            return cached.value;
        }

        missCount.incrementAndGet();
        return null;
    }

    // Position cache methods

    /**
     * Puts a position update in cache with timestamp validation.
     * 
     * @param update the position update
     */
    public void putPosition(PositionUpdate update) {
        if (!enabled || update == null || update.getInstrumentKey() == null) {
            return;
        }

        // Key: instrumentKey + product
        String key = update.getInstrumentKey() + ":" + update.getProduct();
        long timestamp = update.getTimestamp() != null ? update.getTimestamp() : System.currentTimeMillis();

        TimestampedValue<PositionUpdate> cached = positions.get(key);

        if (cached != null && timestamp < cached.timestamp) {
            outOfOrderRejections.incrementAndGet();
            return; // IGNORE
        }

        positions.put(key, new TimestampedValue<>(update, timestamp));
        enforceSizeLimit(positions, maxSize);
    }

    /**
     * Gets a position update from cache.
     * 
     * @param instrumentKey the instrument key
     * @param product       the product type
     * @return the position update, or null if not found
     */
    public PositionUpdate getPosition(String instrumentKey, String product) {
        if (!enabled || instrumentKey == null || product == null) {
            return null;
        }

        String key = instrumentKey + ":" + product;
        TimestampedValue<PositionUpdate> cached = positions.get(key);
        if (cached != null) {
            hitCount.incrementAndGet();
            return cached.value;
        }

        missCount.incrementAndGet();
        return null;
    }

    // GTT cache methods

    /**
     * Puts a GTT update in cache with timestamp validation.
     * 
     * @param update the GTT update
     */
    public void putGtt(GttUpdate update) {
        if (!enabled || update == null || update.getGttId() == null) {
            return;
        }

        String key = update.getGttId();
        long timestamp = update.getTimestamp() != null ? update.getTimestamp() : System.currentTimeMillis();

        TimestampedValue<GttUpdate> cached = gtt.get(key);

        if (cached != null && timestamp < cached.timestamp) {
            outOfOrderRejections.incrementAndGet();
            return; // IGNORE
        }

        gtt.put(key, new TimestampedValue<>(update, timestamp));
        enforceSizeLimit(gtt, maxSize);
    }

    /**
     * Gets a GTT update from cache.
     * 
     * @param gttId the GTT ID
     * @return the GTT update, or null if not found
     */
    public GttUpdate getGtt(String gttId) {
        if (!enabled || gttId == null) {
            return null;
        }

        TimestampedValue<GttUpdate> cached = gtt.get(gttId);
        if (cached != null) {
            hitCount.incrementAndGet();
            return cached.value;
        }

        missCount.incrementAndGet();
        return null;
    }

    // Utility methods

    /**
     * Clears all caches (for resync).
     */
    public void clear() {
        orders.clear();
        holdings.clear();
        positions.clear();
        gtt.clear();
        logger.info("Portfolio cache cleared");
    }

    /**
     * Gets cache statistics.
     * 
     * @return statistics object
     */
    public CacheStatistics getStatistics() {
        return new CacheStatistics(
                orders.size(),
                holdings.size(),
                positions.size(),
                gtt.size(),
                hitCount.get(),
                missCount.get(),
                outOfOrderRejections.get());
    }

    /**
     * Shuts down cleanup executor.
     */
    public void shutdown() {
        if (cleanupExecutor != null && !cleanupExecutor.isShutdown()) {
            logger.info("Shutting down cache cleanup executor");
            cleanupExecutor.shutdown();
        }
    }

    // Private helper methods

    private <T> void enforceSizeLimit(Map<String, TimestampedValue<T>> map, int limit) {
        if (map.size() > limit) {
            // Remove oldest entry (simple LRU approximation)
            String oldestKey = null;
            long oldestTime = Long.MAX_VALUE;

            for (Map.Entry<String, TimestampedValue<T>> entry : map.entrySet()) {
                if (entry.getValue().timestamp < oldestTime) {
                    oldestTime = entry.getValue().timestamp;
                    oldestKey = entry.getKey();
                }
            }

            if (oldestKey != null) {
                map.remove(oldestKey);
            }
        }
    }

    private void cleanupExpired() {
        try {
            long now = System.currentTimeMillis();
            long ttlMs = ttlSeconds * 1000L;

            int removed = 0;
            removed += cleanupMap(orders, now, ttlMs);
            removed += cleanupMap(holdings, now, ttlMs);
            removed += cleanupMap(positions, now, ttlMs);
            removed += cleanupMap(gtt, now, ttlMs);

            if (removed > 0) {
                logger.debug("Cleaned up {} expired cache entries", removed);
            }
        } catch (Exception e) {
            logger.error("Error during cache cleanup", e);
        }
    }

    private <T> int cleanupMap(Map<String, TimestampedValue<T>> map, long now, long ttlMs) {
        java.util.concurrent.atomic.AtomicInteger removed = new java.util.concurrent.atomic.AtomicInteger(0);
        map.entrySet().removeIf(entry -> {
            boolean expired = (now - entry.getValue().timestamp) > ttlMs;
            if (expired)
                removed.incrementAndGet();
            return expired;
        });
        return removed.get();
    }

    // Inner classes

    private static class TimestampedValue<T> {
        final T value;
        final long timestamp;

        TimestampedValue(T value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
    }

    public static class CacheStatistics {
        public final int orderCount;
        public final int holdingCount;
        public final int positionCount;
        public final int gttCount;
        public final long hitCount;
        public final long missCount;
        public final long outOfOrderRejections;

        CacheStatistics(int orderCount, int holdingCount, int positionCount, int gttCount,
                long hitCount, long missCount, long outOfOrderRejections) {
            this.orderCount = orderCount;
            this.holdingCount = holdingCount;
            this.positionCount = positionCount;
            this.gttCount = gttCount;
            this.hitCount = hitCount;
            this.missCount = missCount;
            this.outOfOrderRejections = outOfOrderRejections;
        }

        public double getHitRate() {
            long total = hitCount + missCount;
            return total > 0 ? (hitCount * 100.0 / total) : 0.0;
        }

        @Override
        public String toString() {
            return String.format(
                    "CacheStats{orders=%d, holdings=%d, positions=%d, gtt=%d, hits=%d, misses=%d, outOfOrder=%d, hitRate=%.1f%%}",
                    orderCount, holdingCount, positionCount, gttCount, hitCount, missCount, outOfOrderRejections,
                    getHitRate());
        }
    }
}
