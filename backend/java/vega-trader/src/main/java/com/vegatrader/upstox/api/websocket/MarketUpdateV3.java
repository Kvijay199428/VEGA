package com.vegatrader.upstox.api.websocket;

import com.vegatrader.upstox.api.response.websocket.*;
import com.vegatrader.upstox.api.websocket.event.MarketUpdateEvent;

import java.util.Map;

/**
 * Wrapper class for decoded market data updates from WebSocket feed.
 * 
 * <p>
 * Provides convenient access to market data without directly exposing the proto
 * response structure.
 * 
 * @since 3.0.0
 */
public class MarketUpdateV3 implements MarketUpdateEvent {

    private final MarketDataFeedV3Response response;

    public MarketUpdateV3(MarketDataFeedV3Response response) {
        this.response = response;
    }

    /**
     * Gets the underlying response object.
     * 
     * @return the MarketDataFeedV3Response
     */
    public MarketDataFeedV3Response getResponse() {
        return response;
    }

    /**
     * Gets the feed type.
     * 
     * @return the FeedType enum
     */
    public FeedType getType() {
        return response.getTypeEnum();
    }

    /**
     * Gets the timestamp when this update was generated.
     * 
     * @return the timestamp in milliseconds, or null if not available
     */
    public Long getTimestampAsLong() {
        return response.getCurrentTsAsLong();
    }

    /**
     * Checks if this is a market info message.
     * 
     * @return true if this is market status information
     */
    public boolean isMarketInfo() {
        return response.isMarketInfo();
    }

    /**
     * Checks if this is an initial feed (snapshot).
     * 
     * @return true if this is the initial snapshot
     */
    public boolean isInitialFeed() {
        return response.isInitialFeed();
    }

    /**
     * Checks if this is a live feed update.
     * 
     * @return true if this is a live update
     */
    public boolean isLiveFeed() {
        return response.isLiveFeed();
    }

    /**
     * Gets market info data (only present in market_info messages).
     * 
     * @return the MarketInfoData, or null if not a market_info message
     */
    public MarketInfoData getMarketInfo() {
        return response.getMarketInfo();
    }

    /**
     * Gets all feed data (present in initial_feed and live_feed messages).
     * 
     * @return map of instrument key to feed data, or null if not a feed message
     */
    public Map<String, FeedData> getFeeds() {
        return response.getFeeds();
    }

    /**
     * Gets feed data for a specific instrument.
     * 
     * @param instrumentKey the instrument key (e.g., "NSE_FO|45450")
     * @return the FeedData for the instrument, or null if not found
     */
    public FeedData getFeed(String instrumentKey) {
        return response.getFeedData(instrumentKey);
    }

    /**
     * Gets the number of instruments in this update.
     * 
     * @return the feed count
     */
    public int getFeedCount() {
        return response.getFeedCount();
    }

    /**
     * Checks if a specific market segment is open (from market_info).
     * 
     * @param segment the segment name (e.g., "NSE_EQ")
     * @return true if the segment is open, false otherwise or if not a market_info
     *         message
     */
    public boolean isSegmentOpen(String segment) {
        if (isMarketInfo() && response.getMarketInfo() != null) {
            return response.getMarketInfo().isSegmentOpen(segment);
        }
        return false;
    }

    /**
     * Checks if NSE Equity market is open.
     * 
     * @return true if NSE_EQ is open
     */
    public boolean isNseEquityOpen() {
        return isSegmentOpen("NSE_EQ");
    }

    /**
     * Checks if BSE Equity market is open.
     * 
     * @return true if BSE_EQ is open
     */
    public boolean isBseEquityOpen() {
        return isSegmentOpen("BSE_EQ");
    }

    /**
     * Checks if NSE F&O market is open.
     * 
     * @return true if NSE_FO is open
     */
    public boolean isNseFoOpen() {
        return isSegmentOpen("NSE_FO");
    }

    // MarketUpdateEvent interface implementation

    /**
     * Gets the instrument key for this update.
     * Returns null for market_info messages (which don't have a specific
     * instrument).
     * For feed messages, returns the first instrument key.
     * 
     * @return instrument key or null
     */
    @Override
    public String getInstrumentKey() {
        if (getFeeds() != null && !getFeeds().isEmpty()) {
            return getFeeds().keySet().iterator().next();
        }
        return null;
    }

    /**
     * Gets the timestamp of this update (interface implementation).
     * 
     * @return timestamp in milliseconds since epoch, or 0 if not available
     */
    @Override
    public long getTimestamp() {
        Long ts = getTimestampAsLong();
        return ts != null ? ts : 0L;
    }

    /**
     * Gets the event type identifier.
     * 
     * @return event type based on feed type
     */
    @Override
    public String getEventType() {
        FeedType type = getType();
        if (type != null) {
            return type.toString().toLowerCase();
        }
        return "unknown";
    }

    /**
     * Gets the underlying data object.
     * 
     * @return this MarketUpdateV3 instance
     */
    @Override
    public Object getData() {
        return this;
    }

    @Override
    public String toString() {
        if (isMarketInfo()) {
            return "MarketUpdateV3{type=MARKET_INFO, marketInfo=" + getMarketInfo() + "}";
        } else {
            return "MarketUpdateV3{type=" + getType() + ", feeds=" + getFeedCount() + ", ts=" + getTimestamp() + "}";
        }
    }
}
