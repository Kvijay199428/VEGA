package com.vegatrader.market.cache;

import com.vegatrader.market.dto.LiveMarketSnapshot;
import com.vegatrader.market.dto.OrderBookSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory hot cache for live market data.
 * Provides O(1) access to latest ticks and order books.
 * 
 * Redis-ready: Can be swapped with Redis implementation later.
 */
@Service
public class MarketCacheService {

    private static final Logger logger = LoggerFactory.getLogger(MarketCacheService.class);

    /** Tick cache: instrumentKey -> LiveMarketSnapshot */
    private final Map<String, LiveMarketSnapshot> tickCache = new ConcurrentHashMap<>();

    /** Depth cache: instrumentKey -> OrderBookSnapshot */
    private final Map<String, OrderBookSnapshot> depthCache = new ConcurrentHashMap<>();

    /**
     * Update tick cache with new snapshot.
     * 
     * @param snapshot Live market snapshot
     */
    public void updateTick(LiveMarketSnapshot snapshot) {
        if (snapshot == null || snapshot.getInstrumentKey() == null)
            return;
        tickCache.put(snapshot.getInstrumentKey(), snapshot);
        logger.trace("Cache updated: {} LTP={}", snapshot.getInstrumentKey(), snapshot.getLtp());
    }

    /**
     * Update depth cache with new snapshot.
     * 
     * @param snapshot Order book snapshot
     */
    public void updateDepth(OrderBookSnapshot snapshot) {
        if (snapshot == null || snapshot.getInstrumentKey() == null)
            return;
        depthCache.put(snapshot.getInstrumentKey(), snapshot);
    }

    /**
     * Get latest tick for instrument.
     * 
     * @param instrumentKey Instrument key
     * @return Latest snapshot or null
     */
    public LiveMarketSnapshot getTick(String instrumentKey) {
        return tickCache.get(instrumentKey);
    }

    /**
     * Get latest depth for instrument.
     * 
     * @param instrumentKey Instrument key
     * @return Latest depth or null
     */
    public OrderBookSnapshot getDepth(String instrumentKey) {
        return depthCache.get(instrumentKey);
    }

    /**
     * Get all cached ticks.
     */
    public Collection<LiveMarketSnapshot> getAllTicks() {
        return tickCache.values();
    }

    /**
     * Get all cached depths.
     */
    public Collection<OrderBookSnapshot> getAllDepths() {
        return depthCache.values();
    }

    /**
     * Check if instrument is in cache.
     */
    public boolean hasTick(String instrumentKey) {
        return tickCache.containsKey(instrumentKey);
    }

    /**
     * Remove instrument from cache (on unsubscribe).
     */
    public void remove(String instrumentKey) {
        tickCache.remove(instrumentKey);
        depthCache.remove(instrumentKey);
    }

    /**
     * Clear all cached data.
     */
    public void clear() {
        tickCache.clear();
        depthCache.clear();
        logger.info("Market cache cleared");
    }

    /**
     * Get cache size.
     */
    public int size() {
        return tickCache.size();
    }
}
