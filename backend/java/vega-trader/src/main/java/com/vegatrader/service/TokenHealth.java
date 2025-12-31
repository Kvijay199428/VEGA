package com.vegatrader.service;

/**
 * Token health status enum.
 * 
 * <p>
 * Tri-state model prevents network errors from poisoning token pool:
 * <ul>
 * <li><strong>HEALTHY</strong>: HTTP 200 - token is valid</li>
 * <li><strong>UNHEALTHY</strong>: HTTP 401/410 - token is expired/revoked</li>
 * <li><strong>UNKNOWN</strong>: Timeout/IOException - network error, status
 * unclear</li>
 * </ul>
 * 
 * <p>
 * <strong>Critical Rule:</strong> Only UNHEALTHY tokens should be disqualified
 * from use. UNKNOWN tokens may be healthy but temporarily unreachable.
 * 
 * @since 3.1.0
 */
public enum TokenHealth {

    /**
     * Token is healthy (HTTP 200 response from profile endpoint).
     */
    HEALTHY,

    /**
     * Token is definitively unhealthy (HTTP 401 Unauthorized or 410 Gone).
     * 
     * <p>
     * Indicates:
     * <ul>
     * <li>Token expired</li>
     * <li>Token revoked</li>
     * <li>Tier mismatch</li>
     * <li>Invalid credentials</li>
     * </ul>
     * 
     * <p>
     * These tokens should not be retried without regeneration.
     */
    UNHEALTHY,

    /**
     * Token health status is unknown (network timeout, DNS failure, etc.).
     * 
     * <p>
     * Indicates:
     * <ul>
     * <li>Network connectivity issue</li>
     * <li>Upstox API temporarily unavailable</li>
     * <li>DNS resolution failure</li>
     * <li>Connection timeout</li>
     * </ul>
     * 
     * <p>
     * These tokens may be healthy but temporarily unreachable.
     * They should NOT be marked as permanently inactive.
     */
    UNKNOWN;

    /**
     * Checks if this status indicates a usable token.
     * 
     * @return true if HEALTHY, false for UNHEALTHY or UNKNOWN
     */
    public boolean isUsable() {
        return this == HEALTHY;
    }

    /**
     * Checks if this status is a definitive authentication failure.
     * 
     * @return true if UNHEALTHY, false otherwise
     */
    public boolean isAuthFailure() {
        return this == UNHEALTHY;
    }
}
