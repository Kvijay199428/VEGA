package com.vegatrader.upstox.api.websocket.replay;

import com.vegatrader.upstox.api.websocket.bus.EventBus;
import com.vegatrader.upstox.api.websocket.event.MarketDataEvent;
import com.vegatrader.upstox.api.websocket.event.PortfolioUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

/**
 * Replay service for deterministic playback of historical market and portfolio
 * events.
 * 
 * <p>
 * Key features:
 * <ul>
 * <li>Supports both MarketDataEvent and PortfolioUpdateEvent replay</li>
 * <li>Maintains original event ordering</li>
 * <li>Integrates with EventBus for downstream processing</li>
 * <li>Supports dry-run mode for testing</li>
 * </ul>
 * 
 * <p>
 * Use cases:
 * <ul>
 * <li>Backtesting strategies with historical data</li>
 * <li>Replaying missed events after reconnect</li>
 * <li>Testing downstream consumers</li>
 * </ul>
 * 
 * @since 2.0.0
 */
@Service
public class ReplayService {

    private static final Logger logger = LoggerFactory.getLogger(ReplayService.class);

    private final EventBus eventBus;

    public ReplayService(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Replays market data events through the event bus.
     *
     * @param events List of historical market data events
     */
    public void replayMarketData(List<MarketDataEvent> events) {
        logger.info("Starting market data replay: {} events", events.size());
        long startTime = System.currentTimeMillis();

        for (MarketDataEvent event : events) {
            try {
                eventBus.publish(event);
            } catch (Exception e) {
                logger.error("Error replaying market data event: {}", e.getMessage());
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        logger.info("Market data replay complete: {} events in {}ms", events.size(), duration);
    }

    /**
     * Replays portfolio events through the event bus.
     *
     * @param events List of historical portfolio events
     */
    public void replayPortfolio(List<PortfolioUpdateEvent> events) {
        logger.info("Starting portfolio replay: {} events", events.size());
        long startTime = System.currentTimeMillis();

        for (PortfolioUpdateEvent event : events) {
            try {
                eventBus.publish(event);
            } catch (Exception e) {
                logger.error("Error replaying portfolio event: {}", e.getMessage());
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        logger.info("Portfolio replay complete: {} events in {}ms", events.size(), duration);
    }

    /**
     * Replays events within a time range.
     *
     * @param from     Start timestamp (inclusive)
     * @param to       End timestamp (inclusive)
     * @param consumer Consumer to receive events
     * @param <T>      Event type
     */
    public <T> void replay(Instant from, Instant to, Consumer<T> consumer) {
        logger.info("Starting time-based replay from {} to {}", from, to);
        // Implementation would query persisted events from database
        // and stream them to the consumer in order
    }

    /**
     * Replays events in dry-run mode (no side effects).
     *
     * @param events   List of events to replay
     * @param consumer Consumer for processing (typically logging)
     * @param <T>      Event type
     */
    public <T> void dryRun(List<T> events, Consumer<T> consumer) {
        logger.info("Starting dry-run replay: {} events", events.size());

        for (T event : events) {
            try {
                consumer.accept(event);
            } catch (Exception e) {
                logger.warn("Dry-run event processing error: {}", e.getMessage());
            }
        }

        logger.info("Dry-run replay complete");
    }

    /**
     * Replays events with throttling (rate limiting).
     *
     * @param events  List of events to replay
     * @param delayMs Delay between events in milliseconds
     */
    public void replayWithThrottle(List<?> events, long delayMs) {
        logger.info("Starting throttled replay: {} events with {}ms delay", events.size(), delayMs);

        for (Object event : events) {
            try {
                if (event instanceof MarketDataEvent) {
                    eventBus.publish((MarketDataEvent) event);
                } else if (event instanceof PortfolioUpdateEvent) {
                    eventBus.publish((PortfolioUpdateEvent) event);
                }

                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Throttled replay interrupted");
                break;
            } catch (Exception e) {
                logger.error("Error in throttled replay: {}", e.getMessage());
            }
        }

        logger.info("Throttled replay complete");
    }
}
