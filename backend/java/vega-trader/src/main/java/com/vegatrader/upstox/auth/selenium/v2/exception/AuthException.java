package com.vegatrader.upstox.auth.selenium.v2.exception;

/**
 * Base exception for authentication failures.
 * Carries failure reason for classification and handling.
 *
 * @since 2.2.0
 */
public class AuthException extends RuntimeException {

    private final TokenFailureReason reason;
    private final String apiName;

    public AuthException(String message, TokenFailureReason reason) {
        super(message);
        this.reason = reason;
        this.apiName = null;
    }

    public AuthException(String message, TokenFailureReason reason, String apiName) {
        super(message);
        this.reason = reason;
        this.apiName = apiName;
    }

    public AuthException(String message, TokenFailureReason reason, Throwable cause) {
        super(message, cause);
        this.reason = reason;
        this.apiName = null;
    }

    public AuthException(String message, TokenFailureReason reason, String apiName, Throwable cause) {
        super(message, cause);
        this.reason = reason;
        this.apiName = apiName;
    }

    public TokenFailureReason getReason() {
        return reason;
    }

    public String getApiName() {
        return apiName;
    }

    /**
     * Check if this exception requires token quarantine.
     */
    public boolean requiresQuarantine() {
        return reason != null && reason.requiresQuarantine();
    }

    /**
     * Check if this exception is retryable.
     */
    public boolean isRetryable() {
        return reason != null && reason.isRetryable();
    }

    @Override
    public String toString() {
        return String.format("AuthException{reason=%s, apiName='%s', message='%s'}",
                reason, apiName, getMessage());
    }
}
