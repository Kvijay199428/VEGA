package com.vegatrader.upstox.auth.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SOC-grade Registry for managing WebSocket sessions by User ID.
 * Allows targeted broadcasting and session management.
 */
@Component
public class AuthSessionRegistry {

    // userId -> Set<Session>
    private final Map<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public void add(String userId, WebSocketSession session) {
        sessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    public void remove(String userId, WebSocketSession session) {
        if (userId == null)
            return;
        var set = sessions.get(userId);
        if (set != null) {
            set.remove(session);
            // Optional: Clean up empty keys if needed to prompt GC,
            // but for a fixed user base, keeping the key is fine.
        }
    }

    public void broadcast(String userId, String payload) {
        var set = sessions.get(userId);
        if (set == null)
            return;

        // Cleanup closed sessions lazily during broadcast
        set.removeIf(s -> !s.isOpen());

        for (var s : set) {
            try {
                // Synchronized sending isn't strictly necessary with TextMessage but good
                // practice if high concurrency
                synchronized (s) {
                    if (s.isOpen()) {
                        s.sendMessage(new TextMessage(payload));
                    }
                }
            } catch (Exception ignored) {
                // Log if strictly needed, but 'ignored' is acceptable for broadcast
                // fire-and-forget
            }
        }
    }

    public void broadcastToAll(String payload) {
        sessions.values().forEach(userSessions -> {
            userSessions.removeIf(s -> !s.isOpen());
            for (var s : userSessions) {
                try {
                    synchronized (s) {
                        if (s.isOpen()) {
                            s.sendMessage(new TextMessage(payload));
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        });
    }
}
