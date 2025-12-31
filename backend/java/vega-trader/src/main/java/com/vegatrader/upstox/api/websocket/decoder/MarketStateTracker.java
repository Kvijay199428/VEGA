package com.vegatrader.upstox.api.websocket.decoder;

import com.upstox.marketdatafeederv3udapi.rpc.proto.MarketDataFeedV3;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tracks market state per Upstox V3 documentation.
 * 
 * <p>
 * Feed synchronization flow:
 * <ol>
 * <li>market_info - Market status for all segments</li>
 * <li>snapshot - Initial market data state</li>
 * <li>live_feed - Real-time updates</li>
 * </ol>
 * 
 * <p>
 * Per documentation: "Do not process ticks until market_info arrives"
 * 
 * @since 3.0.0
 */
public class MarketStateTracker {

    private final AtomicBoolean marketInfoReceived = new AtomicBoolean(false);
    private final AtomicBoolean snapshotReceived = new AtomicBoolean(false);

    /**
     * Checks if the streamer is ready to process live feeds.
     * 
     * @return true if market_info and initial snapshot have been received
     */
    public boolean isReadyForLiveFeed() {
        return marketInfoReceived.get() && snapshotReceived.get();
    }

    /**
     * Checks if market_info has been received.
     * 
     * @return true if market_info was received
     */
    public boolean hasMarketInfo() {
        return marketInfoReceived.get();
    }

    /**
     * Checks if initial snapshot has been received.
     * 
     * @return true if snapshot was received
     */
    public boolean hasSnapshot() {
        return snapshotReceived.get();
    }

    /**
     * Updates state based on the received feed response.
     * 
     * @param feed the FeedResponse from Upstox
     */
    public void onFeedReceived(MarketDataFeedV3.FeedResponse feed) {
        if (feed.hasMarketInfo()) {
            marketInfoReceived.set(true);
        }
        // First non-market_info feed after market_info is the snapshot
        if (marketInfoReceived.get() && !snapshotReceived.get() && !feed.hasMarketInfo()) {
            snapshotReceived.set(true);
        }
    }

    /**
     * Resets the tracker state.
     * Should be called on disconnect or before reconnect.
     */
    public void reset() {
        marketInfoReceived.set(false);
        snapshotReceived.set(false);
    }

    @Override
    public String toString() {
        return String.format("MarketStateTracker{marketInfo=%s, snapshot=%s}",
                marketInfoReceived.get(), snapshotReceived.get());
    }
}
