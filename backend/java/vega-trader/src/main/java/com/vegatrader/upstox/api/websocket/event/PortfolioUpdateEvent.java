package com.vegatrader.upstox.api.websocket.event;

/**
 * Domain event interface for portfolio data updates.
 * 
 * <p>
 * This abstraction decouples business listeners from transport-specific DTOs,
 * allowing the underlying implementation to change without breaking listener
 * contracts.
 * 
 * <p>
 * Similar to MarketUpdateEvent but for portfolio-specific updates.
 * 
 * @since 2.0.0
 */
public interface PortfolioUpdateEvent {

    /**
     * Gets the update type (ORDER, HOLDING, POSITION, GTT).
     * 
     * @return update type string
     */
    String getUpdateType();

    /**
     * Gets the timestamp of this update.
     * 
     * @return timestamp in milliseconds since epoch
     */
    long getTimestamp();

    /**
     * Gets the event type identifier.
     * 
     * @return event type (e.g., "portfolio_update", "error")
     */
    String getEventType();

    /**
     * Gets the underlying data object.
     * 
     * <p>
     * Allows type-specific casting when needed:
     * 
     * <pre>
     * if (event instanceof PortfolioUpdate) {
     *     PortfolioUpdate update = (PortfolioUpdate) event;
     *     // Access specific fields
     * }
     * </pre>
     * 
     * @return the underlying event data
     */
    Object getData();
}
