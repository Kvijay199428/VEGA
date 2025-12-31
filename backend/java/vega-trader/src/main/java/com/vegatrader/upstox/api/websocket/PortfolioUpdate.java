package com.vegatrader.upstox.api.websocket;

import com.vegatrader.upstox.api.response.websocket.*;
import com.vegatrader.upstox.api.websocket.event.PortfolioUpdateEvent;

/**
 * Wrapper class for portfolio updates from WebSocket feed.
 * 
 * <p>
 * Provides convenient access to portfolio data without directly exposing
 * the response structure. Implements PortfolioUpdateEvent for event bus
 * integration.
 * 
 * <p>
 * Similar to MarketUpdateV3 but for portfolio-specific updates.
 * 
 * @since 2.0.0
 */
public class PortfolioUpdate implements PortfolioUpdateEvent {

    private final PortfolioUpdateFeed.UpdateType updateType;
    private final long timestamp;
    private final Object data;

    // Type-specific data
    private OrderUpdate orderUpdate;
    private HoldingUpdate holdingUpdate;
    private PositionUpdate positionUpdate;
    private GttUpdate gttUpdate;

    /**
     * Creates a portfolio update from an order update.
     */
    public PortfolioUpdate(OrderUpdate orderUpdate) {
        this.updateType = PortfolioUpdateFeed.UpdateType.ORDER;
        this.timestamp = orderUpdate.getTimestamp() != null ? orderUpdate.getTimestamp() : System.currentTimeMillis();
        this.data = orderUpdate;
        this.orderUpdate = orderUpdate;
    }

    /**
     * Creates a portfolio update from a holding update.
     */
    public PortfolioUpdate(HoldingUpdate holdingUpdate) {
        this.updateType = PortfolioUpdateFeed.UpdateType.HOLDING;
        this.timestamp = holdingUpdate.getTimestamp() != null ? holdingUpdate.getTimestamp()
                : System.currentTimeMillis();
        this.data = holdingUpdate;
        this.holdingUpdate = holdingUpdate;
    }

    /**
     * Creates a portfolio update from a position update.
     */
    public PortfolioUpdate(PositionUpdate positionUpdate) {
        this.updateType = PortfolioUpdateFeed.UpdateType.POSITION;
        this.timestamp = positionUpdate.getTimestamp() != null ? positionUpdate.getTimestamp()
                : System.currentTimeMillis();
        this.data = positionUpdate;
        this.positionUpdate = positionUpdate;
    }

    /**
     * Creates a portfolio update from a GTT update.
     */
    public PortfolioUpdate(GttUpdate gttUpdate) {
        this.updateType = PortfolioUpdateFeed.UpdateType.GTT;
        this.timestamp = gttUpdate.getTimestamp() != null ? gttUpdate.getTimestamp() : System.currentTimeMillis();
        this.data = gttUpdate;
        this.gttUpdate = gttUpdate;
    }

    // PortfolioUpdateEvent interface implementation

    @Override
    public String getUpdateType() {
        return updateType.toString();
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getEventType() {
        return "portfolio_update";
    }

    @Override
    public Object getData() {
        return data;
    }

    // Type checking methods

    /**
     * Checks if this is an order update.
     * 
     * @return true if update type is ORDER
     */
    public boolean isOrderUpdate() {
        return updateType == PortfolioUpdateFeed.UpdateType.ORDER;
    }

    /**
     * Checks if this is a holding update.
     * 
     * @return true if update type is HOLDING
     */
    public boolean isHoldingUpdate() {
        return updateType == PortfolioUpdateFeed.UpdateType.HOLDING;
    }

    /**
     * Checks if this is a position update.
     * 
     * @return true if update type is POSITION
     */
    public boolean isPositionUpdate() {
        return updateType == PortfolioUpdateFeed.UpdateType.POSITION;
    }

    /**
     * Checks if this is a GTT update.
     * 
     * @return true if update type is GTT
     */
    public boolean isGttUpdate() {
        return updateType == PortfolioUpdateFeed.UpdateType.GTT;
    }

    // Type-safe extraction methods

    /**
     * Gets the order update (if this is an order update).
     * 
     * @return OrderUpdate or null if not an order update
     */
    public OrderUpdate getOrderUpdate() {
        return orderUpdate;
    }

    /**
     * Gets the holding update (if this is a holding update).
     * 
     * @return HoldingUpdate or null if not a holding update
     */
    public HoldingUpdate getHoldingUpdate() {
        return holdingUpdate;
    }

    /**
     * Gets the position update (if this is a position update).
     * 
     * @return PositionUpdate or null if not a position update
     */
    public PositionUpdate getPositionUpdate() {
        return positionUpdate;
    }

    /**
     * Gets the GTT update (if this is a GTT update).
     * 
     * @return GttUpdate or null if not a GTT update
     */
    public GttUpdate getGttUpdate() {
        return gttUpdate;
    }

    @Override
    public String toString() {
        return String.format("PortfolioUpdate{type=%s, ts=%d, data=%s}",
                updateType, timestamp, data);
    }
}
