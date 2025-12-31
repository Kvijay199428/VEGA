package com.vegatrader.upstox.api.admin.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Admin Actions Audit Entity.
 * Tracks all administrative actions for SEBI compliance.
 * Append-only, immutable audit trail.
 * 
 * @since 5.0.0
 */
@Entity
@Table(name = "admin_actions_audit")
public class AdminActionAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @Column(name = "action_type", nullable = false, length = 64)
    private String actionType; // STRIKE_DISABLE, STRIKE_ENABLE, BROKER_PRIORITY, CONTRACT_ROLLBACK, EXPORT

    @Column(name = "target_entity", length = 64)
    private String targetEntity; // e.g., "NIFTY 50 CE 24000", "UPSTOX"

    @Column(name = "target_id", length = 128)
    private String targetId; // Specific identifier

    @Column(name = "old_value", length = 1024)
    private String oldValue; // JSON of previous state

    @Column(name = "new_value", length = 1024)
    private String newValue; // JSON of new state

    @Column(name = "reason", length = 512)
    private String reason;

    @Column(name = "performed_by", nullable = false, length = 64)
    private String performedBy;

    @Column(name = "performer_role", length = 32)
    private String performerRole; // ADMIN, SUPER_ADMIN, SYSTEM

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 256)
    private String userAgent;

    @Column(name = "performed_at", nullable = false)
    private Instant performedAt;

    @Column(name = "success")
    private Boolean success;

    @Column(name = "error_message", length = 512)
    private String errorMessage;

    @Column(name = "integrity_hash", length = 64)
    private String integrityHash;

    // Default constructor
    public AdminActionAuditEntity() {
        this.performedAt = Instant.now();
    }

    // Builder-style factory
    public static AdminActionAuditEntity create(String actionType, String targetEntity,
            String targetId, String performedBy) {
        AdminActionAuditEntity entity = new AdminActionAuditEntity();
        entity.actionType = actionType;
        entity.targetEntity = targetEntity;
        entity.targetId = targetId;
        entity.performedBy = performedBy;
        entity.success = true;
        return entity;
    }

    // Getters
    public Long getAuditId() {
        return auditId;
    }

    public String getActionType() {
        return actionType;
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getReason() {
        return reason;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public String getPerformerRole() {
        return performerRole;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Instant getPerformedAt() {
        return performedAt;
    }

    public Boolean getSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getIntegrityHash() {
        return integrityHash;
    }

    // Setters
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public void setPerformerRole(String performerRole) {
        this.performerRole = performerRole;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setPerformedAt(Instant performedAt) {
        this.performedAt = performedAt;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setIntegrityHash(String integrityHash) {
        this.integrityHash = integrityHash;
    }
}
