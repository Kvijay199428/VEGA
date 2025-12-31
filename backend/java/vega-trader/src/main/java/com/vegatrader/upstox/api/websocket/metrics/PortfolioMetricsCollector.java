package com.vegatrader.upstox.api.websocket.metrics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Metrics collector for portfolio data streamer.
 * 
 * <p>
 * ⚠️ CRITICAL: Metrics are NOT logging. Separate concerns.
 * 
 * <p>
 * Tracks:
 * <ul>
 * <li>Updates received, processed</li>
 * <li>Parse errors</li>
 * <li>Buffer saturations</li>
 * <li>Reconnect attempts</li>
 * <li>Out-of-order rejections</li>
 * <li>Subscriber errors</li>
 * <li>Per-type update counts</li>
 * </ul>
 * 
 * @since 2.0.0
 */
public class PortfolioMetricsCollector {

    // Overall metrics
    private final AtomicLong updatesReceived = new AtomicLong(0);
    private final AtomicLong updatesProcessed = new AtomicLong(0);
    private final AtomicLong parseErrors = new AtomicLong(0);
    private final AtomicLong bufferSaturations = new AtomicLong(0);
    private final AtomicLong reconnectAttempts = new AtomicLong(0);
    private final AtomicLong outOfOrderRejections = new AtomicLong(0);
    private final AtomicLong subscriberErrors = new AtomicLong(0);

    // Per-type counters
    private final AtomicLong orderUpdates = new AtomicLong(0);
    private final AtomicLong holdingUpdates = new AtomicLong(0);
    private final AtomicLong positionUpdates = new AtomicLong(0);
    private final AtomicLong gttUpdates = new AtomicLong(0);

    private volatile long startTime = System.currentTimeMillis();

    public PortfolioMetricsCollector() {
    }

    // Increment methods

    public void incrementUpdatesReceived() {
        updatesReceived.incrementAndGet();
    }

    public void incrementUpdatesProcessed() {
        updatesProcessed.incrementAndGet();
    }

    public void incrementParseErrors() {
        parseErrors.incrementAndGet();
    }

    public void incrementBufferSaturations() {
        bufferSaturations.incrementAndGet();
    }

    public void incrementReconnectAttempts() {
        reconnectAttempts.incrementAndGet();
    }

    public void incrementOutOfOrderRejections() {
        outOfOrderRejections.incrementAndGet();
    }

    public void incrementSubscriberErrors() {
        subscriberErrors.incrementAndGet();
    }

    public void incrementOrderUpdates() {
        orderUpdates.incrementAndGet();
    }

    public void incrementHoldingUpdates() {
        holdingUpdates.incrementAndGet();
    }

    public void incrementPositionUpdates() {
        positionUpdates.incrementAndGet();
    }

    public void incrementGttUpdates() {
        gttUpdates.incrementAndGet();
    }

    // Getter methods

    public long getUpdatesReceived() {
        return updatesReceived.get();
    }

    public long getUpdatesProcessed() {
        return updatesProcessed.get();
    }

    public long getParseErrors() {
        return parseErrors.get();
    }

    public long getBufferSaturations() {
        return bufferSaturations.get();
    }

    public long getReconnectAttempts() {
        return reconnectAttempts.get();
    }

    public long getOutOfOrderRejections() {
        return outOfOrderRejections.get();
    }

    public long getSubscriberErrors() {
        return subscriberErrors.get();
    }

    public long getOrderUpdates() {
        return orderUpdates.get();
    }

    public long getHoldingUpdates() {
        return holdingUpdates.get();
    }

    public long getPositionUpdates() {
        return positionUpdates.get();
    }

    public long getGttUpdates() {
        return gttUpdates.get();
    }

    /**
     * Gets immutable metrics snapshot.
     * 
     * @return metrics snapshot
     */
    public PortfolioMetrics getMetricsSnapshot() {
        long uptime = System.currentTimeMillis() - startTime;
        double processingRate = uptime > 0 ? (updatesProcessed.get() * 1000.0 / uptime) : 0.0;

        return new PortfolioMetrics(
                updatesReceived.get(),
                updatesProcessed.get(),
                parseErrors.get(),
                bufferSaturations.get(),
                reconnectAttempts.get(),
                outOfOrderRejections.get(),
                subscriberErrors.get(),
                orderUpdates.get(),
                holdingUpdates.get(),
                positionUpdates.get(),
                gttUpdates.get(),
                processingRate,
                uptime);
    }

    /**
     * Resets all metrics counters.
     */
    public void reset() {
        updatesReceived.set(0);
        updatesProcessed.set(0);
        parseErrors.set(0);
        bufferSaturations.set(0);
        reconnectAttempts.set(0);
        outOfOrderRejections.set(0);
        subscriberErrors.set(0);
        orderUpdates.set(0);
        holdingUpdates.set(0);
        positionUpdates.set(0);
        gttUpdates.set(0);
        startTime = System.currentTimeMillis();
    }

    /**
     * Immutable metrics snapshot.
     */
    public static class PortfolioMetrics {
        public final long updatesReceived;
        public final long updatesProcessed;
        public final long parseErrors;
        public final long bufferSaturations;
        public final long reconnectAttempts;
        public final long outOfOrderRejections;
        public final long subscriberErrors;
        public final long orderUpdates;
        public final long holdingUpdates;
        public final long positionUpdates;
        public final long gttUpdates;
        public final double processingRate; // updates/sec
        public final long uptimeMs;

        PortfolioMetrics(long updatesReceived, long updatesProcessed, long parseErrors,
                long bufferSaturations, long reconnectAttempts, long outOfOrderRejections,
                long subscriberErrors, long orderUpdates, long holdingUpdates,
                long positionUpdates, long gttUpdates, double processingRate, long uptimeMs) {
            this.updatesReceived = updatesReceived;
            this.updatesProcessed = updatesProcessed;
            this.parseErrors = parseErrors;
            this.bufferSaturations = bufferSaturations;
            this.reconnectAttempts = reconnectAttempts;
            this.outOfOrderRejections = outOfOrderRejections;
            this.subscriberErrors = subscriberErrors;
            this.orderUpdates = orderUpdates;
            this.holdingUpdates = holdingUpdates;
            this.positionUpdates = positionUpdates;
            this.gttUpdates = gttUpdates;
            this.processingRate = processingRate;
            this.uptimeMs = uptimeMs;
        }

        @Override
        public String toString() {
            return String.format(
                    "PortfolioMetrics{received=%d, processed=%d, errors=%d, saturations=%d, " +
                            "reconnects=%d, outOfOrder=%d, subscriberErrs=%d, " +
                            "orders=%d, holdings=%d, positions=%d, gtt=%d, rate=%.2f/s, uptime=%dms}",
                    updatesReceived, updatesProcessed, parseErrors, bufferSaturations,
                    reconnectAttempts, outOfOrderRejections, subscriberErrors,
                    orderUpdates, holdingUpdates, positionUpdates, gttUpdates,
                    processingRate, uptimeMs);
        }
    }
}
