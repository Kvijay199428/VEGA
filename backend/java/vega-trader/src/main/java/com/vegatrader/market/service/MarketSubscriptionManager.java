package com.vegatrader.market.service;

import com.vegatrader.market.cache.MarketCacheService;
import com.vegatrader.market.dto.LiveMarketSnapshot;
import com.vegatrader.market.dto.OrderBookSnapshot;
import com.vegatrader.market.feed.FeedMode;
import com.vegatrader.market.feed.MarketFeed;
import com.vegatrader.market.feed.MarketFeedListener;
import com.vegatrader.market.subscription.SubscriptionRegistry;
import com.vegatrader.market.websocket.MarketBroadcaster;
import com.vegatrader.alert.AlertEngine;
import com.vegatrader.strategy.StrategyEngine;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Orchestrator for Market Data subscriptions.
 * Wires together Client Requests -> Registry -> Upstox Feed -> Cache ->
 * AlertEngine -> StrategyEngine -> Broadcast.
 */
@Service
public class MarketSubscriptionManager implements MarketFeedListener {

    private static final Logger logger = LoggerFactory.getLogger(MarketSubscriptionManager.class);

    @Autowired
    private MarketFeed marketFeed;

    @Autowired
    private SubscriptionRegistry registry;

    @Autowired
    private MarketCacheService cache;

    @Autowired
    private MarketBroadcaster broadcaster;

    @Autowired
    private AlertEngine alertEngine;

    @Autowired
    @Lazy // Circular dependency protection
    private StrategyEngine strategyEngine;

    @PostConstruct
    public void init() {
        logger.info("Initializing Market Subscription Manager");
        marketFeed.setListener(this);
        marketFeed.connect();
    }

    /**
     * Subscribe client to instruments.
     */
    public Set<String> subscribe(String clientId, Set<String> instruments, FeedMode mode) {
        Set<String> newSubs = registry.subscribe(clientId, instruments, mode);

        if (!newSubs.isEmpty()) {
            logger.info("Subscribing to {} new instruments on feed", newSubs.size());
            marketFeed.subscribe(newSubs, mode);
        }

        return newSubs;
    }

    /**
     * Unsubscribe client from instruments.
     */
    public Set<String> unsubscribe(String clientId, Set<String> instruments) {
        Set<String> removed = registry.unsubscribe(clientId, instruments);

        if (!removed.isEmpty()) {
            logger.info("Unsubscribing from {} instruments on feed", removed.size());
            marketFeed.unsubscribe(removed);
        }

        return removed;
    }

    // MarketFeedListener Implementation

    @Override
    public void onTick(LiveMarketSnapshot tick) {
        if (tick == null)
            return;

        // Update Cache
        cache.updateTick(tick);

        // Check Alerts
        alertEngine.onTick(tick);

        // Run Strategies
        if (strategyEngine != null) {
            strategyEngine.onTick(tick);
        }

        // Broadcast to clients
        broadcaster.broadcastTick(tick);
    }

    @Override
    public void onDepth(OrderBookSnapshot depth) {
        if (depth == null)
            return;

        // Update Cache
        cache.updateDepth(depth);

        // Check Alerts
        alertEngine.onDepth(depth);

        // Run Strategies
        if (strategyEngine != null) {
            strategyEngine.onDepth(depth);
        }

        // Broadcast to clients
        broadcaster.broadcastDepth(depth);
    }

    @Override
    public void onConnected() {
        logger.info("Market Feed Connected");
        broadcaster.broadcastAll("SYSTEM", "Market Feed Connected");

        // Resubscribe if we had active subscriptions (recovery logic)
        Set<String> active = registry.getActiveSubscriptions();
        if (!active.isEmpty()) {
            logger.info("Resubscribing to {} instruments after reconnect", active.size());
            marketFeed.subscribe(active, FeedMode.FULL);
        }
    }

    @Override
    public void onDisconnected() {
        logger.warn("Market Feed Disconnected");
        broadcaster.broadcastAll("SYSTEM", "Market Feed Disconnected");
    }

    @Override
    public void onError(Throwable error) {
        logger.error("Market Feed Error", error);
    }
}
