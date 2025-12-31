package com.vegatrader.upstox.api.websocket.listener;

import com.vegatrader.upstox.api.websocket.PortfolioUpdate;

/**
 * Listener interface for GTT updates from portfolio WebSocket feed.
 * 
 * @since 2.0.0
 */
@FunctionalInterface
public interface OnGttUpdateListener {

    /**
     * Called when a GTT update is received.
     * 
     * @param update the portfolio update containing GTT data
     */
    void onUpdate(PortfolioUpdate update);
}
