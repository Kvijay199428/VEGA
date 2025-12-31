package com.vegatrader.upstox.api.websocket.buffer;

import com.vegatrader.upstox.api.websocket.MarketUpdateV3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Bounded buffer for market data updates with backpressure handling.
 * 
 * <p>
 * Key characteristics:
 * <ul>
 * <li>Bounded capacity (default 65,536)</li>
 * <li>Non-blocking offer (never blocks WebSocket thread)</li>
 * <li>Dropped tick metrics</li>
 * <li>Thread-safe</li>
 * </ul>
 * 
 * <p>
 * Backpressure policy:
 * <ul>
 * <li>Buffer full → drop tick (controlled data loss)</li>
 * <li>Track drop count for monitoring</li>
 * <li>Never block I/O thread</li>
 * </ul>
 * 
 * <p>
 * This is standard architecture for market data feeds where:
 * <ul>
 * <li>You cannot slow the broker</li>
 * <li>Bursts can exceed 10-50× normal rate</li>
 * <li>Silent data loss is worse than measured loss</li>
 * </ul>
 * 
 * @since 3.1.0
 */
public class MarketDataBuffer {

    private static final Logger logger = LoggerFactory.getLogger(MarketDataBuffer.class);

    /** Default buffer capacity (power of 2 for performance) */
    public static final int DEFAULT_CAPACITY = 1 << 16; // 65,536

    private final BlockingQueue<MarketUpdateV3> queue;
    private final AtomicLong droppedCount = new AtomicLong(0);
    private final AtomicLong offeredCount = new AtomicLong(0);
    private final AtomicLong consumedCount = new AtomicLong(0);
    private final int capacity;

