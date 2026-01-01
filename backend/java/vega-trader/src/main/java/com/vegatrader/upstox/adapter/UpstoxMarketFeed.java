package com.vegatrader.upstox.adapter;

import com.vegatrader.market.dto.DepthLevel;
import com.vegatrader.market.dto.LiveMarketSnapshot;
import com.vegatrader.market.dto.OrderBookSnapshot;
import com.vegatrader.market.feed.FeedMode;
import com.vegatrader.market.feed.MarketFeed;
import com.vegatrader.market.feed.MarketFeedListener;
import com.vegatrader.service.UpstoxTokenProvider;
import com.vegatrader.upstox.api.instrument.provider.InstrumentKeyProvider;
import com.vegatrader.upstox.api.response.websocket.FeedData;
import com.vegatrader.upstox.api.response.websocket.BidAskQuote;
import com.vegatrader.upstox.api.websocket.MarketDataStreamerV3;
import com.vegatrader.upstox.api.websocket.MarketUpdateV3;
import com.vegatrader.upstox.api.websocket.settings.MarketDataStreamerSettings;
import com.vegatrader.upstox.api.websocket.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Upstox implementation of the generic MarketFeed.
 * Adapts MarketDataStreamerV3 to the standardized MarketFeed interface.
 */
@Component
public class UpstoxMarketFeed implements MarketFeed {

    private static final Logger logger = LoggerFactory.getLogger(UpstoxMarketFeed.class);

    @Autowired
    private UpstoxTokenProvider tokenProvider;

    @Autowired(required = false)
    private InstrumentKeyProvider instrumentKeyProvider;

    // FullD30Mapper and OrderBookCompressor intentionally unused until proto access
    // is enabled
    // @Autowired private FullD30Mapper fullD30Mapper;
    // @Autowired private OrderBookCompressor orderBookCompressor;

    @Autowired(required = false)
    private com.vegatrader.journal.JournalWriter journalWriter;

    private MarketDataStreamerV3 streamer;
    private MarketFeedListener listener;
    private final Set<String> subscriptions = ConcurrentHashMap.newKeySet();

    private static final String DEFAULT_WS_URL = "wss://api-v2.upstox.com/feed/market-data-feed/v3";
    private static final String DEFAULT_AUTH_URL = "https://api-v2.upstox.com/feed/market-data-feed/auth/authorize";

    @PostConstruct
    public void init() {
        MarketDataStreamerSettings settings = new MarketDataStreamerSettings();
        settings.setWsUrl(DEFAULT_WS_URL);
        settings.setAuthorizeUrl(DEFAULT_AUTH_URL);
        settings.setUseAuthorizeEndpoint(true);
        settings.setAutoReconnectEnabled(true);
        settings.setEnableLogging(true);
        settings.setLogMarketUpdates(false);

        if (instrumentKeyProvider != null) {
            streamer = new MarketDataStreamerV3(tokenProvider, instrumentKeyProvider, settings);
        } else {
            streamer = new MarketDataStreamerV3(tokenProvider, settings);
        }
        streamer.setJournalWriter(journalWriter);

        streamer.setOnMarketUpdateListener(this::handleMarketUpdate);
        streamer.setOnOpenListener(() -> {
            logger.info("Upstox Feed Connected");
            if (listener != null)
                listener.onConnected();
        });
        streamer.setOnCloseListener((code, reason) -> {
            logger.info("Upstox Feed Disconnected: {} - {}", code, reason);
            if (listener != null)
                listener.onDisconnected();
        });
        streamer.setOnErrorListener(error -> {
            logger.error("Upstox Feed Error", error);
            if (listener != null)
                listener.onError(error instanceof Exception ? (Exception) error : new Exception(error));
        });
    }

    @Override
    public void connect() {
        if (streamer != null) {
            streamer.connect();
        }
    }

