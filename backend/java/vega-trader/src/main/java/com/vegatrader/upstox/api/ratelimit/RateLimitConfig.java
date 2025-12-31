package com.vegatrader.upstox.api.ratelimit;

/**
 * Rate limit configuration.
 * <p>
 * Defines the rate limits for different time windows.
 * </p>
 *
 * @since 2.0.0
 */
public class RateLimitConfig {

    private final int perSecondLimit;
    private final int perMinuteLimit;
    private final int per30MinLimit;
    private final String category;

    /**
     * Creates a new rate limit configuration.
     *
     * @param perSecondLimit requests allowed per second
     * @param perMinuteLimit requests allowed per minute
     * @param per30MinLimit  requests allowed per 30 minutes
     * @param category       the API category name
     */
    public RateLimitConfig(int perSecondLimit, int perMinuteLimit, int per30MinLimit, String category) {
        this.perSecondLimit = perSecondLimit;
        this.perMinuteLimit = perMinuteLimit;
        this.per30MinLimit = per30MinLimit;
        this.category = category;
    }

    /**
     * Creates configuration for Standard APIs (50/500/2000).
     *
     * @return RateLimitConfig for standard APIs
     */
    public static RateLimitConfig standardApi() {
        return new RateLimitConfig(50, 500, 2000, "Standard API");
    }

    /**
     * Creates configuration for Multi-Order APIs (4/40/160).
     *
     * @return RateLimitConfig for multi-order APIs
     */
    public static RateLimitConfig multiOrderApi() {
        return new RateLimitConfig(4, 40, 160, "Multi-Order API");
    }

    /**
     * Gets the per-second limit.
     *
     * @return requests per second
     */
    public int getPerSecondLimit() {
        return perSecondLimit;
    }

    /**
     * Gets the per-minute limit.
     *
     * @return requests per minute
     */
    public int getPerMinuteLimit() {
        return perMinuteLimit;
    }

    /**
     * Gets the per-30-minute limit.
     *
     * @return requests per 30 minutes
     */
    public int getPer30MinLimit() {
        return per30MinLimit;
    }

    /**
     * Gets the category name.
     *
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return String.format("%s: %d/sec, %d/min, %d/30min",
                category, perSecondLimit, perMinuteLimit, per30MinLimit);
    }
}
