package com.vegatrader.service.exception;

/**
 * Exception thrown when no healthy access tokens are available.
 * 
 * <p>
 * This exception indicates that all tokens have failed health checks
 * with definitive authentication failures (HTTP 401/410), not transient
 * network errors.
 * 
 * <p>
 * This is a checked exception to force callers to handle the scenario
 * explicitly, enabling:
 * <ul>
 * <li>Token regeneration coordination</li>
 * <li>Distinct alerting for auth vs network failures</li>
 * <li>Clean retry logic</li>
 * </ul>
 * 
 * <p>
 * <strong>Recovery Options:</strong>
 * <ol>
 * <li>Trigger token regeneration via login flow</li>
 * <li>Escalate to monitoring/alerting</li>
 * <li>Retry with exponential backoff</li>
 * </ol>
 * 
 * @since 3.1.0
 */
public class NoHealthyTokenException extends Exception {

    private final int totalTokens;
    private final int unhealthyTokens;
    private final int unknownTokens;

    /**
     * Creates exception with token health statistics.
     * 
     * @param message         the detail message
     * @param totalTokens     total number of tokens checked
     * @param unhealthyTokens number of definitively unhealthy tokens (401/410)
     * @param unknownTokens   number of tokens with unknown status (network errors)
     */
    public NoHealthyTokenException(String message, int totalTokens, int unhealthyTokens, int unknownTokens) {
        super(message);
        this.totalTokens = totalTokens;
        this.unhealthyTokens = unhealthyTokens;
        this.unknownTokens = unknownTokens;
    }

    /**
     * Creates exception with simple message.
     * 
     * @param message the detail message
     */
    public NoHealthyTokenException(String message) {
        this(message, 0, 0, 0);
    }

    /**
     * Gets total number of tokens checked.
     * 
     * @return token count
     */
    public int getTotalTokens() {
        return totalTokens;
    }

    /**
     * Gets number of definitively unhealthy tokens.
     * 
     * <p>
     * These tokens returned HTTP 401 or 410, indicating
     * they are expired, revoked, or tier-mismatched.
     * 
     * @return unhealthy token count
     */
    public int getUnhealthyTokens() {
        return unhealthyTokens;
    }

    /**
     * Gets number of tokens with unknown health status.
     * 
     * <p>
     * These tokens failed health checks due to network
     * errors, timeouts, or DNS failures - not authentication issues.
     * They may be healthy but temporarily unreachable.
     * 
     * @return unknown status token count
     */
    public int getUnknownTokens() {
        return unknownTokens;
    }

    /**
     * Checks if all tokens are definitively unhealthy.
     * 
     * <p>
     * If true, token regeneration is required.
     * If false, network issues may be the cause.
     * 
     * @return true if all checked tokens are unhealthy (no unknown states)
     */
    public boolean isDefinitiveAuthFailure() {
        return unknownTokens == 0 && unhealthyTokens > 0;
    }

    @Override
    public String toString() {
        if (totalTokens > 0) {
            return String.format("NoHealthyTokenException: %s (total=%d, unhealthy=%d, unknown=%d)",
                    getMessage(), totalTokens, unhealthyTokens, unknownTokens);
        }
        return super.toString();
    }
}
