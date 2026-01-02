package com.vegatrader.upstox.auth.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vegatrader.upstox.auth.dto.AuthStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class AuthStatusWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(AuthStatusWebSocketHandler.class);

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        sessions.add(session);
        logger.debug("Auth WebSocket Status Connection established: {}", session.getId());

        // HARD FIX #2: Send Initial HELLO
        // This is mandatory to prevent browsers from thinking the connection is dead
        broadcastToSession(session, currentStatusSnapshot());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        sessions.remove(session);
        logger.debug("Auth WebSocket Status Transport Error: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        logger.debug("Auth WebSocket Status Connection closed: {}", session.getId());
    }

    // Need a way to get current status snapshot.
    // Since handler is stateless regarding status (AuthService holds it),
    // we need to inject AuthService or have AuthService push it.
    // However, circular dependency risk: AuthService -> Handler. Handler ->
    // AuthService.
    // SOLUTION: Use a volatile reference or static holder, or simply let
    // AuthService push
    // the initial state via a new method `registerSession(session)` but that's
    // complex to change.

    // Easier approach: AuthService already pushes on update.
    // But for "Initial Hello", we don't have the status here.
    // We will create a thread-safe atomic reference to the last known status.

    private volatile AuthStatus lastKnownStatus;

    public void updateStatus(AuthStatus status) {
        this.lastKnownStatus = status;
        String payload = serialize(status);
        if (payload != null) {
            for (WebSocketSession s : sessions) {
                if (s.isOpen())
                    send(s, payload);
            }
        }
    }

    private void broadcastToSession(WebSocketSession session, AuthStatus status) {
        if (status == null)
            return;
        String payload = serialize(status);
        if (payload != null && session.isOpen()) {
            send(session, payload);
        }
    }

    private String serialize(AuthStatus status) {
        try {
            return mapper.writeValueAsString(status);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize AuthStatus", e);
            return null;
        }
    }

    private void send(WebSocketSession session, String payload) {
        try {
            session.sendMessage(new TextMessage(payload));
        } catch (IOException ignored) {
        }
    }

    private AuthStatus currentStatusSnapshot() {
        return lastKnownStatus;
    }

    public void broadcastStatus(AuthStatus status) {
        updateStatus(status);
    }
}
