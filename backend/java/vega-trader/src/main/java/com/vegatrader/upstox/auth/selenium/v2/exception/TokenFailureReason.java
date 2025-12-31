package com.vegatrader.upstox.auth.selenium.v2.exception;

/**
 * Failure reason taxonomy for token generation failures.
 * Used for classification, quarantine decisions, and metrics.
 *
 * @since 2.2.0
 */
public enum TokenFailureReason {

    /**
     * Network timeout during page load or API call.
     * Retryable.
     */
    NETWORK_TIMEOUT,

    /**
     * Invalid credentials (wrong mobile, PIN, or TOTP).
     * NOT retryable - requires user intervention.
     */
    INVALID_CREDENTIALS,

    /**
     * CAPTCHA detected on login page.
     * NOT retryable - requires manual intervention.
     * Token should be quarantined.
     */
    CAPTCHA,

    /**
     * Rate limit hit (too many login attempts).
     * NOT retryable immediately - requires cooldown.
     */
    RATE_LIMIT,

    /**
     * Selenium DOM change - expected element not found.
     * May indicate Upstox UI change.
     */
    SELENIUM_DOM_CHANGE,

    /**
     * Browser crashed or failed to start.
     * Retryable with new browser instance.
     */
    BROWSER_CRASH,

    /**
     * Token exchange failed after successful login.
     */
    TOKEN_EXCHANGE_FAILED,

    /**
     * Profile verification failed - token invalid.
     */
    PROFILE_VERIFICATION_FAILED,

    /**
     * Consent page handling failed.
     */
    CONSENT_FAILED,

    /**
     * Unknown or unclassified failure.
     */
    UNKNOWN;

    /**
     * Check if this failure reason requires token quarantine.
     * Quarantined tokens are blocked from auto-regeneration.
     */
    public boolean requiresQuarantine() {
        return this == CAPTCHA || this == INVALID_CREDENTIALS || this == RATE_LIMIT;
    }

    /**
     * Check if this failure is retryable.
     */
    public boolean isRetryable() {
        return this == NETWORK_TIMEOUT || this == BROWSER_CRASH;
    }

    /**
     * Check if this failure requires manual intervention.
     */
    public boolean requiresManualIntervention() {
        return this == CAPTCHA || this == INVALID_CREDENTIALS;
    }
}
