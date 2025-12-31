package com.vegatrader.upstox.api.response.websocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * Main response wrapper for Market Data Feed V3 WebSocket messages.
 * 
 * <p>
 * This class handles all three types of messages:
 * <ul>
 * <li>market_info - Market status information (first message)</li>
 * <li>initial_feed - Snapshot of current market data (second message)</li>
 * <li>live_feed - Real-time market updates (subsequent messages)</li>
 * </ul>
 * 
 * @since 3.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketDataFeedV3Response {

    /**
     * Type of feed message.
     */
    @SerializedName("type")
    private String type;

    /**
     * Current timestamp when the message was generated (Unix timestamp in
     * milliseconds).
     */
    @SerializedName("currentTs")
    private String currentTs;

    /**
     * Market information data.
     * Present only when type is "market_info".
     */
    @SerializedName("marketInfo")
    private MarketInfoData marketInfo;

    /**
     * Feed data mapped by instrument key.
     * Present when type is "live_feed" or "initial_feed".
     * 
     * <p>
     * Key format: "EXCHANGE|TOKEN" (e.g., "NSE_FO|45450")
     */
    @SerializedName("feeds")
    private Map<String, FeedData> feeds;

    public MarketDataFeedV3Response() {
    }

    // Getters and Setters

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the type as an enum.
     * 
     * @return the FeedType enum, or null if type is invalid
     */
    public FeedType getTypeEnum() {
        if (type != null) {
            try {
                return FeedType.fromValue(type);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    public String getCurrentTs() {
        return currentTs;
    }

    public void setCurrentTs(String currentTs) {
        this.currentTs = currentTs;
    }

    /**
     * Gets the current timestamp as a long value.
     * 
     * @return the timestamp in milliseconds, or null if not set
     */
    public Long getCurrentTsAsLong() {
        return currentTs != null ? Long.parseLong(currentTs) : null;
    }

    public MarketInfoData getMarketInfo() {
        return marketInfo;
    }

    public void setMarketInfo(MarketInfoData marketInfo) {
        this.marketInfo = marketInfo;
    }

    public Map<String, FeedData> getFeeds() {
        return feeds;
    }

    public void setFeeds(Map<String, FeedData> feeds) {
        this.feeds = feeds;
    }

    /**
     * Gets feed data for a specific instrument key.
     * 
     * @param instrumentKey the instrument key (e.g., "NSE_FO|45450")
     * @return the feed data, or null if not found
     */
    public FeedData getFeedData(String instrumentKey) {
        return feeds != null ? feeds.get(instrumentKey) : null;
    }

    /**
     * Checks if this is a market info message.
     * 
     * @return true if type is "market_info"
     */
    public boolean isMarketInfo() {
        FeedType feedType = getTypeEnum();
        return feedType == FeedType.MARKET_INFO;
    }

    /**
     * Checks if this is an initial feed message.
     * 
     * @return true if type is "initial_feed"
     */
    public boolean isInitialFeed() {
        FeedType feedType = getTypeEnum();
        return feedType == FeedType.INITIAL_FEED;
    }

    /**
     * Checks if this is a live feed message.
     * 
     * @return true if type is "live_feed"
     */
    public boolean isLiveFeed() {
        FeedType feedType = getTypeEnum();
        return feedType == FeedType.LIVE_FEED;
    }

    /**
     * Gets the number of instrument feeds in this message.
     * 
     * @return the number of feeds, or 0 if no feeds present
     */
    public int getFeedCount() {
        return feeds != null ? feeds.size() : 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MarketDataFeedV3Response{");
        sb.append("type='").append(type).append('\'');
        sb.append(", ts=").append(currentTs);
        if (isMarketInfo()) {
            sb.append(", marketInfo=").append(marketInfo);
        } else if (feeds != null) {
            sb.append(", feeds=").append(feeds.size());
        }
        sb.append('}');
        return sb.toString();
    }
}
