package com.vegatrader.market.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vegatrader.market.subscription.SubscriptionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for market data streaming.
 * Manages client sessions and message delivery.
 */
@Component
public class MarketWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(MarketWebSocketHandler.class);

    /** Session ID -> WebSocketSession */
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private final SubscriptionRegistry subscriptionRegistry;
    private final ObjectMapper objectMapper;

    public MarketWebSocketHandler(SubscriptionRegistry subscriptionRegistry, ObjectMapper objectMapper) {
        this.subscriptionRegistry = subscriptionRegistry;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        logger.info("WebSocket connected: {} (total: {})", sessionId, sessions.size());

        // Send welcome message
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "CONNECTED",
                    "sessionId", sessionId,
                    "message", "Market WebSocket connected"))));
        } catch (IOException e) {
            logger.error("Failed to send welcome message", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        sessions.remove(sessionId);

        // Cleanup subscriptions for this client
        Set<String> removed = subscriptionRegistry.removeClient(sessionId);

        logger.info("WebSocket disconnected: {} (removed {} subs, remaining: {})",
                sessionId, removed.size(), sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Handle incoming messages (subscriptions, unsubscriptions)
        try {
            String payload = message.getPayload();
            logger.debug("Received from {}: {}", session.getId(), payload);

            // Parse and handle subscription commands
            @SuppressWarnings("unchecked")
            Map<String, Object> msg = objectMapper.readValue(payload, Map.class);
            String type = (String) msg.get("type");

            if ("SUBSCRIBE".equals(type)) {
                handleSubscribe(session, msg);
            } else if ("UNSUBSCRIBE".equals(type)) {
                handleUnsubscribe(session, msg);
            } else if ("PING".equals(type)) {
                session.sendMessage(new TextMessage("{\"type\":\"PONG\"}"));
            }

        } catch (Exception e) {
            logger.error("Error handling message from {}", session.getId(), e);
        }
    }

    private void handleSubscribe(WebSocketSession session, Map<String, Object> msg) throws IOException {
        @SuppressWarnings("unchecked")
        java.util.List<String> instruments = (java.util.List<String>) msg.get("instruments");
        if (instruments != null && !instruments.isEmpty()) {
            Set<String> instSet = new java.util.HashSet<>(instruments);
            subscriptionRegistry.subscribe(session.getId(), instSet,
                    com.vegatrader.market.feed.FeedMode.FULL);

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                    "type", "SUBSCRIBED",
                    "instruments", instruments))));
        }
    }

    private void handleUnsubscribe(WebSocketSession session, Map<String, Object> msg) throws IOException {
        @SuppressWarnings("unchecked")
        java.util.List<String> instruments = (java.util.List<String>) msg.get("instruments");
        Set<String> instSet = instruments != null ? new java.util.HashSet<>(instruments) : null;
        subscriptionRegistry.unsubscribe(session.getId(), instSet);

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(Map.of(
                "type", "UNSUBSCRIBED",
                "instruments", instruments != null ? instruments : "all"))));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.error("WebSocket error for {}: {}", session.getId(), exception.getMessage());
    }

    /**
     * Get all active sessions.
     */
    public Map<String, WebSocketSession> getSessions() {
        return sessions;
    }

    /**
     * Get session by ID.
     */
    public WebSocketSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * Get connected session count.
     */
    public int getSessionCount() {
        return sessions.size();
    }
}
