package com.vegatrader.market.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vegatrader.market.dto.LiveMarketSnapshot;
import com.vegatrader.market.dto.OrderBookSnapshot;
import com.vegatrader.market.subscription.SubscriptionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Market data broadcaster - fans out market updates to subscribed clients.
 * Filters updates based on client subscriptions.
 */
@Service
public class MarketBroadcaster {

    private static final Logger logger = LoggerFactory.getLogger(MarketBroadcaster.class);

    private final MarketWebSocketHandler webSocketHandler;
    private final SubscriptionRegistry subscriptionRegistry;
    private final ObjectMapper objectMapper;

    /** Broadcast statistics */
    private final AtomicLong ticksBroadcast = new AtomicLong(0);
    private final AtomicLong depthsBroadcast = new AtomicLong(0);
    private final AtomicLong errors = new AtomicLong(0);

    public MarketBroadcaster(
            MarketWebSocketHandler webSocketHandler,
            SubscriptionRegistry subscriptionRegistry,
            ObjectMapper objectMapper) {
        this.webSocketHandler = webSocketHandler;
        this.subscriptionRegistry = subscriptionRegistry;
        this.objectMapper = objectMapper;
    }

    /**
     * Broadcast tick update to subscribed clients.
     * 
     * @param snapshot Live market snapshot
     */
    public void broadcastTick(LiveMarketSnapshot snapshot) {
        if (snapshot == null)
            return;

        String instrumentKey = snapshot.getInstrumentKey();
        Set<String> clients = subscriptionRegistry.getClientsForInstrument(instrumentKey);

        if (clients.isEmpty())
            return;

        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "type", "TICK",
                    "data", snapshot));
            TextMessage message = new TextMessage(json);

            for (String clientId : clients) {
                sendToClient(clientId, message);
            }

            ticksBroadcast.incrementAndGet();

        } catch (Exception e) {
            logger.error("Error broadcasting tick for {}", instrumentKey, e);
            errors.incrementAndGet();
        }
    }

    /**
     * Broadcast depth update to subscribed clients.
     * 
     * @param snapshot Order book snapshot
     */
    public void broadcastDepth(OrderBookSnapshot snapshot) {
        if (snapshot == null)
            return;

        String instrumentKey = snapshot.getInstrumentKey();
        Set<String> clients = subscriptionRegistry.getClientsForInstrument(instrumentKey);

        if (clients.isEmpty())
            return;

        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "type", "DEPTH",
                    "data", snapshot));
            TextMessage message = new TextMessage(json);

            for (String clientId : clients) {
                sendToClient(clientId, message);
            }

            depthsBroadcast.incrementAndGet();

        } catch (Exception e) {
            logger.error("Error broadcasting depth for {}", instrumentKey, e);
            errors.incrementAndGet();
        }
    }

    /**
     * Broadcast to all connected clients (admin messages).
     * 
     * @param type Message type
     * @param data Message data
     */
    public void broadcastAll(String type, Object data) {
        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "type", type,
                    "data", data));
            TextMessage message = new TextMessage(json);

            for (String clientId : webSocketHandler.getSessions().keySet()) {
                sendToClient(clientId, message);
            }

        } catch (Exception e) {
            logger.error("Error broadcasting to all", e);
        }
    }

    /**
     * Send message to specific client.
     */
    public void sendToClient(String clientId, TextMessage message) {
        WebSocketSession session = webSocketHandler.getSession(clientId);
        if (session != null && session.isOpen()) {
            try {
                synchronized (session) {
                    session.sendMessage(message);
                }
            } catch (IOException e) {
                logger.warn("Failed to send to client {}: {}", clientId, e.getMessage());
                errors.incrementAndGet();
            }
        }
    }

    /**
     * Get broadcast statistics.
     */
    public Map<String, Long> getStats() {
        return Map.of(
                "ticksBroadcast", ticksBroadcast.get(),
                "depthsBroadcast", depthsBroadcast.get(),
                "errors", errors.get(),
                "connectedClients", (long) webSocketHandler.getSessionCount());
    }
}
