package com.vegatrader.upstox.api.rms.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity for fo_contract_lifecycle table.
 * Tracks F&O contract expiry and rollover.
 * 
 * @since 4.1.0
 */
@Entity
@Table(name = "fo_contract_lifecycle")
public class FoContractLifecycleEntity {

    @Id
    @Column(name = "instrument_key", length = 64)
    private String instrumentKey;

    @Column(name = "underlying_key", nullable = false)
    private String underlyingKey;

    @Column(name = "underlying_symbol")
    private String underlyingSymbol;

    @Column(name = "instrument_type", length = 8, nullable = false)
    private String instrumentType;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "strike_price")
    private Double strikePrice;

    @Column(name = "lot_size")
    private Integer lotSize;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public FoContractLifecycleEntity() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public String getUnderlyingKey() {
        return underlyingKey;
    }

    public void setUnderlyingKey(String underlyingKey) {
        this.underlyingKey = underlyingKey;
    }

    public String getUnderlyingSymbol() {
        return underlyingSymbol;
    }

    public void setUnderlyingSymbol(String underlyingSymbol) {
        this.underlyingSymbol = underlyingSymbol;
    }

    public String getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(String instrumentType) {
        this.instrumentType = instrumentType;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Double getStrikePrice() {
        return strikePrice;
    }

    public void setStrikePrice(Double strikePrice) {
        this.strikePrice = strikePrice;
    }

    public Integer getLotSize() {
        return lotSize;
    }

    public void setLotSize(Integer lotSize) {
        this.lotSize = lotSize;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // --- Utility Methods ---

    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    public boolean isFuture() {
        return "FUT".equalsIgnoreCase(instrumentType);
    }

    public boolean isOption() {
        return "CE".equalsIgnoreCase(instrumentType) || "PE".equalsIgnoreCase(instrumentType);
    }

    public int daysToExpiry() {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }
}
