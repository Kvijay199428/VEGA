package com.vegatrader.upstox.auth.selenium.v2.exception;

/**
 * Exception thrown for network timeout failures.
 * Retryable with exponential backoff.
 *
 * @since 2.2.0
 */
public class NetworkTimeoutException extends AuthException {

    private final long timeoutMs;

    public NetworkTimeoutException(String message) {
        super(message, TokenFailureReason.NETWORK_TIMEOUT);
        this.timeoutMs = 0;
    }

    public NetworkTimeoutException(String message, long timeoutMs) {
        super(message, TokenFailureReason.NETWORK_TIMEOUT);
        this.timeoutMs = timeoutMs;
    }

    public NetworkTimeoutException(String message, Throwable cause) {
        super(message, TokenFailureReason.NETWORK_TIMEOUT, cause);
        this.timeoutMs = 0;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    @Override
    public boolean isRetryable() {
        return true; // Network timeouts are retryable
    }
}
