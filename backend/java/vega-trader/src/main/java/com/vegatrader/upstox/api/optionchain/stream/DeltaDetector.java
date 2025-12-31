package com.vegatrader.upstox.api.optionchain.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

/**
 * Delta Detection Engine - detects field-level changes.
 * Per websocket/a1.md section 4.
 * 
 * Only emit fields that changed since last sequence.
 * 
 * @since 4.8.0
 */
@Component
public class DeltaDetector {

    private static final Logger logger = LoggerFactory.getLogger(DeltaDetector.class);

    // Cache of last known state per instrument key
    private final Map<String, CachedState> stateCache = new ConcurrentHashMap<>();

    /**
     * Detect delta between previous and new leg state.
     */
    public Optional<WsMessage.Delta> detectDelta(
            long seq,
            int strike,
            String leg,
            String instrumentKey,
            OptionChainFeedStreamV3.OptionLeg newLeg) {

        String cacheKey = instrumentKey + "|" + leg;
        CachedState previous = stateCache.get(cacheKey);

        Map<String, Object> changedFields = new LinkedHashMap<>();

        if (previous == null) {
            // First time - all fields are new
            changedFields.putAll(extractAllFields(newLeg));
        } else {
            // Compare with previous
            changedFields.putAll(compareFields(previous.leg(), newLeg));
        }

        if (changedFields.isEmpty()) {
            return Optional.empty();
        }

        // Update cache
        stateCache.put(cacheKey, new CachedState(newLeg, Instant.now()));

        logger.debug("Delta detected: {} fields changed for {}", changedFields.size(), instrumentKey);

        return Optional.of(new WsMessage.Delta(
                seq,
                strike,
                leg,
                instrumentKey,
                changedFields,
                Instant.now()));
    }

    /**
     * Compare fields and return only changed ones.
     */
    private Map<String, Object> compareFields(
            OptionChainFeedStreamV3.OptionLeg previous,
            OptionChainFeedStreamV3.OptionLeg current) {

        Map<String, Object> changes = new LinkedHashMap<>();

        if (previous.marketData() == null || current.marketData() == null) {
            return extractAllFields(current);
        }

        var prevMd = previous.marketData();
        var currMd = current.marketData();

        // Market data comparisons
        if (prevMd.ltp() != currMd.ltp()) {
            changes.put("market_data.ltp", currMd.ltp());
        }
        if (prevMd.oi() != currMd.oi()) {
            changes.put("market_data.oi", currMd.oi());
        }
        if (prevMd.volume() != currMd.volume()) {
            changes.put("market_data.volume", currMd.volume());
        }
        if (prevMd.bidPrice() != currMd.bidPrice()) {
            changes.put("market_data.bid_price", currMd.bidPrice());
        }
        if (prevMd.bidQty() != currMd.bidQty()) {
            changes.put("market_data.bid_qty", currMd.bidQty());
        }
        if (prevMd.askPrice() != currMd.askPrice()) {
            changes.put("market_data.ask_price", currMd.askPrice());
        }
        if (prevMd.askQty() != currMd.askQty()) {
            changes.put("market_data.ask_qty", currMd.askQty());
        }

        // Greeks comparisons
        if (previous.greeks() != null && current.greeks() != null) {
            var prevG = previous.greeks();
            var currG = current.greeks();

            if (prevG.iv() != currG.iv()) {
                changes.put("greeks.iv", currG.iv());
            }
            if (prevG.delta() != currG.delta()) {
                changes.put("greeks.delta", currG.delta());
            }
            if (prevG.theta() != currG.theta()) {
                changes.put("greeks.theta", currG.theta());
            }
            if (prevG.gamma() != currG.gamma()) {
                changes.put("greeks.gamma", currG.gamma());
            }
            if (prevG.vega() != currG.vega()) {
                changes.put("greeks.vega", currG.vega());
            }
        }

        return changes;
    }

    /**
     * Extract all fields for initial snapshot or full update.
     */
    private Map<String, Object> extractAllFields(OptionChainFeedStreamV3.OptionLeg leg) {
        Map<String, Object> fields = new LinkedHashMap<>();

        if (leg.marketData() != null) {
            var md = leg.marketData();
            fields.put("market_data.ltp", md.ltp());
            fields.put("market_data.oi", md.oi());
            fields.put("market_data.volume", md.volume());
            fields.put("market_data.bid_price", md.bidPrice());
            fields.put("market_data.bid_qty", md.bidQty());
            fields.put("market_data.ask_price", md.askPrice());
            fields.put("market_data.ask_qty", md.askQty());
        }

        if (leg.greeks() != null) {
            var g = leg.greeks();
            fields.put("greeks.iv", g.iv());
            fields.put("greeks.delta", g.delta());
            fields.put("greeks.theta", g.theta());
            fields.put("greeks.gamma", g.gamma());
            fields.put("greeks.vega", g.vega());
        }

        return fields;
    }

    /**
     * Clear cache for an instrument.
     */
    public void clearCache(String instrumentKey) {
        stateCache.keySet().removeIf(k -> k.startsWith(instrumentKey));
    }

    /**
     * Clear all cache.
     */
    public void clearAllCache() {
        stateCache.clear();
    }

    private record CachedState(
            OptionChainFeedStreamV3.OptionLeg leg,
            Instant cachedAt) {
    }
}
