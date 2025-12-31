package com.vegatrader.upstox.api.websocket.buffer;

import com.vegatrader.upstox.api.websocket.PortfolioUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * NO-DROP bounded buffer for portfolio updates with timeout-based backpressure.
 * 
 * <p>
 * ⚠️ CRITICAL DIFFERENCE FROM MARKET DATA: Portfolio events are NEVER dropped
 * silently.
 * 
 * <p>
 * Key characteristics:
 * <ul>
 * <li>Bounded capacity (default 16,384)</li>
 * <li>Bounded blocking offer with timeout (NOT non-blocking like
 * MarketDataBuffer)</li>
 * <li>Saturation tracking (NOT drop tracking)</li>
 * <li>Triggers controlled reconnect on timeout</li>
 * <li>Thread-safe</li>
 * </ul>
 * 
 * <p>
 * Backpressure policy:
 * <ul>
 * <li>Buffer full → bounded blocking with 50ms timeout</li>
 * <li>Timeout → trigger controlled reconnect (NEVER drop)</li>
 * <li>Track saturation events for monitoring</li>
 * </ul>
 * 
 * @since 2.0.0
 */
public class PortfolioDataBuffer {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioDataBuffer.class);

    /**
     * Default buffer capacity (smaller than market data - portfolio is low volume)
     */
    public static final int DEFAULT_CAPACITY = 1 << 14; // 16,384

    private final BlockingQueue<PortfolioUpdate> queue;
    private final AtomicLong saturationCount = new AtomicLong(0);
    private final AtomicLong offeredCount = new AtomicLong(0);
    private final AtomicLong consumedCount = new AtomicLong(0);
    private final int capacity;

    /**
     * Creates a buffer with default capacity (16,384).
     */
    public PortfolioDataBuffer() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Creates a buffer with specified capacity.
     * 
     * @param capacity the maximum number of updates to buffer
     * @throws IllegalArgumentException if capacity <= 0
     */
    public PortfolioDataBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
        this.queue = new ArrayBlockingQueue<>(capacity);
        logger.info("PortfolioDataBuffer initialized with capacity: {}", capacity);
    }

    /**
     * Attempts to add an update to the buffer with timeout.
     * 
     * <p>
     * ⚠️ CRITICAL: This method uses BOUNDED BLOCKING, not silent drops.
     * If the buffer is full:
     * <ol>
     * <li>Waits up to timeout for space to become available</li>
     * <li>If timeout expires, returns false (saturation event)</li>
     * <li>Caller must trigger controlled reconnect</li>
     * </ol>
     * 
     * <p>
     * This is the correct behavior for portfolio feeds because:
     * <ul>
     * <li>Dropping order updates → incorrect state, wrong PnL, regulatory
     * issues</li>
     * <li>Controlled reconnect → resync from authoritative source</li>
     * <li>Metrics allow monitoring of saturation conditions</li>
     * </ul>
     * 
     * @param update  the portfolio update to buffer
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout
     * @return true if added, false if timeout (saturation)
     * @throws NullPointerException if update is null
     * @throws InterruptedException if interrupted while waiting
     */
    public boolean offerWithTimeout(PortfolioUpdate update, long timeout, TimeUnit unit)
            throws InterruptedException {
        if (update == null) {
            throw new NullPointerException("update must not be null");
        }

        offeredCount.incrementAndGet();
        boolean accepted = queue.offer(update, timeout, unit);

        if (!accepted) {
            long saturations = saturationCount.incrementAndGet();

            // Log saturation events at intervals
            if (saturations % 100 == 0) {
                logger.warn("Buffer saturation: {} total events (buffer full at {})",
                        saturations, capacity);
            }
        }

        return accepted;
    }

    /**
     * Retrieves and removes the next update, waiting if necessary.
     * 
     * <p>
     * This method blocks until an element is available.
     * Should only be called from the single consumer thread.
     * 
     * @return the next portfolio update
     * @throws InterruptedException if interrupted while waiting
     */
    public PortfolioUpdate take() throws InterruptedException {
        PortfolioUpdate update = queue.take();
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
    public PortfolioUpdate poll(long timeout, TimeUnit unit) throws InterruptedException {
        PortfolioUpdate update = queue.poll(timeout, unit);
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
     * Checks if buffer is saturated (full).
     * 
     * @return true if no space available
     */
    public boolean isSaturated() {
        return size() >= capacity;
    }

    /**
     * Gets the total number of updates offered to the buffer.
     * 
     * @return total offered count
     */
    public long getOfferedCount() {
        return offeredCount.get();
    }

    /**
     * Gets the total number of updates consumed from the buffer.
     * 
     * @return total consumed count
     */
    public long getConsumedCount() {
        return consumedCount.get();
    }

    /**
     * Gets the total number of saturation events.
     * 
     * <p>
     * ⚠️ This is NOT a drop count. Portfolio events are never dropped.
     * Saturation events indicate timeout occurred, triggering controlled reconnect.
     * 
     * @return total saturation event count
     */
    public long getSaturationCount() {
        return saturationCount.get();
    }

    /**
     * Gets the saturation rate as a percentage of offered updates.
     * 
     * @return 0.0 to 100.0 representing saturation percentage
     */
    public double getSaturationRatePercent() {
        long offered = offeredCount.get();
        if (offered == 0) {
            return 0.0;
        }
        return (saturationCount.get() * 100.0) / offered;
    }

    /**
     * Clears all pending updates and resets metrics.
     * For testing and recovery purposes.
     */
    public void clear() {
        queue.clear();
        saturationCount.set(0);
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
                saturationCount.get(),
                getUtilizationPercent(),
                getSaturationRatePercent());
    }

    /**
     * Immutable statistics snapshot.
     */
    public static class BufferStatistics {
        public final int capacity;
        public final int currentSize;
        public final long offeredCount;
        public final long consumedCount;
        public final long saturationCount;
        public final double utilizationPercent;
        public final double saturationRatePercent;

        BufferStatistics(int capacity, int currentSize, long offeredCount,
                long consumedCount, long saturationCount,
                double utilizationPercent, double saturationRatePercent) {
            this.capacity = capacity;
            this.currentSize = currentSize;
            this.offeredCount = offeredCount;
            this.consumedCount = consumedCount;
            this.saturationCount = saturationCount;
            this.utilizationPercent = utilizationPercent;
            this.saturationRatePercent = saturationRatePercent;
        }

        @Override
        public String toString() {
            return String.format(
                    "BufferStats{capacity=%d, size=%d, offered=%d, consumed=%d, " +
                            "saturations=%d, utilization=%.1f%%, saturationRate=%.2f%%}",
                    capacity, currentSize, offeredCount, consumedCount,
                    saturationCount, utilizationPercent, saturationRatePercent);
        }
    }
}
