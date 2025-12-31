package com.vegatrader.upstox.api.websocket.listener;

import com.vegatrader.upstox.api.websocket.MarketUpdateV3;

/**
 * Listener interface for receiving market data updates from WebSocket feed.
 * 
 * @since 3.0.0
 */
@FunctionalInterface
public interface OnMarketUpdateV3Listener {

    /**
     * Called when a market data update is received.
     * 
     * @param marketUpdate the decoded market update containing feed data
     */
    void onUpdate(MarketUpdateV3 marketUpdate);
}
