package com.vegatrader.upstox.api.websocket.state;

/**
 * Portfolio feed state enum for health tracking.
 * 
 * <p>
 * State machine for monitoring feed health and gating listener invocation.
 * 
 * @since 2.0.0
 */
public enum PortfolioFeedState {
    /**
     * Initial connection attempt in progress.
     */
    CONNECTING,

    /**
     * Connected to WebSocket, waiting for initial data snapshot.
     */
    SYNCING,

    /**
     * Fully operational - receiving and processing updates normally.
     */
    LIVE,

    /**
     * Connected but experiencing errors/warnings (parse errors, buffer saturation).
     */
    DEGRADED,

    /**
     * Not connected to WebSocket.
     */
    DISCONNECTED
}
