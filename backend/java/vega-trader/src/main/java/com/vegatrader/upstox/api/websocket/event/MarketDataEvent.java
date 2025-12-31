package com.vegatrader.upstox.api.websocket.event;

import java.time.Instant;

/**
 * Base event for market data updates.
 * 
 * <p>
 * All market data events extend this class for unified processing
 * through the event bus and replay system.
 * 
 * @since 2.0.0
 */
public abstract class MarketDataEvent {

    private final Instant timestamp;
    private final String instrumentKey;

    protected MarketDataEvent(String instrumentKey) {
        this.timestamp = Instant.now();
        this.instrumentKey = instrumentKey;
    }

    protected MarketDataEvent(String instrumentKey, Instant timestamp) {
        this.timestamp = timestamp;
        this.instrumentKey = instrumentKey;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getInstrumentKey() {
        return instrumentKey;
    }

    /**
     * Returns the event type for routing.
     */
    public abstract MarketDataEventType getEventType();

    /**
     * Market data event types for routing and filtering.
     */
    public enum MarketDataEventType {
        MARKET_INFO,
        SNAPSHOT,
        TICK,
        OHLC,
        DEPTH
    }
}
