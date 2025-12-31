package com.vegatrader.upstox.auth.service;

import com.vegatrader.upstox.auth.entity.TokenAuditEntity;
import com.vegatrader.upstox.auth.repository.TokenAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

/**
 * Token Audit Service.
 * Provides append-only audit logging for token lifecycle events.
 * Per a1.md Section 1 - SEBI-auditable token traceability.
 * 
 * @since 5.0.0
 */
@Service
public class TokenAuditService {

    private static final Logger logger = LoggerFactory.getLogger(TokenAuditService.class);

    private final TokenAuditRepository auditRepository;

    @Autowired
    public TokenAuditService(TokenAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    /**
     * Log a token issuance event.
     */
    public void logTokenIssued(String apiName, String token, String actorId, String actorType, String ipAddress) {
        logEvent(apiName, "ISSUED", token, actorId, actorType, ipAddress, null);
    }

    /**
     * Log a token refresh event.
     */
    public void logTokenRefreshed(String apiName, String oldToken, String newToken, String actorId, String ipAddress) {
        String details = String.format("{\"old_hash\":\"%s\",\"new_hash\":\"%s\"}",
                hashToken(oldToken), hashToken(newToken));
        logEvent(apiName, "REFRESHED", newToken, actorId, "SYSTEM", ipAddress, details);
    }

    /**
     * Log a token revocation event.
     */
    public void logTokenRevoked(String apiName, String token, String actorId, String actorType, String ipAddress,
            String reason) {
        String details = String.format("{\"reason\":\"%s\"}", reason);
        logEvent(apiName, "REVOKED", token, actorId, actorType, ipAddress, details);
    }

    /**
     * Log a token expiration event.
     */
    public void logTokenExpired(String apiName, String token) {
        logEvent(apiName, "EXPIRED", token, "SYSTEM", "SCHEDULER", null, null);
    }

    /**
     * Log a health check failure.
     */
    public void logHealthCheckFailed(String apiName, String token, String errorMessage) {
        String details = String.format("{\"error\":\"%s\"}", errorMessage);
        logEvent(apiName, "HEALTH_CHECK_FAIL", token, "SYSTEM", "HEALTH_CHECKER", null, details);
    }

    /**
     * Get audit trail for an API.
     */
    public List<TokenAuditEntity> getAuditTrail(String apiName) {
        return auditRepository.findByApiNameOrderByEventTimestampDesc(apiName);
    }

    /**
     * Get recent audit events.
     */
    public List<TokenAuditEntity> getRecentAuditEvents(String apiName, int limit) {
        return auditRepository.findRecentByApiName(apiName, limit);
    }

    /**
     * Get audit events within a time range.
     */
    public List<TokenAuditEntity> getAuditByTimeRange(Instant start, Instant end) {
        return auditRepository.findByTimeRange(start, end);
    }

    /**
     * Get all health check failures.
     */
    public List<TokenAuditEntity> getHealthCheckFailures() {
        return auditRepository.findHealthCheckFailures();
    }

    // === Private Helpers ===

    private void logEvent(String apiName, String eventType, String token,
            String actorId, String actorType, String ipAddress, String details) {
        try {
            TokenAuditEntity audit = new TokenAuditEntity();
            audit.setApiName(apiName);
            audit.setEventType(eventType);
            audit.setTokenHash(hashToken(token));
            audit.setActorId(actorId);
            audit.setActorType(actorType);
            audit.setIpAddress(ipAddress);
            audit.setDetails(details);
            audit.setEventTimestamp(Instant.now());
            audit.setIntegrityHash(computeIntegrityHash(audit));

            auditRepository.save(audit);
            logger.info("Token audit logged: {} {} for {}", eventType, apiName, actorId);
        } catch (Exception e) {
            logger.error("Failed to log token audit: {}", e.getMessage());
        }
    }

    private String hashToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256 not available");
            return null;
        }
    }

    private String computeIntegrityHash(TokenAuditEntity audit) {
        String data = String.format("%s|%s|%s|%s|%s",
                audit.getApiName(), audit.getEventType(), audit.getTokenHash(),
                audit.getActorId(), audit.getEventTimestamp());
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
