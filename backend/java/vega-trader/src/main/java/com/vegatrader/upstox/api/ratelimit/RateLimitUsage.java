package com.vegatrader.upstox.api.ratelimit;

/**
 * Rate limit usage statistics.
 * <p>
 * Provides current usage information for all time windows.
 * </p>
 *
 * @since 2.0.0
 */
public class RateLimitUsage {

    private final int perSecond;
    private final int perSecondLimit;
    private final int perMinute;
    private final int perMinuteLimit;
    private final int per30Min;
    private final int per30MinLimit;

    /**
     * Creates rate limit usage statistics.
     *
     * @param perSecond      current requests in last second
     * @param perSecondLimit limit for per second
     * @param perMinute      current requests in last minute
     * @param perMinuteLimit limit for per minute
     * @param per30Min       current requests in last 30 minutes
     * @param per30MinLimit  limit for per 30 minutes
     */
    public RateLimitUsage(int perSecond, int perSecondLimit,
            int perMinute, int perMinuteLimit,
            int per30Min, int per30MinLimit) {
        this.perSecond = perSecond;
        this.perSecondLimit = perSecondLimit;
        this.perMinute = perMinute;
        this.perMinuteLimit = perMinuteLimit;
        this.per30Min = per30Min;
        this.per30MinLimit = per30MinLimit;
    }

    /**
     * Gets per-second usage percentage.
     *
     * @return percentage (0-100)
     */
    public double getPerSecondUtilization() {
        return (double) perSecond / perSecondLimit * 100;
    }

    /**
     * Gets per-minute usage percentage.
     *
     * @return percentage (0-100)
     */
    public double getPerMinuteUtilization() {
        return (double) perMinute / perMinuteLimit * 100;
    }

    /**
     * Gets per-30-minute usage percentage.
     *
     * @return percentage (0-100)
     */
    public double getPer30MinUtilization() {
        return (double) per30Min / per30MinLimit * 100;
    }

    /**
     * Returns true if any limit is close to being exceeded (>80%).
     *
     * @return true if nearing limit
     */
    public boolean isNearingLimit() {
        return getPerSecondUtilization() > 80 ||
                getPerMinuteUtilization() > 80 ||
                getPer30MinUtilization() > 80;
    }

    /**
     * Gets the highest utilization percentage across all windows.
     *
     * @return max utilization percentage
     */
    public double getMaxUtilization() {
        return Math.max(getPerSecondUtilization(),
                Math.max(getPerMinuteUtilization(), getPer30MinUtilization()));
    }

    public int getPerSecond() {
        return perSecond;
    }

    public int getPerSecondLimit() {
        return perSecondLimit;
    }

    public int getPerMinute() {
        return perMinute;
    }

    public int getPerMinuteLimit() {
        return perMinuteLimit;
    }

    public int getPer30Min() {
        return per30Min;
    }

    public int getPer30MinLimit() {
        return per30MinLimit;
    }

    @Override
    public String toString() {
        return String.format(
                "RateLimitUsage{per_sec=%d/%d (%.1f%%), per_min=%d/%d (%.1f%%), per_30min=%d/%d (%.1f%%)}",
                perSecond, perSecondLimit, getPerSecondUtilization(),
                perMinute, perMinuteLimit, getPerMinuteUtilization(),
                per30Min, per30MinLimit, getPer30MinUtilization());
    }
}
