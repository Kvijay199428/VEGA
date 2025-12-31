package com.vegatrader.upstox.api.websocket.dispatcher;

import com.vegatrader.upstox.api.websocket.event.MarketDataEvent;
import com.vegatrader.upstox.api.websocket.event.PortfolioUpdateEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Central event dispatcher for market and portfolio events.
 * 
 * <p>
 * Provides a thread-safe event distribution mechanism with:
 * <ul>
 * <li>Multiple listener registration</li>
 * <li>Type-safe event routing</li>
 * <li>Metrics integration via Micrometer</li>
 * <li>Error isolation between listeners</li>
 * </ul>
 * 
 * @since 2.0.0
 */
@Component
public class FeedDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(FeedDispatcher.class);

    private final CopyOnWriteArrayList<Consumer<MarketDataEvent>> marketDataListeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Consumer<PortfolioUpdateEvent>> portfolioListeners = new CopyOnWriteArrayList<>();

    // Metrics
    private final Counter marketEventsDispatched;
    private final Counter portfolioEventsDispatched;
    private final Counter dispatchErrors;

    public FeedDispatcher(MeterRegistry meterRegistry) {
        this.marketEventsDispatched = Counter.builder("feed.dispatcher.market.events")
                .description("Number of market events dispatched")
                .tag("type", "market")
                .register(meterRegistry);

        this.portfolioEventsDispatched = Counter.builder("feed.dispatcher.portfolio.events")
                .description("Number of portfolio events dispatched")
                .tag("type", "portfolio")
                .register(meterRegistry);

        this.dispatchErrors = Counter.builder("feed.dispatcher.errors")
                .description("Number of dispatch errors")
                .register(meterRegistry);

        logger.info("FeedDispatcher initialized with Micrometer metrics");
    }

    /**
     * Registers a market data event listener.
     */
    public void registerMarketDataListener(Consumer<MarketDataEvent> listener) {
        marketDataListeners.add(listener);
        logger.debug("Registered market data listener: {}", listener.getClass().getSimpleName());
    }

    /**
     * Registers a portfolio event listener.
     */
    public void registerPortfolioListener(Consumer<PortfolioUpdateEvent> listener) {
        portfolioListeners.add(listener);
        logger.debug("Registered portfolio listener: {}", listener.getClass().getSimpleName());
    }

    /**
     * Dispatches a market data event to all registered listeners.
     */
    public void dispatch(MarketDataEvent event) {
        marketEventsDispatched.increment();

        for (Consumer<MarketDataEvent> listener : marketDataListeners) {
            try {
                listener.accept(event);
            } catch (Exception e) {
                dispatchErrors.increment();
                logger.error("Market data listener error: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Dispatches a portfolio event to all registered listeners.
     */
    public void dispatch(PortfolioUpdateEvent event) {
        portfolioEventsDispatched.increment();

        for (Consumer<PortfolioUpdateEvent> listener : portfolioListeners) {
            try {
                listener.accept(event);
            } catch (Exception e) {
                dispatchErrors.increment();
                logger.error("Portfolio listener error: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Removes a market data listener.
     */
    public void removeMarketDataListener(Consumer<MarketDataEvent> listener) {
        marketDataListeners.remove(listener);
    }

    /**
     * Removes a portfolio listener.
     */
    public void removePortfolioListener(Consumer<PortfolioUpdateEvent> listener) {
        portfolioListeners.remove(listener);
    }

    /**
     * Gets the count of registered market data listeners.
     */
    public int getMarketDataListenerCount() {
        return marketDataListeners.size();
    }

    /**
     * Gets the count of registered portfolio listeners.
     */
    public int getPortfolioListenerCount() {
        return portfolioListeners.size();
    }
}
