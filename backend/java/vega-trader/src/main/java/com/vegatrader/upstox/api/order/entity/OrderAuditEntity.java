package com.vegatrader.upstox.api.order.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Order Audit Entity.
 * Immutable append-only trail for every order lifecycle event.
 * Per a3.md Section 1.3 - SEBI Critical.
 * 
 * @since 5.0.0
 */
@Entity
@Table(name = "order_audit")
public class OrderAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    @Column(name = "event_type", nullable = false, length = 32)
    private String eventType; // PLACE, MODIFY, CANCEL, REJECT, FILL, PARTIAL_FILL

    @Column(name = "old_snapshot", columnDefinition = "TEXT")
    private String oldSnapshot; // JSON of previous state

    @Column(name = "new_snapshot", columnDefinition = "TEXT")
    private String newSnapshot; // JSON of new state

    @Column(name = "actor_type", length = 16)
    private String actorType; // USER, SYSTEM, ADMIN

    @Column(name = "actor_id", length = 64)
    private String actorId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 256)
    private String userAgent;

    @Column(name = "event_ts", nullable = false)
    private Instant eventTimestamp;

    @Column(name = "broker_code", length = 16)
    private String brokerCode;

    @Column(name = "latency_ms")
    private Long latencyMs;

    @Column(name = "integrity_hash", length = 64)
    private String integrityHash;

    // Default constructor
    public OrderAuditEntity() {
        this.eventTimestamp = Instant.now();
    }

    // Factory for order events
    public static OrderAuditEntity create(String orderId, String eventType,
            String actorId, String actorType) {
        OrderAuditEntity entity = new OrderAuditEntity();
        entity.orderId = orderId;
        entity.eventType = eventType;
        entity.actorId = actorId;
        entity.actorType = actorType;
        return entity;
    }

    // Getters
    public Long getAuditId() {
        return auditId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getOldSnapshot() {
        return oldSnapshot;
    }

    public String getNewSnapshot() {
        return newSnapshot;
    }

    public String getActorType() {
        return actorType;
    }

    public String getActorId() {
        return actorId;
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

    public String getBrokerCode() {
        return brokerCode;
    }

    public Long getLatencyMs() {
        return latencyMs;
    }

    public String getIntegrityHash() {
        return integrityHash;
    }

    // Setters
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setOldSnapshot(String oldSnapshot) {
        this.oldSnapshot = oldSnapshot;
    }

    public void setNewSnapshot(String newSnapshot) {
        this.newSnapshot = newSnapshot;
    }

    public void setActorType(String actorType) {
        this.actorType = actorType;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
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

    public void setBrokerCode(String brokerCode) {
        this.brokerCode = brokerCode;
    }

    public void setLatencyMs(Long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public void setIntegrityHash(String integrityHash) {
        this.integrityHash = integrityHash;
    }
}
