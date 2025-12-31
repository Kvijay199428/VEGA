package com.vegatrader.upstox.api.admin.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Broker Registry Entity.
 * Stores broker configurations and priority settings.
 * 
 * @since 5.0.0
 */
@Entity
@Table(name = "broker_registry")
public class BrokerRegistryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "broker_code", nullable = false, unique = true, length = 32)
    private String brokerCode; // UPSTOX, ZERODHA, FYERS

    @Column(name = "broker_name", nullable = false, length = 128)
    private String brokerName;

    @Column(name = "exchange", nullable = false, length = 16)
    private String exchange; // NSE, BSE, MCX

    @Column(name = "instrument_type", length = 16)
    private String instrumentType; // EQ, FO, CD

    @Column(name = "priority", nullable = false)
    private Integer priority = 1; // 1 = highest priority

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "max_order_value")
    private Long maxOrderValue;

    @Column(name = "rate_limit_per_minute")
    private Integer rateLimitPerMinute;

    @Column(name = "supports_multi_order")
    private Boolean supportsMultiOrder = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    // Default constructor
    public BrokerRegistryEntity() {
        this.createdAt = Instant.now();
        this.isActive = true;
    }

    // Factory
    public static BrokerRegistryEntity create(String brokerCode, String brokerName,
            String exchange, int priority) {
        BrokerRegistryEntity entity = new BrokerRegistryEntity();
        entity.brokerCode = brokerCode;
        entity.brokerName = brokerName;
        entity.exchange = exchange;
        entity.priority = priority;
        return entity;
    }

    // Update priority
    public void updatePriority(int newPriority, String updatedBy) {
        this.priority = newPriority;
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getBrokerCode() {
        return brokerCode;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public String getExchange() {
        return exchange;
    }

    public String getInstrumentType() {
        return instrumentType;
    }

    public Integer getPriority() {
        return priority;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public Long getMaxOrderValue() {
        return maxOrderValue;
    }

    public Integer getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }

    public Boolean getSupportsMultiOrder() {
        return supportsMultiOrder;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    // Setters
    public void setBrokerCode(String brokerCode) {
        this.brokerCode = brokerCode;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public void setInstrumentType(String instrumentType) {
        this.instrumentType = instrumentType;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public void setMaxOrderValue(Long maxOrderValue) {
        this.maxOrderValue = maxOrderValue;
    }

    public void setRateLimitPerMinute(Integer rateLimitPerMinute) {
        this.rateLimitPerMinute = rateLimitPerMinute;
    }

    public void setSupportsMultiOrder(Boolean supportsMultiOrder) {
        this.supportsMultiOrder = supportsMultiOrder;
    }
}