    @Override
    public void disconnect() {
        if (streamer != null) {
            streamer.disconnect();
        }
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void subscribe(Set<String> instrumentKeys, FeedMode mode) {
        if (streamer == null || instrumentKeys.isEmpty())
            return;

        Mode upstoxMode = Mode.valueOf(mode.name());
        streamer.subscribe(instrumentKeys, upstoxMode);
        subscriptions.addAll(instrumentKeys);
    }

    @Override
    public void unsubscribe(Set<String> instrumentKeys) {
        if (streamer == null || instrumentKeys.isEmpty())
            return;

        streamer.unsubscribe(instrumentKeys);
        subscriptions.removeAll(instrumentKeys);
    }

    @Override
    public void setListener(MarketFeedListener listener) {
        this.listener = listener;
    }

    @Override
    public Set<String> getSubscriptions() {
        return Collections.unmodifiableSet(subscriptions);
    }

    @Override
    public String getSourceName() {
        return "UPSTOX_V3";
    }

    private void handleMarketUpdate(MarketUpdateV3 update) {
        if (listener == null)
            return;

        if (update.getFeeds() != null) {
            update.getFeeds().forEach((key, feedData) -> {
                LiveMarketSnapshot tick = mapToSnapshot(key, feedData, update.getTimestamp());
                if (tick != null) {
                    listener.onTick(tick);
                }

                if (feedData.getMarketLevel() != null) {
                    OrderBookSnapshot depth = mapToDepth(key, feedData, update.getTimestamp());
                    if (depth != null) {
                        listener.onDepth(depth);
                    }
                }
            });
        }
    }

    private LiveMarketSnapshot mapToSnapshot(String key, FeedData data, long timestamp) {
        if (data == null)
            return null;

        LiveMarketSnapshot.LiveMarketSnapshotBuilder builder = LiveMarketSnapshot.builder()
                .instrumentKey(key)
                .receiveTimestamp(System.currentTimeMillis())
                .exchangeTimestamp(timestamp);

        boolean hasData = false;

        if (data.getLtpc() != null) {
            builder.ltp(data.getLtpc().getLtp() != null ? data.getLtpc().getLtp() : 0.0)
                    .close(data.getLtpc().getCp() != null ? data.getLtpc().getCp() : 0.0);

            if (data.getLtpc().getLttAsLong() != null) {
                builder.exchangeTimestamp(data.getLtpc().getLttAsLong());
            }
            hasData = true;
        }

        if (data.getMarketOhlc() != null) {
            builder.open(data.getMarketOhlc().getOpen() != null ? data.getMarketOhlc().getOpen() : 0.0)
                    .high(data.getMarketOhlc().getHigh() != null ? data.getMarketOhlc().getHigh() : 0.0)
                    .low(data.getMarketOhlc().getLow() != null ? data.getMarketOhlc().getLow() : 0.0)
                    .volume(data.getMarketOhlc().getVolume() != null ? data.getMarketOhlc().getVolume() : 0L)
                    .oi(data.getMarketOhlc().getOi() != null ? data.getMarketOhlc().getOi() : 0L);

            if (data.getMarketOhlc().getClose() != null) {
                builder.close(data.getMarketOhlc().getClose());
            }
            hasData = true;
        }

        return hasData ? builder.build() : null;
    }

    private OrderBookSnapshot mapToDepth(String key, FeedData data, long timestamp) {
        if (data == null || data.getMarketLevel() == null)
            return null;

        List<BidAskQuote> quotes = data.getMarketLevel().getBidAskQuotes();
        if (quotes == null)
            return null;

        List<DepthLevel> bids = new ArrayList<>();
        List<DepthLevel> asks = new ArrayList<>();

        for (BidAskQuote q : quotes) {
            if (q.getBq() != null && q.getBq() > 0) {
                bids.add(new DepthLevel(q.getBp(), q.getBq(), q.getBno() != null ? q.getBno() : 0));
            }
            if (q.getAq() != null && q.getAq() > 0) {
                asks.add(new DepthLevel(q.getAp(), q.getAq(), q.getAno() != null ? q.getAno() : 0));
            }
        }

        return OrderBookSnapshot.builder()
                .instrumentKey(key)
                .timestamp(timestamp)
                .bids(bids)
                .asks(asks)
                .build();
    }
}
