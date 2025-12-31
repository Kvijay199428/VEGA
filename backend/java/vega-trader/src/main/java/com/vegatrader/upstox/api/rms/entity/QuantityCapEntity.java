package com.vegatrader.upstox.api.rms.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity for symbol_quantity_caps table.
 * Per-symbol max quantity and value caps.
 * 
 * @since 4.1.0
 */
@Entity
@Table(name = "symbol_quantity_caps")
public class QuantityCapEntity {

    @Id
    @Column(name = "instrument_key", length = 64)
    private String instrumentKey;

    @Column(name = "max_qty", nullable = false)
    private Integer maxQty;

    @Column(name = "max_value")
    private Double maxValue;

    @Column(name = "reason")
    private String reason;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public QuantityCapEntity() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public Integer getMaxQty() {
        return maxQty;
    }

    public void setMaxQty(Integer maxQty) {
        this.maxQty = maxQty;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // --- Utility Methods ---

    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return !effectiveDate.isAfter(today)
                && (expiryDate == null || !expiryDate.isBefore(today));
    }

    public boolean exceedsLimit(int qty, double price) {
        if (qty > maxQty)
            return true;
        if (maxValue != null && maxValue > 0 && qty * price > maxValue)
            return true;
        return false;
    }
}
