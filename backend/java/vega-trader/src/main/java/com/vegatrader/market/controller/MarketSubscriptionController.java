package com.vegatrader.market.controller;

import com.vegatrader.market.cache.MarketCacheService;
import com.vegatrader.market.dto.LiveMarketSnapshot;
import com.vegatrader.market.dto.OrderBookSnapshot;
import com.vegatrader.market.dto.SubscriptionRequest;
import com.vegatrader.market.feed.FeedMode;
import com.vegatrader.market.subscription.SubscriptionRegistry;
import com.vegatrader.market.service.MarketSubscriptionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * REST API for market data subscriptions and snapshots.
 * 
 * Base URL: /api/market
 */
@RestController
@RequestMapping("/api/market")
public class MarketSubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(MarketSubscriptionController.class);

    @Autowired
    private MarketSubscriptionManager subscriptionManager;

    @Autowired
    private SubscriptionRegistry subscriptionRegistry; // Keep for queries

    @Autowired
    private MarketCacheService marketCache;

    /**
     * POST /api/market/subscribe
     * Subscribe to market data for instruments.
     */
    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, Object>> subscribe(
            @RequestBody SubscriptionRequest request,
            @RequestHeader(value = "X-Client-Id", defaultValue = "default") String clientId) {

        Set<String> instruments = request.getAllInstrumentKeys();

        // Handle null mode
        FeedMode mode = FeedMode.FULL;
        if (request.getMode() != null) {
            mode = FeedMode.fromUpstox(request.getMode());
        }

        if (instruments.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "No instruments specified"));
        }

        // Delegate to Manager which handles Feed subscription
        Set<String> newSubs = subscriptionManager.subscribe(clientId, instruments, mode);

        logger.info("Client {} subscribed to {} instruments ({} new)",
                clientId, instruments.size(), newSubs.size());

        return ResponseEntity.ok(Map.of(
                "subscribed", instruments,
                "newSubscriptions", newSubs,
                "mode", mode.name(),
                "totalActive", subscriptionRegistry.getTotalSubscriptionCount()));
    }

    /**
     * POST /api/market/unsubscribe
     * Unsubscribe from market data.
     */
    @PostMapping("/unsubscribe")
    public ResponseEntity<Map<String, Object>> unsubscribe(
            @RequestBody SubscriptionRequest request,
            @RequestHeader(value = "X-Client-Id", defaultValue = "default") String clientId) {

        Set<String> instruments = request.getAllInstrumentKeys();

        // Delegate to Manager
        Set<String> removed = subscriptionManager.unsubscribe(clientId,
                instruments.isEmpty() ? null : instruments);

        logger.info("Client {} unsubscribed, {} instruments released", clientId, removed.size());

        return ResponseEntity.ok(Map.of(
                "unsubscribed", removed,
                "totalActive", subscriptionRegistry.getTotalSubscriptionCount()));
    }

    /**
     * GET /api/market/snapshot
     * Get latest cached snapshot for instrument.
     */
    @GetMapping("/snapshot")
    public ResponseEntity<LiveMarketSnapshot> getSnapshot(
            @RequestParam String instrument) {

        LiveMarketSnapshot snapshot = marketCache.getTick(instrument);
        if (snapshot == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(snapshot);
    }

    /**
     * GET /api/market/depth
     * Get latest cached depth for instrument.
     */
    @GetMapping("/depth")
    public ResponseEntity<OrderBookSnapshot> getDepth(
            @RequestParam String instrument) {

        OrderBookSnapshot depth = marketCache.getDepth(instrument);
        if (depth == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(depth);
    }

    /**
     * GET /api/market/subscriptions
     * Get current subscription status.
     */
    @GetMapping("/subscriptions")
    public ResponseEntity<Map<String, Object>> getSubscriptions(
            @RequestHeader(value = "X-Client-Id", defaultValue = "default") String clientId) {

        return ResponseEntity.ok(Map.of(
                "clientId", clientId,
                "instruments", subscriptionRegistry.getInstrumentsForClient(clientId),
                "totalActive", subscriptionRegistry.getTotalSubscriptionCount(),
                "totalClients", subscriptionRegistry.getClientCount()));
    }

    /**
     * GET /api/market/status
     * Get overall market feed status.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "activeSubscriptions", subscriptionRegistry.getTotalSubscriptionCount(),
                "connectedClients", subscriptionRegistry.getClientCount(),
                "cacheSize", marketCache.size(),
                "status", "OPERATIONAL"));
    }
}
