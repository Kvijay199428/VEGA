package com.vegatrader.upstox.auth.selenium.v2;

import com.vegatrader.upstox.auth.selenium.v2.exception.TokenFailureReason;

import java.time.Duration;
import java.time.Instant;

/**
 * Token state tracking for quarantine, cooldown, and failure management.
 * Implements enterprise-grade token lifecycle management.
 *
 * @since 2.2.0
 */
public class TokenStateV2 {

    private static final Duration DEFAULT_COOLDOWN = Duration.ofMinutes(15);
    private static final int MAX_CONSECUTIVE_FAILURES = 3;

    private final String apiName;
    private boolean quarantined;
    private Instant lastFailureAt;
    private TokenFailureReason lastFailureReason;
    private int consecutiveFailures;
    private Instant quarantinedAt;
    private String quarantineReason;

    public TokenStateV2(String apiName) {
        this.apiName = apiName;
        this.quarantined = false;
        this.consecutiveFailures = 0;
    }

    /**
     * Check if token is in cooldown period (15 minutes after failure).
     */
    public boolean isInCooldown() {
        if (lastFailureAt == null) {
            return false;
        }
        return Instant.now().isBefore(lastFailureAt.plus(DEFAULT_COOLDOWN));
    }

    /**
     * Get remaining cooldown time in seconds.
     */
    public long getCooldownRemainingSeconds() {
        if (lastFailureAt == null) {
            return 0;
        }
        Instant cooldownEnd = lastFailureAt.plus(DEFAULT_COOLDOWN);
        long remaining = Duration.between(Instant.now(), cooldownEnd).getSeconds();
        return Math.max(0, remaining);
    }

    /**
     * Mark a failure and update state.
     * 
     * @param reason failure reason
     */
    public void markFailure(TokenFailureReason reason) {
        this.lastFailureAt = Instant.now();
        this.lastFailureReason = reason;
        this.consecutiveFailures++;

        // Auto-quarantine on critical failures
        if (reason.requiresQuarantine()) {
            quarantine(reason.name());
        }

        // Auto-quarantine after max consecutive failures
        if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
            quarantine("Max consecutive failures reached: " + consecutiveFailures);
        }
    }

    /**
     * Quarantine the token (block from auto-regeneration).
     * 
     * @param reason quarantine reason
     */
    public void quarantine(String reason) {
        this.quarantined = true;
        this.quarantinedAt = Instant.now();
        this.quarantineReason = reason;
    }

    /**
     * Clear quarantine (manual intervention).
     */
    public void clearQuarantine() {
        this.quarantined = false;
        this.quarantinedAt = null;
        this.quarantineReason = null;
        this.consecutiveFailures = 0;
    }

    /**
     * Mark success and reset failure counters.
     */
    public void markSuccess() {
        this.consecutiveFailures = 0;
        this.lastFailureReason = null;
    }

    /**
     * Check if token can attempt regeneration.
     */
    public boolean canAttemptRegeneration() {
        if (quarantined) {
            return false;
        }
        if (isInCooldown()) {
            return false;
        }
        return true;
    }

    // Getters

    public String getApiName() {
        return apiName;
    }

    public boolean isQuarantined() {
        return quarantined;
    }

    public Instant getLastFailureAt() {
        return lastFailureAt;
    }

    public TokenFailureReason getLastFailureReason() {
        return lastFailureReason;
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures;
    }

    public Instant getQuarantinedAt() {
        return quarantinedAt;
    }

    public String getQuarantineReason() {
        return quarantineReason;
    }

    @Override
    public String toString() {
        return String.format("TokenStateV2{apiName='%s', quarantined=%s, consecutiveFailures=%d, inCooldown=%s}",
                apiName, quarantined, consecutiveFailures, isInCooldown());
    }
}
