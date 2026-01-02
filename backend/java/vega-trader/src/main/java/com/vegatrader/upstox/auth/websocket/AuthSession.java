package com.vegatrader.upstox.auth.websocket;

import com.vegatrader.upstox.auth.dto.AuthEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a connected Auth WebSocket session.
 * Tracks sequence numbers and event history.
 */
public class AuthSession {
    private final String sessionId;
    private final AtomicLong sequenceGenerator = new AtomicLong(0);
    private final List<AuthEvent> eventHistory = Collections.synchronizedList(new ArrayList<>());
    private static final int HISTORY_LIMIT = 50;

    public AuthSession(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long nextSeq() {
        return sequenceGenerator.incrementAndGet();
    }

    public long lastSeq() {
        return sequenceGenerator.get();
    }

    public void recordEvent(AuthEvent event) {
        eventHistory.add(event);
        if (eventHistory.size() > HISTORY_LIMIT) {
            eventHistory.remove(0);
        }
    }

    public List<AuthEvent> getHistory() {
        return new ArrayList<>(eventHistory);
    }
}
