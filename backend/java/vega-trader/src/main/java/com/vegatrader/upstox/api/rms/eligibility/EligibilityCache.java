package com.vegatrader.upstox.api.rms.eligibility;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine-backed cache for product eligibility.
 * 
 * <p>
 * Features:
 * <ul>
 * <li>100K max entries</li>
 * <li>60 second TTL</li>
 * <li>No DB hit on order placement</li>
 * </ul>
 * 
 * @since 4.1.0
 */
@Component
public class EligibilityCache {

    private static final Logger logger = LoggerFactory.getLogger(EligibilityCache.class);
    private static final int MAX_SIZE = 100_000;
    private static final int TTL_SECONDS = 60;

    private final LoadingCache<String, ProductEligibility> cache;
    private final EligibilityResolver resolver;

    public EligibilityCache(EligibilityResolver resolver) {
        this.resolver = resolver;
        this.cache = Caffeine.newBuilder()
                .maximumSize(MAX_SIZE)
                .expireAfterWrite(TTL_SECONDS, TimeUnit.SECONDS)
                .recordStats()
                .build(this::load);

        logger.info("EligibilityCache initialized: maxSize={}, ttl={}s", MAX_SIZE, TTL_SECONDS);
    }

    private ProductEligibility load(String instrumentKey) {
        logger.debug("Cache miss, loading eligibility for: {}", instrumentKey);
        return resolver.resolve(instrumentKey);
    }

    /**
     * Gets eligibility from cache (or loads if missing).
     */
    public ProductEligibility getEligibility(String instrumentKey) {
        return cache.get(instrumentKey);
    }

    /**
     * Invalidates a specific key.
     */
    public void invalidate(String instrumentKey) {
        cache.invalidate(instrumentKey);
    }

    /**
     * Invalidates all cached entries.
     */
    public void invalidateAll() {
        cache.invalidateAll();
        logger.info("All eligibility cache entries invalidated");
    }

    /**
     * Gets cache stats for monitoring.
     */
    public String getStats() {
        var stats = cache.stats();
        return String.format("hits=%d, misses=%d, hitRate=%.2f%%, size=%d",
                stats.hitCount(), stats.missCount(),
                stats.hitRate() * 100, cache.estimatedSize());
    }

    /**
     * Gets cache size.
     */
    public long size() {
        return cache.estimatedSize();
    }
}
