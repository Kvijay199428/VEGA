package com.vegatrader.upstox.api.websocket.ratelimiter;

/**
 * Exception thrown when rate limit is exceeded.
 * 
 * @since 2.0.0
 */
public class RateLimitExceededException extends RuntimeException {

    private final String category;
    private final int requested;
    private final int available;

    public RateLimitExceededException(String message) {
        super(message);
        this.category = null;
        this.requested = 0;
        this.available = 0;
    }

    public RateLimitExceededException(String category, int requested, int available) {
        super(String.format("Rate limit exceeded for category %s. Requested: %d, Available: %d",
                category, requested, available));
        this.category = category;
        this.requested = requested;
        this.available = available;
    }

    public String getCategory() {
        return category;
    }

    public int getRequested() {
        return requested;
    }

    public int getAvailable() {
        return available;
    }
}
