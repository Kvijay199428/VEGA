package com.vegatrader.upstox.api.websocket.listener;

import com.vegatrader.upstox.api.websocket.PortfolioUpdate;

/**
 * Listener interface for order updates from portfolio WebSocket feed.
 * 
 * @since 2.0.0
 */
@FunctionalInterface
public interface OnOrderUpdateListener {

    /**
     * Called when an order update is received.
     * 
     * @param update the portfolio update containing order data
     */
    void onUpdate(PortfolioUpdate update);
}
