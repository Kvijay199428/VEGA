package com.vegatrader.upstox.auth.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Token Audit Entity.
 * Tracks token issuance, refresh, and revocation per SEBI requirements.
 * Append-only, immutable audit trail.
 * 
 * @since 5.0.0
 */
@Entity
@Table(name = "token_audit")
public class TokenAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @Column(name = "api_name", nullable = false, length = 64)
    private String apiName;

    @Column(name = "event_type", nullable = false, length = 32)
    private String eventType; // ISSUED, REFRESHED, REVOKED, EXPIRED, HEALTH_CHECK_FAIL

    @Column(name = "token_hash", length = 64)
    private String tokenHash; // SHA-256 of token (never store raw token)

    @Column(name = "actor_id", length = 64)
    private String actorId; // User or System identifier

    @Column(name = "actor_type", length = 16)
    private String actorType; // USER, SYSTEM, SCHEDULER

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 256)
    private String userAgent;

    @Column(name = "event_ts", nullable = false)
    private Instant eventTimestamp;

    @Column(name = "details", length = 1024)
    private String details; // JSON or text for additional context

    @Column(name = "integrity_hash", length = 64)
    private String integrityHash; // SHA-256 of row for tamper detection

    // Default constructor
    public TokenAuditEntity() {
        this.eventTimestamp = Instant.now();
    }

    // Builder-style constructor
    public TokenAuditEntity(String apiName, String eventType, String tokenHash,
            String actorId, String actorType, String ipAddress) {
        this();
        this.apiName = apiName;
        this.eventType = eventType;
        this.tokenHash = tokenHash;
        this.actorId = actorId;
        this.actorType = actorType;
        this.ipAddress = ipAddress;
    }

    // Getters
    public Long getAuditId() {
        return auditId;
    }

    public String getApiName() {
        return apiName;
    }

    public String getEventType() {
        return eventType;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public String getActorId() {
        return actorId;
    }

    public String getActorType() {
        return actorType;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Instant getEventTimestamp() {
        return eventTimestamp;
    }

    public String getDetails() {
        return details;
    }

    public String getIntegrityHash() {
        return integrityHash;
    }

    // Setters
    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public void setActorType(String actorType) {
        this.actorType = actorType;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setEventTimestamp(Instant eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setIntegrityHash(String integrityHash) {
        this.integrityHash = integrityHash;
    }
}
