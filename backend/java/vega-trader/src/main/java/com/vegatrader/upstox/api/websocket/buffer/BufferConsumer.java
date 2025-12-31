package com.vegatrader.upstox.api.websocket.buffer;

import com.vegatrader.upstox.api.websocket.MarketUpdateV3;
import com.vegatrader.upstox.api.websocket.bus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Worker thread that consumes updates from the buffer and publishes to event
 * bus.
 * 
 * <p>
 * This separates the I/O thread (WebSocket) from processing threads,
 * ensuring that slow consumers don't block the network layer.
 * 
 * <p>
 * Architecture:
 * 
 * <pre>
 * WebSocket Thread → Buffer.offer() [non-blocking]
 *                         ↓
 *                  Worker Pool (this class)
 *                         ↓
 *                  EventBus.publish()
 *                         ↓
 *               Subscribers (cache, metrics, etc.)
 * </pre>
 * 
 * @since 3.1.0
 */
public class BufferConsumer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(BufferConsumer.class);

    private final MarketDataBuffer buffer;
    private final EventBus eventBus;
    private final String workerName;

    /**
     * Creates a buffer consumer.
     * 
     * @param buffer     the buffer to consume from
     * @param eventBus   the event bus to publish to
     * @param workerName identifier for this worker (for logging)
     */
    public BufferConsumer(MarketDataBuffer buffer, EventBus eventBus, String workerName) {
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
     * <li>Publishes to event bus</li>
     * <li>Repeats until interrupted</li>
     * </ol>
     * 
     * <p>
     * On interruption, exits cleanly.
     */
    @Override
    public void run() {
        logger.info("BufferConsumer [{}] started", workerName);

        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Blocking call - waits for next update
                    MarketUpdateV3 update = buffer.take();

                    // Publish to event bus (subscribers notified synchronously)
                    eventBus.publish(update);

                } catch (InterruptedException e) {
                    // Restore interrupt flag and exit
                    Thread.currentThread().interrupt();
                    logger.info("BufferConsumer [{}] interrupted, exiting", workerName);
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("BufferConsumer [{}] failed with unexpected error: {}",
                    workerName, e.getMessage(), e);
        } finally {
            logger.info("BufferConsumer [{}] stopped", workerName);
        }
    }
}
