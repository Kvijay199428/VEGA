package com.vegatrader.hft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.LongAdder;

/**
 * Co-location Latency Profiler.
 * Measures end-to-end latency from PTP timestamp to consumption.
 */
@Service
public class LatencyProfiler {

    private static final Logger logger = LoggerFactory.getLogger(LatencyProfiler.class);

    private final LongAdder totalLatencyNs = new LongAdder();
    private final LongAdder eventCount = new LongAdder();

    /**
     * Record a latency sample.
     * 
     * @param e          The PTP event containing ingress timestamp
     * @param consumedNs The current timestamp when processing finished
     */
    public void record(PtpMarketEvent e, long consumedNs) {
        if (e == null)
            return;

        long latency = consumedNs - e.getPtpNs();
        // Filter outliers or validation
        if (latency > 0) {
            totalLatencyNs.add(latency);
            eventCount.increment();
        }
    }

    /**
     * Get average latency in microseconds.
     */
    public double avgLatencyUs() {
        long count = eventCount.sum();
        if (count == 0)
            return 0.0;
        return (totalLatencyNs.sum() / (double) count) / 1000.0;
    }

    /**
     * Print stats to log (periodically called or on demand).
     */
    public void printStats() {
        logger.info("Latency Stats: Count={} Avg={}us", eventCount.sum(), String.format("%.3f", avgLatencyUs()));
    }
}
