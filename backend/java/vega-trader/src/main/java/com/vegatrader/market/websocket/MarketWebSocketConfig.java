package com.vegatrader.market.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket configuration for market data streaming to frontend.
 * Registers /ws/market endpoint.
 */
@Configuration
@EnableWebSocket
public class MarketWebSocketConfig implements WebSocketConfigurer {

    private final MarketWebSocketHandler marketWebSocketHandler;

    public MarketWebSocketConfig(MarketWebSocketHandler marketWebSocketHandler) {
        this.marketWebSocketHandler = marketWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(marketWebSocketHandler, "/ws/market")
                .setAllowedOrigins("*");
    }
}
