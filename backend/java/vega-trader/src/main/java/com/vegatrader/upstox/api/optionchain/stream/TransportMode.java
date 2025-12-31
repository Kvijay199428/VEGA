package com.vegatrader.upstox.api.optionchain.stream;

/**
 * Transport mode for option chain streaming.
 * Per websocket/b2.md section 1.1.
 * 
 * @since 4.8.0
 */
public enum TransportMode {

    /**
     * RFC-6455 Text WebSocket - For debugging and compliance review.
     */
    WS_TEXT("WebSocket Text (JSON)"),

    /**
     * RFC-6455 Binary WebSocket - Production, low latency.
     */
    WS_BINARY("WebSocket Binary"),

    /**
     * HTTP Long Polling - Fallback for network-constrained environments.
     */
    HTTP_LONG_POLL("HTTP Long Polling"),

    /**
     * Internal event bus - No network, backend processing only.
     */
    INTERNAL_BUS("Internal Event Bus");

    private final String description;

    TransportMode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if this mode uses WebSocket.
     */
    public boolean isWebSocket() {
        return this == WS_TEXT || this == WS_BINARY;
    }

    /**
     * Check if this mode uses binary frames.
     */
    public boolean isBinary() {
        return this == WS_BINARY;
    }
}
