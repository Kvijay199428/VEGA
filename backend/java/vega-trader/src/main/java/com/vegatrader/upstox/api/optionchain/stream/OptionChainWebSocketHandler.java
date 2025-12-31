package com.vegatrader.upstox.api.optionchain.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

/**
 * Option Chain WebSocket Handler - enterprise-grade streaming.
 * Per websocket/a1.md sections 5-11.
 * 
 * Features:
 * - Snapshot bootstrap on subscribe
 * - Delta-based updates
 * - Heartbeat with ping/pong
 * - Multi-consumer support
 * 
 * @since 4.8.0
 */
@Component
public class OptionChainWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(OptionChainWebSocketHandler.class);

    private static final long HEARTBEAT_INTERVAL_MS = 3000;
    private static final int MAX_MISSED_HEARTBEATS = 2;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OptionChainStreamManager streamManager;
    private final DeltaDetector deltaDetector;

    // Session management
    private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();

    public OptionChainWebSocketHandler(
            OptionChainStreamManager streamManager,
            DeltaDetector deltaDetector) {
        this.streamManager = streamManager;
        this.deltaDetector = deltaDetector;

        // Start heartbeat scheduler
        heartbeatScheduler.scheduleAtFixedRate(
                this::sendHeartbeats,
                HEARTBEAT_INTERVAL_MS,
                HEARTBEAT_INTERVAL_MS,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.put(sessionId, new SessionInfo(session, Instant.now(), new CopyOnWriteArraySet<>()));
        logger.info("WebSocket connected: {}", sessionId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        logger.info("WebSocket disconnected: {} ({})", sessionId, status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        Map<String, Object> msg = objectMapper.readValue(payload, Map.class);

        String type = (String) msg.get("type");
        String sessionId = session.getId();

        switch (type) {
            case "SUBSCRIBE" -> handleSubscribe(sessionId, msg);
            case "UNSUBSCRIBE" -> handleUnsubscribe(sessionId, msg);
            case "PONG" -> handlePong(sessionId, msg);
            default -> logger.warn("Unknown message type: {}", type);
        }
    }

    /**
     * Handle subscription request.
     */
    private void handleSubscribe(String sessionId, Map<String, Object> msg) {
        String underlyingKey = (String) msg.get("underlyingKey");
        String expiry = (String) msg.get("expiry");
        String streamKey = underlyingKey + "|" + expiry;

        SessionInfo info = sessions.get(sessionId);
        if (info == null)
            return;

        // Add subscription
        info.subscriptions().add(streamKey);

        // Send snapshot
        OptionChainFeedStreamV3 stream = streamManager.getOrCreateStream(underlyingKey, expiry);
        sendSnapshot(info.session(), stream);

        // Send subscribed confirmation
        sendMessage(info.session(), new WsMessage.Subscribed(
                underlyingKey, expiry, stream.getSequenceNumber(), Instant.now()));

        logger.info("Subscribed {} to {}", sessionId, streamKey);
    }

    /**
     * Handle unsubscription request.
     */
    private void handleUnsubscribe(String sessionId, Map<String, Object> msg) {
        String underlyingKey = (String) msg.get("underlyingKey");
        String expiry = (String) msg.get("expiry");
        String streamKey = underlyingKey + "|" + expiry;

        SessionInfo info = sessions.get(sessionId);
        if (info == null)
            return;

        info.subscriptions().remove(streamKey);

        sendMessage(info.session(), new WsMessage.Unsubscribed(
                underlyingKey, expiry, Instant.now()));

        logger.info("Unsubscribed {} from {}", sessionId, streamKey);
    }

    /**
     * Handle pong response.
     */
    private void handlePong(String sessionId, Map<String, Object> msg) {
        SessionInfo info = sessions.get(sessionId);
        if (info != null) {
            info.updateLastPong();
        }
    }

    /**
     * Send snapshot to a session.
     */
    private void sendSnapshot(WebSocketSession session, OptionChainFeedStreamV3 stream) {
        WsMessage.Snapshot snapshot = new WsMessage.Snapshot(
                stream.getSequenceNumber(),
                stream.getUnderlyingKey(),
                stream.getExpiry().toString(),
                stream.getStrikes(),
                Instant.now());
        sendMessage(session, snapshot);
    }

    /**
     * Broadcast delta to all subscribers.
     */
    public void broadcastDelta(String streamKey, WsMessage.Delta delta) {
        for (SessionInfo info : sessions.values()) {
            if (info.subscriptions().contains(streamKey)) {
                sendMessage(info.session(), delta);
            }
        }
    }

    /**
     * Send heartbeats to all sessions.
     */
    private void sendHeartbeats() {
        long ts = System.currentTimeMillis();
        WsMessage.Ping ping = new WsMessage.Ping(ts);

        for (var entry : sessions.entrySet()) {
            SessionInfo info = entry.getValue();

            // Check for missed heartbeats
            if (info.getMissedHeartbeats() >= MAX_MISSED_HEARTBEATS) {
                logger.warn("Session {} missed {} heartbeats, closing",
                        entry.getKey(), info.getMissedHeartbeats());
                try {
                    info.session().close(CloseStatus.GOING_AWAY);
                } catch (IOException e) {
                    logger.error("Error closing session", e);
                }
                sessions.remove(entry.getKey());
                continue;
            }

            sendMessage(info.session(), ping);
            info.incrementMissedHeartbeats();
        }
    }

    /**
     * Send message to session.
     */
    private void sendMessage(WebSocketSession session, Object message) {
        if (!session.isOpen())
            return;

        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            logger.error("Error sending message to {}", session.getId(), e);
        }
    }

    /**
     * Get active session count.
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }

    /**
     * Get total subscriptions.
     */
    public int getTotalSubscriptions() {
        return sessions.values().stream()
                .mapToInt(s -> s.subscriptions().size())
                .sum();
    }

    /**
     * Session info with subscription tracking.
     */
    private static class SessionInfo {
        private final WebSocketSession session;
        private Instant lastPong;
        private final Set<String> subscriptions;
        private int missedHeartbeats = 0;

        SessionInfo(WebSocketSession session, Instant connected, Set<String> subscriptions) {
            this.session = session;
            this.lastPong = connected;
            this.subscriptions = subscriptions;
        }

        WebSocketSession session() {
            return session;
        }

        Set<String> subscriptions() {
            return subscriptions;
        }

        int getMissedHeartbeats() {
            return missedHeartbeats;
        }

        void updateLastPong() {
            this.lastPong = Instant.now();
            this.missedHeartbeats = 0;
        }

        void incrementMissedHeartbeats() {
            this.missedHeartbeats++;
        }
    }
}
