package com.vegatrader.upstox.api.broker;

import java.util.Map;

/**
 * Broker configuration record.
 * 
 * @since 4.2.0
 */
public record Broker(
        String brokerId,
        String name,
        String apiType,
        boolean enabled,
        int priority,
        String apiBaseUrl,
        String websocketUrl,
        String authType,
        Map<String, String> config) {

    /**
     * Check if broker is active.
     */
    public boolean isActive() {
        return enabled;
    }

    /**
     * Check if this is REST API broker.
     */
    public boolean isRestApi() {
        return "REST".equalsIgnoreCase(apiType);
    }

    /**
     * Check if WebSocket supported.
     */
    public boolean hasWebSocket() {
        return websocketUrl != null && !websocketUrl.isBlank();
    }
}
