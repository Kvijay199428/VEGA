package com.vegatrader.upstox.api.order.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Audit Event JPA entity for order lifecycle tracking.
 * Per b2.md section 6.
 * 
 * @since 4.9.0
 */
@Entity
@Table(name = "order_audit_events", indexes = {
        @Index(name = "idx_audit_order_id", columnList = "order_id"),
        @Index(name = "idx_audit_user_id", columnList = "user_id"),
        @Index(name = "idx_audit_event_type", columnList = "event_type"),
        @Index(name = "idx_audit_created_at", columnList = "created_at")
})
public class AuditEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 50)
    private String eventId;

    @Column(name = "order_id", nullable = false, length = 50)
    private String orderId;

    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "previous_state", length = 30)
    private String previousState;

    @Column(name = "new_state", length = 30)
    private String newState;

    @Column(name = "source", length = 20)
    private String source; // SYSTEM, EXCHANGE, USER

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload; // JSON

    @Column(name = "request_id", length = 50)
    private String requestId;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "data_source", length = 20)
    private String dataSource; // CACHE, DB, BROKER

    @Column(name = "response_hash", length = 100)
    private String responseHash;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (eventId == null) {
            eventId = "EVT-" + System.currentTimeMillis() + "-" + orderId.hashCode();
        }
    }

    // Static factory
    public static AuditEventEntity create(String orderId, String eventType, String payload) {
        AuditEventEntity event = new AuditEventEntity();
        event.setOrderId(orderId);
        event.setEventType(eventType);
        event.setPayload(payload);
        event.setSource("SYSTEM");
        return event;
    }

    public static AuditEventEntity stateChange(String orderId, String userId,
            String prevState, String newState) {
        AuditEventEntity event = new AuditEventEntity();
        event.setOrderId(orderId);
        event.setUserId(userId);
        event.setEventType("STATE_CHANGE");
        event.setPreviousState(prevState);
        event.setNewState(newState);
        event.setSource("SYSTEM");
        return event;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getPreviousState() {
        return previousState;
    }

    public void setPreviousState(String previousState) {
        this.previousState = previousState;
    }

    public String getNewState() {
        return newState;
    }

    public void setNewState(String newState) {
        this.newState = newState;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getResponseHash() {
        return responseHash;
    }

    public void setResponseHash(String responseHash) {
        this.responseHash = responseHash;
    }

    public Integer getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Integer latencyMs) {
        this.latencyMs = latencyMs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
