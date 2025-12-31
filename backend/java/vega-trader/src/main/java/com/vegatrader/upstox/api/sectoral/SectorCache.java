package com.vegatrader.upstox.api.sectoral;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe cache for sectoral index data with TTL (Time-To-Live).
 * <p>
 * This cache stores fetched sector data in memory to avoid repeated downloads
 * from NSE. Each cache entry has a 24-hour TTL by default.
 * </p>
 * <p>
 * <b>Usage Example:</b>
 * 
 * <pre>{@code
 * SectorCache cache = new SectorCache();
 * SectorDataFetcher fetcher = new SectorDataFetcher();
 * 
 * // Try to get from cache, fetch if not present
 * List<SectorConstituent> bankStocks = cache.getOrFetch(
 *         SectoralIndex.BANK,
 *         () -> fetcher.fetchSectorData(SectoralIndex.BANK));
 * }</pre>
 * </p>
 *
 * @since 2.0.0
 */
public class SectorCache {

    private static final Logger logger = LoggerFactory.getLogger(SectorCache.class);
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    private final Map<SectoralIndex, CacheEntry> cache = new ConcurrentHashMap<>();
    private final Duration ttl;

    /**
     * Creates a new sector cache with default 24-hour TTL.
     */
    public SectorCache() {
        this(DEFAULT_TTL);
    }

    /**
     * Creates a new sector cache with custom TTL.
     *
     * @param ttl the time-to-live for cache entries
     */
    public SectorCache(Duration ttl) {
        this.ttl = ttl;
        logger.info("SectorCache initialized with TTL: {}", ttl);
    }

    /**
     * Gets sector data from cache if available and not expired.
     *
     * @param sector the sectoral index
     * @return the cached data, or null if not cached or expired
     */
    public List<SectorConstituent> get(SectoralIndex sector) {
        CacheEntry entry = cache.get(sector);

        if (entry == null) {
            logger.debug("Cache MISS for {}", sector.getSectorKey());
            return null;
        }

        if (entry.isExpired()) {
            logger.info("Cache entry EXPIRED for {}", sector.getSectorKey());
            cache.remove(sector);
            return null;
        }

        logger.debug("Cache HIT for {} (age: {})", sector.getSectorKey(),
                Duration.between(entry.timestamp, Instant.now()));
        return entry.data;
    }

    /**
     * Stores sector data in cache.
     *
     * @param sector the sectoral index
     * @param data   the sector data to cache
     */
    public void put(SectoralIndex sector, List<SectorConstituent> data) {
        if (data == null || data.isEmpty()) {
            logger.warn("Refusing to cache null or empty data for {}", sector.getSectorKey());
            return;
        }

        CacheEntry entry = new CacheEntry(data, Instant.now(), ttl);
        cache.put(sector, entry);

        logger.info("Cached {} constituents for {} (expires in {})",
                data.size(), sector.getSectorKey(), ttl);
    }

    /**
     * Gets sector data from cache or fetches it using the provided fetcher.
     * <p>
     * This is a convenience method that implements the common cache-aside pattern.
     * </p>
     *
     * @param sector  the sectoral index
     * @param fetcher the fetcher function to use if cache miss
     * @return the sector data
     * @throws SectorDataFetcher.SectorDataException if fetching fails
     */
    public List<SectorConstituent> getOrFetch(SectoralIndex sector,
            SectorFetcher fetcher)
            throws SectorDataFetcher.SectorDataException {
        // Try cache first
        List<SectorConstituent> cached = get(sector);
        if (cached != null) {
            return cached;
        }

        // Cache miss - fetch and store
        logger.info("Fetching sector data for {} (cache miss)", sector.getSectorKey());
        List<SectorConstituent> data = fetcher.fetch();
        put(sector, data);
        return data;
    }

    /**
     * Invalidates (removes) cache entry for a sector.
     *
     * @param sector the sectoral index
     * @return true if entry was removed, false if not cached
     */
    public boolean invalidate(SectoralIndex sector) {
        boolean removed = cache.remove(sector) != null;
        if (removed) {
            logger.info("Invalidated cache for {}", sector.getSectorKey());
        }
        return removed;
    }

    /**
     * Clears all cache entries.
     */
    public void clearAll() {
        int size = cache.size();
        cache.clear();
        logger.info("Cleared all cache entries ({})", size);
    }

    /**
     * Gets the number of cached sectors.
     *
     * @return the cache size
     */
    public int size() {
        return cache.size();
    }

    /**
     * Removes all expired entries from cache.
     *
     * @return the number of entries removed
     */
    public int cleanupExpired() {
        int removed = 0;

        for (Map.Entry<SectoralIndex, CacheEntry> entry : cache.entrySet()) {
            if (entry.getValue().isExpired()) {
                cache.remove(entry.getKey());
                removed++;
            }
        }

        if (removed > 0) {
            logger.info("Cleaned up {} expired cache entries", removed);
        }

        return removed;
    }

    /**
     * Gets cache statistics.
     *
     * @return cache statistics string
     */
    public String getStatistics() {
        int total = cache.size();
        long expired = cache.values().stream()
                .filter(CacheEntry::isExpired)
                .count();

        return String.format("SectorCache{total=%d, expired=%d, active=%d, ttl=%s}",
                total, expired, total - expired, ttl);
    }

    /**
     * Functional interface for sector data fetching.
     */
    @FunctionalInterface
    public interface SectorFetcher {
        List<SectorConstituent> fetch() throws SectorDataFetcher.SectorDataException;
    }

    /**
     * Internal cache entry with timestamp and TTL.
     */
    private static class CacheEntry {
        private final List<SectorConstituent> data;
        private final Instant timestamp;
        private final Duration ttl;

        CacheEntry(List<SectorConstituent> data, Instant timestamp, Duration ttl) {
            this.data = data;
            this.timestamp = timestamp;
            this.ttl = ttl;
        }

        boolean isExpired() {
            return Instant.now().isAfter(timestamp.plus(ttl));
        }
    }
}