    /**
     * Creates a buffer with default capacity (65,536).
     */
    public MarketDataBuffer() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Creates a buffer with specified capacity.
     * 
     * @param capacity the maximum number of updates to buffer
     * @throws IllegalArgumentException if capacity <= 0
     */
    public MarketDataBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
        this.queue = new ArrayBlockingQueue<>(capacity);
        logger.info("MarketDataBuffer initialized with capacity: {}", capacity);
    }

    /**
     * Attempts to add an update to the buffer (non-blocking).
     * 
     * <p>
     * CRITICAL: This method never blocks. If the buffer is full,
     * the update is dropped and metrics are updated.
     * 
     * <p>
     * This is the correct behavior for market feeds because:
     * <ul>
     * <li>Blocking would kill the WebSocket I/O thread</li>
     * <li>Controlled data loss is better than connection loss</li>
     * <li>Metrics allow monitoring of overload conditions</li>
     * </ul>
     * 
     * @param update the market update to buffer
     * @return true if added, false if buffer full (dropped)
     * @throws NullPointerException if update is null
     */
    public boolean offer(MarketUpdateV3 update) {
        if (update == null) {
            throw new NullPointerException("update must not be null");
        }

        offeredCount.incrementAndGet();
        boolean accepted = queue.offer(update);

        if (!accepted) {
            long dropped = droppedCount.incrementAndGet();

            // Log backpressure events at intervals (not every drop)
            if (dropped % 1000 == 0) {
                logger.warn("Backpressure: {} total ticks dropped (buffer full at {})",
                        dropped, capacity);
            }
        }

        return accepted;
    }

    /**
     * Retrieves and removes the next update, waiting if necessary.
     * 
     * <p>
     * This method blocks until an element is available.
     * Should only be called from dedicated consumer threads.
     * 
     * @return the next market update
     * @throws InterruptedException if interrupted while waiting
     */
    public MarketUpdateV3 take() throws InterruptedException {
        MarketUpdateV3 update = queue.take();
        consumedCount.incrementAndGet();
        return update;
    }

    /**
     * Retrieves and removes the next update, waiting up to the specified time.
     * 
     * @param timeout how long to wait before giving up
     * @param unit    the time unit of the timeout
     * @return the next update, or null if timeout elapsed
     * @throws InterruptedException if interrupted while waiting
     */
    public MarketUpdateV3 poll(long timeout, TimeUnit unit) throws InterruptedException {
        MarketUpdateV3 update = queue.poll(timeout, unit);
        if (update != null) {
            consumedCount.incrementAndGet();
        }
        return update;
    }

    /**
     * Gets the current number of updates in the buffer.
     * 
     * @return the buffer size (0 to capacity)
     */
    public int size() {
        return queue.size();
    }

    /**
     * Gets the buffer capacity.
     * 
     * @return the maximum buffer size
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Gets the current buffer utilization as a percentage.
     * 
     * @return 0.0 to 100.0 representing buffer fill percentage
     */
    public double getUtilizationPercent() {
        return (size() * 100.0) / capacity;
    }

    /**
     * Checks if buffer utilization is high (>70%).
     * 
     * @return true if buffer is >70% full
     */
    public boolean isHighUtilization() {
        return getUtilizationPercent() > 70.0;
    }

    /**
     * Checks if buffer utilization is critical (>90%).
     * 
     * @return true if buffer is >90% full
     */
    public boolean isCriticalUtilization() {
        return getUtilizationPercent() > 90.0;
    }

    /**
     * Gets the total number of ticks offered to the buffer.
     * 
     * @return total offered count (accepted + dropped)
     */
    public long getOfferedCount() {
        return offeredCount.get();
    }

    /**
     * Gets the total number of ticks consumed from the buffer.
     * 
     * @return total consumed count
     */
    public long getConsumedCount() {
        return consumedCount.get();
    }

    /**
     * Gets the total number of ticks dropped due to buffer full.
     * 
     * @return total dropped count
     */
    public long getDroppedCount() {
        return droppedCount.get();
    }

    /**
     * Gets the drop rate as a percentage of offered ticks.
     * 
     * @return 0.0 to 100.0 representing drop percentage
     */
    public double getDropRatePercent() {
        long offered = offeredCount.get();
        if (offered == 0) {
            return 0.0;
        }
        return (droppedCount.get() * 100.0) / offered;
    }

    /**
     * Clears all pending updates and resets metrics.
     * For testing and recovery purposes.
     */
    public void clear() {
        queue.clear();
        droppedCount.set(0);
        offeredCount.set(0);
        consumedCount.set(0);
        logger.info("Buffer cleared and metrics reset");
    }

    /**
     * Gets buffer statistics for monitoring.
     * 
     * @return statistics object
     */
    public BufferStatistics getStatistics() {
        return new BufferStatistics(
                capacity,
                size(),
                offeredCount.get(),
                consumedCount.get(),
                droppedCount.get(),
                getUtilizationPercent(),
                getDropRatePercent());
    }

    /**
     * Immutable statistics snapshot.
     */
    public static class BufferStatistics {
        public final int capacity;
        public final int currentSize;
        public final long offeredCount;
        public final long consumedCount;
        public final long droppedCount;
        public final double utilizationPercent;
        public final double dropRatePercent;

        BufferStatistics(int capacity, int currentSize, long offeredCount,
                long consumedCount, long droppedCount,
                double utilizationPercent, double dropRatePercent) {
            this.capacity = capacity;
            this.currentSize = currentSize;
            this.offeredCount = offeredCount;
            this.consumedCount = consumedCount;
            this.droppedCount = droppedCount;
            this.utilizationPercent = utilizationPercent;
            this.dropRatePercent = dropRatePercent;
        }

        @Override
        public String toString() {
            return String.format(
                    "BufferStats{capacity=%d, size=%d, offered=%d, consumed=%d, " +
                            "dropped=%d, utilization=%.1f%%, dropRate=%.2f%%}",
                    capacity, currentSize, offeredCount, consumedCount,
                    droppedCount, utilizationPercent, dropRatePercent);
        }
    }
}
