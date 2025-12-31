package com.vegatrader.upstox.api.optionchain.stream;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * OptionChainFeedStreamV3 - Core authoritative option chain state.
 * Per websocket/a1.md section 3.
 * 
 * This structure is ALWAYS complete and never partial.
 * All updates modify this object first, then propagate outward.
 * 
 * @since 4.8.0
 */
public class OptionChainFeedStreamV3 {

    private final String underlyingKey;
    private final LocalDate expiry;
    private final AtomicLong sequenceNumber = new AtomicLong(0);
    private volatile Instant lastUpdated = Instant.now();

    // strike_price → StrikeNode
    private final Map<Integer, StrikeNode> strikes = new ConcurrentHashMap<>();

    public OptionChainFeedStreamV3(String underlyingKey, LocalDate expiry) {
        this.underlyingKey = underlyingKey;
        this.expiry = expiry;
    }

    // === Core State Access ===

    public String getUnderlyingKey() {
        return underlyingKey;
    }

    public LocalDate getExpiry() {
        return expiry;
    }

    public long getSequenceNumber() {
        return sequenceNumber.get();
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public Map<Integer, StrikeNode> getStrikes() {
        return strikes;
    }

    // === Mutation with Sequence ===

    public synchronized long updateStrike(int strikePrice, StrikeNode node) {
        strikes.put(strikePrice, node);
        lastUpdated = Instant.now();
        return sequenceNumber.incrementAndGet();
    }

    public synchronized long updateLeg(int strikePrice, String legType, OptionLeg leg) {
        StrikeNode node = strikes.computeIfAbsent(strikePrice,
                k -> new StrikeNode(strikePrice, null, null));

        if ("CALL".equalsIgnoreCase(legType)) {
            strikes.put(strikePrice, new StrikeNode(strikePrice, leg, node.put()));
        } else {
            strikes.put(strikePrice, new StrikeNode(strikePrice, node.call(), leg));
        }

        lastUpdated = Instant.now();
        return sequenceNumber.incrementAndGet();
    }

    public synchronized void loadSnapshot(Map<Integer, StrikeNode> snapshot) {
        strikes.clear();
        strikes.putAll(snapshot);
        lastUpdated = Instant.now();
        sequenceNumber.incrementAndGet();
    }

    // === Key Generation ===

    public String getStreamKey() {
        return underlyingKey + "|" + expiry;
    }

    // === Nested Records ===

    /**
     * Strike node containing call and put legs.
     */
    public record StrikeNode(
            int strikePrice,
            OptionLeg call,
            OptionLeg put) {
    }

    /**
     * Option leg with market data and greeks.
     */
    public record OptionLeg(
            String instrumentKey,
            MarketData marketData,
            OptionGreeks greeks,
            com.vegatrader.analytics.valuation.ValuationResult valuation) {
    }

    /**
     * Market data fields.
     */
    public record MarketData(
            double ltp,
            double closePrice,
            long volume,
            long oi,
            double bidPrice,
            int bidQty,
            double askPrice,
            int askQty,
            long prevOi) {
    }

    /**
     * Option greeks.
     */
    public record OptionGreeks(
            double delta,
            double gamma,
            double theta,
            double vega,
            double iv,
            double pop) {
    }
}
