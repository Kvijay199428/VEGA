package com.vegatrader.upstox.api.optionchain.stream;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

/**
 * WebSocket configuration for option chain streaming.
 * Per websocket/a1.md.
 * 
 * @since 4.8.0
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final OptionChainWebSocketHandler optionChainHandler;

    public WebSocketConfig(OptionChainWebSocketHandler optionChainHandler) {
        this.optionChainHandler = optionChainHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(optionChainHandler, "/ws/option-chain")
                .setAllowedOrigins("*"); // Configure for production
    }
}
