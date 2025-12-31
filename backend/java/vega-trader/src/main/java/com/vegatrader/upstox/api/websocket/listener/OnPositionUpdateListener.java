package com.vegatrader.upstox.api.websocket.listener;

import com.vegatrader.upstox.api.websocket.PortfolioUpdate;

/**
 * Listener interface for position updates from portfolio WebSocket feed.
 * 
 * @since 2.0.0
 */
@FunctionalInterface
public interface OnPositionUpdateListener {

    /**
     * Called when a position update is received.
     * 
     * @param update the portfolio update containing position data
     */
    void onUpdate(PortfolioUpdate update);
}
