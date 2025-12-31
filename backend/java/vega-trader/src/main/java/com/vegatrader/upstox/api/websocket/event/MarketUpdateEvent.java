package com.vegatrader.upstox.api.websocket.event;

/**
 * Domain event interface for market data updates.
 * 
 * <p>
 * This abstraction decouples business listeners from transport-specific DTOs,
 * allowing the underlying implementation (MarketUpdateV3, V4, etc.) to change
 * without breaking listener contracts.
 * 
 * <p>
 * Enterprise pattern used in:
 * <ul>
 * <li>Trading engines</li>
 * <li>FIX gateways</li>
 * <li>OMS systems</li>
 * </ul>
 * 
 * @since 3.1.0
 */
public interface MarketUpdateEvent {

    /**
     * Gets the instrument key for this update.
     * 
     * @return instrument key (e.g., "NSE_EQ|INE002A01018")
     */
    String getInstrumentKey();

    /**
     * Gets the timestamp of this update.
     * 
     * @return timestamp in milliseconds since epoch
     */
    long getTimestamp();

    /**
     * Gets the event type identifier.
     * 
     * @return event type (e.g., "market_update", "heartbeat", "error")
     */
    String getEventType();

    /**
     * Gets the underlying data object.
     * 
     * <p>
     * Allows type-specific casting when needed:
     * 
     * <pre>
     * if (event instanceof MarketUpdateV3 update) {
     *     // Access V3-specific fields
     * }
     * </pre>
     * 
     * @return the underlying event data
     */
    Object getData();
}
