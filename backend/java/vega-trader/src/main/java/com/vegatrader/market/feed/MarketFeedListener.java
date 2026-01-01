package com.vegatrader.market.feed;

import com.vegatrader.market.dto.LiveMarketSnapshot;
import com.vegatrader.market.dto.OrderBookSnapshot;

/**
 * Callback interface for market feed events.
 * Implement this to receive real-time market updates.
 */
public interface MarketFeedListener {

    /**
     * Called on each tick update.
     * 
     * @param tick Latest market snapshot
     */
    void onTick(LiveMarketSnapshot tick);

    /**
     * Called on depth update (if subscribed to FULL or FULL_D30).
     * 
     * @param depth Order book snapshot
     */
    void onDepth(OrderBookSnapshot depth);

    /**
     * Called on connection established.
     */
    default void onConnected() {
    }

    /**
     * Called on connection lost.
     */
    default void onDisconnected() {
    }

    /**
     * Called on feed error.
     * 
     * @param error The error
     */
    default void onError(Throwable error) {
    }
}
