package com.vegatrader.upstox.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Standard envelope for Auth WebSocket events.
 * Follows strict monotonic sequencing.
 */
@Data
@Builder
public class AuthEvent {
    private long seq; // Monotonic sequence number
    private String ts; // Server timestamp (ISO-8601)
    private String type; // Event type (TOKEN_READY, TOKEN_FAILED, etc.)
    private Map<String, Object> payload; // Event payload
}
