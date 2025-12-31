package com.vegatrader.upstox.api.ratelimit;

/**
 * Rate limit status enumeration.
 * <p>
 * Indicates the result of a rate limit check.
 * </p>
 *
 * @since 2.0.0
 */
public enum RateLimitStatus {

    /**
     * Request is allowed - within all rate limits.
     */
    OK("Request allowed"),

    /**
     * Per-second limit exceeded (e.g., 50 requests/second).
     */
    LIMIT_EXCEEDED_SECOND("Per-second limit exceeded"),

    /**
     * Per-minute limit exceeded (e.g., 500 requests/minute).
     */
    LIMIT_EXCEEDED_MINUTE("Per-minute limit exceeded"),

    /**
     * Per-30-minute limit exceeded (e.g., 2000 requests/30min).
     */
    LIMIT_EXCEEDED_30MIN("Per-30-minute limit exceeded");

    private final String message;

    RateLimitStatus(String message) {
        this.message = message;
    }

    /**
     * Gets the status message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns true if the request is allowed.
     *
     * @return true if status is OK
     */
    public boolean isAllowed() {
        return this == OK;
    }

    /**
     * Returns true if any limit was exceeded.
     *
     * @return true if not OK
     */
    public boolean isExceeded() {
        return this != OK;
    }

    @Override
    public String toString() {
        return name() + ": " + message;
    }
}
