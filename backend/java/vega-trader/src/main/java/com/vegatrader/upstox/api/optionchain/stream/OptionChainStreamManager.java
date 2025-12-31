package com.vegatrader.upstox.api.optionchain.stream;

import com.vegatrader.analytics.valuation.OptionChainValuationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Option Chain Stream Manager - manages authoritative streams.
 * Per websocket/a1.md section 2.
 * 
 * Single source of truth for all option chain state.
 * 
 * @since 4.8.0
 */
@Service
public class OptionChainStreamManager {

    private static final Logger logger = LoggerFactory.getLogger(OptionChainStreamManager.class);

    // streamKey → OptionChainFeedStreamV3
    private final Map<String, OptionChainFeedStreamV3> streams = new ConcurrentHashMap<>();
    private final OptionChainValuationService valuationService;

    public OptionChainStreamManager(OptionChainValuationService valuationService) {
        this.valuationService = valuationService;
    }

    /**
     * Get or create a stream for underlying + expiry.
     */
    public OptionChainFeedStreamV3 getOrCreateStream(String underlyingKey, String expiry) {
        String streamKey = underlyingKey + "|" + expiry;

        return streams.computeIfAbsent(streamKey, k -> {
            logger.info("Creating new stream: {}", streamKey);
            return new OptionChainFeedStreamV3(underlyingKey, LocalDate.parse(expiry));
        });
    }

    /**
     * Get existing stream.
     */
    public OptionChainFeedStreamV3 getStream(String underlyingKey, String expiry) {
        String streamKey = underlyingKey + "|" + expiry;
        return streams.get(streamKey);
    }

    /**
     * Update stream with new data and return sequence number.
     */
    public long updateStream(String underlyingKey, String expiry,
            Map<Integer, OptionChainFeedStreamV3.StrikeNode> strikes) {

        // Enrich with valuation
        var enrichedStrikes = valuationService.enrich(strikes, LocalDate.parse(expiry),
                com.vegatrader.analytics.valuation.ValuationSettings.defaults());

        OptionChainFeedStreamV3 stream = getOrCreateStream(underlyingKey, expiry);
        stream.loadSnapshot(enrichedStrikes);

        logger.debug("Updated stream {} with {} strikes (enriched), seq={}", stream.getStreamKey(),
                enrichedStrikes.size(), stream.getSequenceNumber());

        return stream.getSequenceNumber();
    }

    /**
     * Get all active streams.
     */
    public Map<String, OptionChainFeedStreamV3> getAllStreams() {
        return Map.copyOf(streams);
    }

    /**
     * Get active stream count.
     */
    public int getActiveStreamCount() {
        return streams.size();
    }

    /**
     * Remove expired streams.
     */
    public void removeExpiredStreams() {
        LocalDate today = LocalDate.now();

        streams.entrySet().removeIf(entry -> {
            LocalDate expiry = entry.getValue().getExpiry();
            boolean expired = expiry.isBefore(today);
            if (expired) {
                logger.info("Removing expired stream: {}", entry.getKey());
            }
            return expired;
        });
    }

    /**
     * Clear all streams.
     */
    public void clearAllStreams() {
        streams.clear();
        logger.info("Cleared all streams");
    }
}
