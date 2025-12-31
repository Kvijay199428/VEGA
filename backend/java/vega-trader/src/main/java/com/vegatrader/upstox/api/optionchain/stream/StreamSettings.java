package com.vegatrader.upstox.api.optionchain.stream;

import java.util.List;

/**
 * Stream settings per websocket/b2.md section 1.2.
 * 
 * @since 4.8.0
 */
public record StreamSettings(
        TransportMode transportMode,
        int heartbeatIntervalMs,
        boolean deltaOnly,
        boolean snapshotOnReconnect,
        boolean latencyTracking) {

    /**
     * Default production settings.
     */
    public static StreamSettings defaults() {
        return new StreamSettings(
                TransportMode.WS_BINARY,
                3000,
                true,
                true,
                true);
    }

    /**
     * Debug settings with text WebSocket.
     */
    public static StreamSettings debug() {
        return new StreamSettings(
                TransportMode.WS_TEXT,
                5000,
                true,
                true,
                true);
    }

    /**
     * Internal-only settings for backend processing.
     */
    public static StreamSettings internal() {
        return new StreamSettings(
                TransportMode.INTERNAL_BUS,
                0,
                true,
                false,
                false);
    }
}
