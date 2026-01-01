package com.vegatrader.market.feed;

import java.util.Set;

/**
 * Exchange-agnostic market data feed interface.
 * Implementations: UpstoxMarketFeed, ZerodhaMarketFeed, etc.
 */
public interface MarketFeed {

    /**
     * Connect to the market data source.
     */
    void connect();

    /**
     * Disconnect from the market data source.
     */
    void disconnect();

    /**
     * Check if connected.
     */
    boolean isConnected();

    /**
     * Subscribe to instruments with specified mode.
     * 
     * @param instrumentKeys Set of instrument keys (e.g., NSE_EQ|RELIANCE)
     * @param mode           Feed mode (LTPC, FULL, FULL_D30)
     */
    void subscribe(Set<String> instrumentKeys, FeedMode mode);

    /**
     * Unsubscribe from instruments.
     * 
     * @param instrumentKeys Set of instrument keys to unsubscribe
     */
    void unsubscribe(Set<String> instrumentKeys);

    /**
     * Set the listener for market updates.
     * 
     * @param listener The callback listener
     */
    void setListener(MarketFeedListener listener);

    /**
     * Get set of currently subscribed instruments.
     */
    Set<String> getSubscriptions();

    /**
     * Get the feed source name.
     */
    String getSourceName();
}
