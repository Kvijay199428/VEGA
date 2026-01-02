package com.vegatrader.config;

import com.vegatrader.upstox.auth.websocket.AuthStatusWebSocketHandler;
import com.vegatrader.upstox.auth.websocket.UserHandshakeInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class AuthWebSocketConfig implements WebSocketConfigurer {

    private final AuthStatusWebSocketHandler authStatusHandler;

    public AuthWebSocketConfig(AuthStatusWebSocketHandler authStatusHandler) {
        this.authStatusHandler = authStatusHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(authStatusHandler, "/ws/auth/status")
                .addInterceptors(new UserHandshakeInterceptor())
                .setAllowedOrigins("*");
    }
}
