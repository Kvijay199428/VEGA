package com.vegatrader.upstox.auth.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Binds WebSocket sessions to a User ID extracted from headers or established
 * session.
 * Mandatory for identifying who owns a connection in check-fail scenarios.
 */
@Component
public class UserHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        // Extract from Header (X-USER-ID) or query param, or default to a system user
        // for now.
        // In a real auth setup, this would validate the JWT.
        // For this terminal implementation, we respect the X-USER-ID header if present,
        // otherwise default to "VGA001" (Admin).

        String userId = null;
        if (request.getHeaders().containsKey("X-USER-ID")) {
            userId = request.getHeaders().getFirst("X-USER-ID");
        }

        if (userId == null || userId.isEmpty()) {
            userId = "VGA001"; // Default Admin User
        }

        attributes.put("userId", userId);
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        // No-op
    }
}
