package com.vegatrader.upstox.api.optionchain.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Option Chain Audit Entity.
 * Tracks every option chain fetch for SEBI compliance.
 * Append-only audit trail.
 * 
 * @since 5.0.0
 */
@Entity
@Table(name = "option_chain_audit")
public class OptionChainAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @Column(name = "instrument_key", nullable = false, length = 64)
    private String instrumentKey;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "fetch_source", length = 16)
    private String fetchSource; // API, CACHE, FALLBACK

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "strike_count")
    private Integer strikeCount;

    @Column(name = "spot_price")
    private Double spotPrice;

    @Column(name = "fetch_latency_ms")
    private Long fetchLatencyMs;

    @Column(name = "user_id", length = 64)
    private String userId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 256)
    private String userAgent;

    @Column(name = "fetch_ts", nullable = false)
    private Instant fetchTimestamp;

    @Column(name = "error_message", length = 512)
    private String errorMessage;

    @Column(name = "integrity_hash", length = 64)
    private String integrityHash;

    // Default constructor
    public OptionChainAuditEntity() {
        this.fetchTimestamp = Instant.now();
    }

    // Factory for success
    public static OptionChainAuditEntity success(String instrumentKey, LocalDate expiry,
            String source, int strikeCount, double spotPrice, long latencyMs) {
        OptionChainAuditEntity entity = new OptionChainAuditEntity();
        entity.instrumentKey = instrumentKey;
        entity.expiryDate = expiry;
        entity.fetchSource = source;
        entity.statusCode = 200;
        entity.strikeCount = strikeCount;
        entity.spotPrice = spotPrice;
        entity.fetchLatencyMs = latencyMs;
        return entity;
    }

    // Factory for error
    public static OptionChainAuditEntity error(String instrumentKey, LocalDate expiry,
            String source, int statusCode, String errorMessage) {
        OptionChainAuditEntity entity = new OptionChainAuditEntity();
        entity.instrumentKey = instrumentKey;
        entity.expiryDate = expiry;
        entity.fetchSource = source;
        entity.statusCode = statusCode;
        entity.errorMessage = errorMessage;
        return entity;
    }

    // Getters
    public Long getAuditId() {
        return auditId;
    }

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public String getFetchSource() {
        return fetchSource;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public Integer getStrikeCount() {
        return strikeCount;
    }

    public Double getSpotPrice() {
        return spotPrice;
    }

    public Long getFetchLatencyMs() {
        return fetchLatencyMs;
    }

    public String getUserId() {
        return userId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Instant getFetchTimestamp() {
        return fetchTimestamp;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getIntegrityHash() {
        return integrityHash;
    }

    // Setters
    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setFetchSource(String fetchSource) {
        this.fetchSource = fetchSource;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public void setStrikeCount(Integer strikeCount) {
        this.strikeCount = strikeCount;
    }

    public void setSpotPrice(Double spotPrice) {
        this.spotPrice = spotPrice;
    }

    public void setFetchLatencyMs(Long fetchLatencyMs) {
        this.fetchLatencyMs = fetchLatencyMs;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setFetchTimestamp(Instant fetchTimestamp) {
        this.fetchTimestamp = fetchTimestamp;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setIntegrityHash(String integrityHash) {
        this.integrityHash = integrityHash;
    }
}
