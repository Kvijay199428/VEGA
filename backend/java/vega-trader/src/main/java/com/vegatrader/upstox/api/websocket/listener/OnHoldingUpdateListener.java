package com.vegatrader.upstox.api.websocket.listener;

import com.vegatrader.upstox.api.websocket.PortfolioUpdate;

/**
 * Listener interface for holding updates from portfolio WebSocket feed.
 * 
 * @since 2.0.0
 */
@FunctionalInterface
public interface OnHoldingUpdateListener {

    /**
     * Called when a holding update is received.
     * 
     * @param update the portfolio update containing holding data
     */
    void onUpdate(PortfolioUpdate update);
}
