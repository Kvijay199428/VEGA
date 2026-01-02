package com.vegatrader.upstox.auth.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vegatrader.upstox.auth.dto.AuthEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthStatusWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(AuthStatusWebSocketHandler.class);

    // Map sessionId -> AuthSession
    private final Map<String, AuthSession> authSessions = new ConcurrentHashMap<>();

    // Map wsSessionId -> sessionId (for cleanup)
    private final Map<String, String> wsToAuthSession = new ConcurrentHashMap<>();

    // Map sessionId -> WebSocketSession (active connection)
    private final Map<String, WebSocketSession> activeConnections = new ConcurrentHashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        String sessionId = extractSessionId(session.getUri());

        if (sessionId == null || sessionId.isEmpty()) {
            logger.warn("Rejecting WS connection: No sessionId provided");
            session.close(CloseStatus.BAD_DATA.withReason("Missing sessionId"));
            return;
        }

        logger.debug("Auth WS Connected: {} (Session: {})", session.getId(), sessionId);

        // Create or get AuthSession
        AuthSession authSession = authSessions.computeIfAbsent(sessionId, AuthSession::new);

        // Bind connection
        activeConnections.put(sessionId, session);
        wsToAuthSession.put(session.getId(), sessionId);

        // CRITICAL: NO INITIAL EMIT.
        // Client must rely on HTTP bootstrap for initial state.
        // WS is for DELTA updates only.
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.debug("Auth WS Transport Error: {}", session.getId());
        // Cleanup handled in afterConnectionClosed
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = wsToAuthSession.remove(session.getId());
        if (sessionId != null) {
            activeConnections.remove(sessionId);
            // We do NOT remove the AuthSession immediately -> allows reconnect/resume
            // Cleanup should happen via expiry or explicit logout
            logger.debug("Auth WS Disconnected: {} (Session: {})", session.getId(), sessionId);
        }
    }

    /**
     * Publish a delta event to a specific session.
     */
    public void publishEvent(String sessionId, String type, Map<String, Object> payload) {
        AuthSession authSession = authSessions.get(sessionId);
        if (authSession == null)
            return; // or create? typically we only publish to active sessions

        long seq = authSession.nextSeq();

        AuthEvent event = AuthEvent.builder()
                .seq(seq)
                .ts(java.time.Instant.now().toString())
                .type(type)
                .payload(payload)
                .build();

        WebSocketSession wsSession = activeConnections.get(sessionId);
        if (wsSession != null && wsSession.isOpen()) {
            send(wsSession, event);
        }
    }

    /**
     * Broadcast an event to ALL active sessions (e.g. system-wide maintenance, or
     * auth update for all tabs of same user)
     */
    public void broadcastEvent(String type, Map<String, Object> payload) {
        activeConnections.forEach((sessionId, wsSession) -> {
            publishEvent(sessionId, type, payload);
        });
    }

    private String extractSessionId(URI uri) {
        if (uri == null)
            return null;
        try {
            return UriComponentsBuilder.fromUri(uri).build()
                    .getQueryParams().getFirst("sessionId");
        } catch (Exception e) {
            return null;
        }
    }

    public AuthSession getAuthSession(String sessionId) {
        return authSessions.get(sessionId);
    }

    private void send(WebSocketSession session, Object event) {
        try {
            String json = mapper.writeValueAsString(event);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            logger.error("Failed to send WS message", e);
        }
    }
}
