package com.vegatrader.upstox.api.optionchain.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Latency tracker for option chain streaming.
 * Per websocket/b2.md section 7.
 * 
 * Tracks latency at each stage:
 * - Broker → Adapter
 * - Adapter → Feed
 * - Feed → Delta
 * - Delta → WebSocket
 * - WebSocket → Client
 * 
 * @since 4.8.0
 */
@Component
public class LatencyTracker {

    private static final Logger logger = LoggerFactory.getLogger(LatencyTracker.class);

    // Recent latency records (bounded)
    private final Deque<LatencyRecord> recentRecords = new java.util.concurrent.LinkedBlockingDeque<>(1000);

    // Aggregated metrics per instrument
    private final Map<String, AggregatedMetrics> metrics = new ConcurrentHashMap<>();

    /**
     * Record a latency measurement.
     */
    public void record(LatencyRecord record) {
        recentRecords.addFirst(record);
        if (recentRecords.size() > 1000) {
            recentRecords.removeLast();
        }

        // Update aggregated metrics
        String key = record.instrument() + "|" + record.expiry();
        metrics.compute(key, (k, existing) -> {
            if (existing == null) {
                return new AggregatedMetrics(1,
                        record.endToEndMs(), record.endToEndMs(), record.endToEndMs());
            }
            return existing.update(record.endToEndMs());
        });

        if (record.endToEndMs() > 30) {
            logger.warn("High latency detected: {}ms for {}", record.endToEndMs(), record.instrument());
        }
    }

    /**
     * Create a latency builder for a request.
     */
    public LatencyBuilder builder(String instrument, String expiry, int strike, String callPut) {
        return new LatencyBuilder(this, instrument, expiry, strike, callPut);
    }

    /**
     * Get recent latency records.
     */
    public List<LatencyRecord> getRecentRecords(int limit) {
        return recentRecords.stream().limit(limit).toList();
    }

    /**
     * Get aggregated metrics for an instrument.
     */
    public AggregatedMetrics getMetrics(String instrument, String expiry) {
        return metrics.get(instrument + "|" + expiry);
    }

    /**
     * Get all aggregated metrics.
     */
    public Map<String, AggregatedMetrics> getAllMetrics() {
        return Map.copyOf(metrics);
    }

    /**
     * Clear all metrics.
     */
    public void clearMetrics() {
        recentRecords.clear();
        metrics.clear();
    }

    /**
     * Latency record per b2.md section 7.2.
     */
    public record LatencyRecord(
            String instrument,
            String expiry,
            int strike,
            String callPut,
            long brokerToAdapterMs,
            long adapterToFeedMs,
            long feedToDeltaMs,
            long deltaToWsMs,
            long wsToClientMs,
            long endToEndMs,
            Instant timestamp) {
    }

    /**
     * Aggregated metrics per b2.md section 7.3.
     */
    public record AggregatedMetrics(
            long count,
            long minMs,
            long maxMs,
            long avgMs) {
        public AggregatedMetrics update(long newLatency) {
            long newCount = count + 1;
            long newMin = Math.min(minMs, newLatency);
            long newMax = Math.max(maxMs, newLatency);
            long newAvg = ((avgMs * count) + newLatency) / newCount;
            return new AggregatedMetrics(newCount, newMin, newMax, newAvg);
        }
    }

    /**
     * Builder for latency tracking through pipeline stages.
     */
    public static class LatencyBuilder {
        private final LatencyTracker tracker;
        private final String instrument;
        private final String expiry;
        private final int strike;
        private final String callPut;

        private long brokerRxTs;
        private long adapterParseTs;
        private long feedApplyTs;
        private long deltaComputeTs;
        private long wsSendTs;
        private long clientAckTs;

        LatencyBuilder(LatencyTracker tracker, String instrument, String expiry, int strike, String callPut) {
            this.tracker = tracker;
            this.instrument = instrument;
            this.expiry = expiry;
            this.strike = strike;
            this.callPut = callPut;
        }

        public LatencyBuilder brokerReceived() {
            this.brokerRxTs = System.currentTimeMillis();
            return this;
        }

        public LatencyBuilder adapterParsed() {
            this.adapterParseTs = System.currentTimeMillis();
            return this;
        }

        public LatencyBuilder feedApplied() {
            this.feedApplyTs = System.currentTimeMillis();
            return this;
        }

        public LatencyBuilder deltaComputed() {
            this.deltaComputeTs = System.currentTimeMillis();
            return this;
        }

        public LatencyBuilder wsSent() {
            this.wsSendTs = System.currentTimeMillis();
            return this;
        }

        public LatencyBuilder clientAcked() {
            this.clientAckTs = System.currentTimeMillis();
            return this;
        }

        public void complete() {
            long brokerToAdapter = adapterParseTs - brokerRxTs;
            long adapterToFeed = feedApplyTs - adapterParseTs;
            long feedToDelta = deltaComputeTs - feedApplyTs;
            long deltaToWs = wsSendTs - deltaComputeTs;
            long wsToClient = clientAckTs > 0 ? clientAckTs - wsSendTs : 0;
            long endToEnd = wsSendTs - brokerRxTs;

            tracker.record(new LatencyRecord(
                    instrument, expiry, strike, callPut,
                    brokerToAdapter, adapterToFeed, feedToDelta, deltaToWs, wsToClient,
                    endToEnd, Instant.now()));
        }
    }
}
