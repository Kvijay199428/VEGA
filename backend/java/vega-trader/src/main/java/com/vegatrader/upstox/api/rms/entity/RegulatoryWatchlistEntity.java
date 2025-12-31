package com.vegatrader.upstox.api.rms.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity for regulatory_watchlist table.
 * Tracks PCA, Surveillance, ASM, GSM entries.
 * 
 * @since 4.1.0
 */
@Entity
@Table(name = "regulatory_watchlist")
@IdClass(RegulatoryWatchlistId.class)
public class RegulatoryWatchlistEntity {

    @Id
    @Column(name = "exchange", length = 8)
    private String exchange;

    @Id
    @Column(name = "symbol", length = 32)
    private String symbol;

    @Id
    @Column(name = "watch_type", length = 16)
    private String watchType;

    @Column(name = "stage", length = 16)
    private String stage;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "reason")
    private String reason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public RegulatoryWatchlistEntity() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getWatchType() {
        return watchType;
    }

    public void setWatchType(String watchType) {
        this.watchType = watchType;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // --- Utility Methods ---

    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return effectiveDate != null && !effectiveDate.isAfter(today)
                && (expiryDate == null || !expiryDate.isBefore(today));
    }

    public boolean isPca() {
        return "PCA".equalsIgnoreCase(watchType);
    }

    public boolean isSurveillance() {
        return "SURVEILLANCE".equalsIgnoreCase(watchType) || "ASM".equalsIgnoreCase(watchType)
                || "GSM".equalsIgnoreCase(watchType);
    }
}
