package com.vegatrader.upstox.api.websocket.buffer;

import com.vegatrader.upstox.api.websocket.PortfolioUpdate;
import com.vegatrader.upstox.api.websocket.bus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SINGLE-THREADED consumer for portfolio data buffer.
 * 
 * <p>
 * ⚠️ CRITICAL: Exactly ONE instance of this consumer must run to maintain
 * ordering guarantees.
 * 
 * <p>
 * Architecture:
 * 
 * <pre>
 * WebSocket Thread → PortfolioDataBuffer.offerWithTimeout() [bounded blocking]
 *                          ↓
 *                   SINGLE Consumer Thread (this class)
 *                          ↓
 *                   EventBus.publish() [ORDERED]
 *                          ↓
 *               Subscribers (cache, metrics, listeners)
 * </pre>
 * 
 * <p>
 * Why single-threaded:
 * <ul>
 * <li>Order 1 (PENDING) → Order 2 (EXECUTED) must arrive in sequence</li>
 * <li>Multiple threads can reorder events</li>
 * <li>Portfolio updates are low volume - throughput is not bottleneck</li
 * >
 * <li>Correctness > performance for portfolio data</li>
 * </ul>
 * 
 * @since 2.0.0
 */
public class PortfolioBufferConsumer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioBufferConsumer.class);

    private final PortfolioDataBuffer buffer;
    private final EventBus eventBus;
    private final String workerName;

    /**
     * Creates a portfolio buffer consumer.
     * 
     * @param buffer     the buffer to consume from
     * @param eventBus   the event bus to publish to
     * @param workerName identifier for this worker (for logging)
     */
    public PortfolioBufferConsumer(PortfolioDataBuffer buffer, EventBus eventBus, String workerName) {
        if (buffer == null) {
            throw new NullPointerException("buffer must not be null");
        }
        if (eventBus == null) {
            throw new NullPointerException("eventBus must not be null");
        }
        if (workerName == null || workerName.isEmpty()) {
            throw new IllegalArgumentException("workerName must not be null or empty");
        }

        this.buffer = buffer;
        this.eventBus = eventBus;
        this.workerName = workerName;
    }

    /**
     * Runs the consumer loop.
     * 
     * <p>
     * Continuously:
     * <ol>
     * <li>Takes update from buffer (blocks if empty)</li>
     * <li>Publishes to event bus in strict order</li>
     * <li>Repeats until interrupted</li>
     * </ol>
     * 
     * <p>
     * ⚠️ ORDERING GUARANTEE: Events are published in the exact order they were
     * buffered.
     * 
     * <p>
     * On interruption, exits cleanly.
     */
    @Override
    public void run() {
        logger.info("PortfolioBufferConsumer [{}] started (SINGLE-THREADED for ordering)", workerName);

        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Blocking call - waits for next update
                    // This ensures sequential processing
                    PortfolioUpdate update = buffer.take();

                    // Publish to event bus (subscribers notified synchronously in order)
                    eventBus.publish(update);

                } catch (InterruptedException e) {
                    // Restore interrupt flag and exit
                    Thread.currentThread().interrupt();
                    logger.info("PortfolioBufferConsumer [{}] interrupted, exiting", workerName);
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("PortfolioBufferConsumer [{}] failed with unexpected error: {}",
                    workerName, e.getMessage(), e);
        } finally {
            logger.info("PortfolioBufferConsumer [{}] stopped", workerName);
        }
    }
}
