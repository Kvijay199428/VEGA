package com.vegatrader.upstox.auth.selenium.v2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Audit event types for token lifecycle tracking.
 *
 * @since 2.2.0
 */
enum TokenAuditEvent {
    GENERATED, // Token successfully generated
    REFRESHED, // Token refreshed before expiry
    FAILED, // Token generation failed
    QUARANTINED, // Token quarantined due to failure
    UNQUARANTINED, // Token manually unquarantined
    MANUAL_OVERRIDE, // Token manually set
    EXPIRED, // Token expired
    VALIDATED // Token validated via Profile API
}

/**
 * Audit service for token lifecycle events.
 * Provides structured logging and optional database persistence.
 *
 * @since 2.2.0
 */
public class TokenAuditService {

    private static final Logger logger = LoggerFactory.getLogger(TokenAuditService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("Asia/Kolkata"));

    private final com.vegatrader.util.time.TimeProvider timeProvider;

    public TokenAuditService(com.vegatrader.util.time.TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * Log an audit event.
     * 
     * @param apiName API name (PRIMARY, WEBSOCKET1, etc.)
     * @param event   event type
     * @param reason  event reason/details
     */
    public void logEvent(String apiName, TokenAuditEvent event, String reason) {
        String timestamp = FORMATTER.format(timeProvider.now());

        String logMessage = String.format("[AUDIT] %s | API: %-12s | Event: %-15s | %s",
                timestamp, apiName, event, reason != null ? reason : "");

        switch (event) {
            case GENERATED:
            case REFRESHED:
            case VALIDATED:
                logger.info(logMessage);
                break;
            case FAILED:
            case QUARANTINED:
                logger.warn(logMessage);
                break;
            case EXPIRED:
                logger.info(logMessage);
                break;
            default:
                logger.info(logMessage);
        }

        // TODO: Persist to database audit table
        // persistAudit(apiName, event, reason, timestamp);
    }

    /**
     * Log successful token generation.
     */
    public void logGenerated(String apiName, String userId) {
        logEvent(apiName, TokenAuditEvent.GENERATED, "userId=" + userId);
    }

    /**
     * Log token refresh.
     */
    public void logRefreshed(String apiName, String trigger) {
        logEvent(apiName, TokenAuditEvent.REFRESHED, "trigger=" + trigger);
    }

    /**
     * Log token failure.
     */
    public void logFailed(String apiName, String failureReason) {
        logEvent(apiName, TokenAuditEvent.FAILED, "reason=" + failureReason);
    }

    /**
     * Log token quarantine.
     */
    public void logQuarantined(String apiName, String reason) {
        logEvent(apiName, TokenAuditEvent.QUARANTINED, reason);
    }

    /**
     * Log token unquarantine (manual).
     */
    public void logUnquarantined(String apiName, String operator) {
        logEvent(apiName, TokenAuditEvent.UNQUARANTINED, "operator=" + operator);
    }

    /**
     * Log manual token override.
     */
    public void logManualOverride(String apiName, String operator) {
        logEvent(apiName, TokenAuditEvent.MANUAL_OVERRIDE, "operator=" + operator);
    }

    /**
     * Log token validation result.
     */
    public void logValidated(String apiName, boolean valid) {
        logEvent(apiName, TokenAuditEvent.VALIDATED, "valid=" + valid);
    }

    /**
     * Log token expiry.
     */
    public void logExpired(String apiName) {
        logEvent(apiName, TokenAuditEvent.EXPIRED, null);
    }
}
