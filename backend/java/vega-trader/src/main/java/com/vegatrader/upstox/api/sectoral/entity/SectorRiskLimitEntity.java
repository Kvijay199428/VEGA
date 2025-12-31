package com.vegatrader.upstox.api.sectoral.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity for sector_risk_limit table.
 * 
 * @since 4.3.0
 */
@Entity
@Table(name = "sector_risk_limit")
public class SectorRiskLimitEntity {

    @Id
    @Column(name = "sector_code", length = 32)
    private String sectorCode;

    @Column(name = "max_exposure")
    private Double maxExposure;

    @Column(name = "max_exposure_pct")
    private Double maxExposurePct = 100.0;

    @Column(name = "max_open_positions")
    private Integer maxOpenPositions = 100;

    @Column(name = "trading_blocked", nullable = false)
    private Boolean tradingBlocked = false;

    @Column(name = "block_reason")
    private String blockReason;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_code", insertable = false, updatable = false)
    private SectorMasterEntity sector;

    public SectorRiskLimitEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public String getSectorCode() {
        return sectorCode;
    }

    public void setSectorCode(String sectorCode) {
        this.sectorCode = sectorCode;
    }

    public Double getMaxExposure() {
        return maxExposure;
    }

    public void setMaxExposure(Double maxExposure) {
        this.maxExposure = maxExposure;
    }

    public Double getMaxExposurePct() {
        return maxExposurePct;
    }

    public void setMaxExposurePct(Double maxExposurePct) {
        this.maxExposurePct = maxExposurePct;
    }

    public Integer getMaxOpenPositions() {
        return maxOpenPositions;
    }

    public void setMaxOpenPositions(Integer maxOpenPositions) {
        this.maxOpenPositions = maxOpenPositions;
    }

    public Boolean getTradingBlocked() {
        return tradingBlocked;
    }

    public void setTradingBlocked(Boolean tradingBlocked) {
        this.tradingBlocked = tradingBlocked;
    }

    public String getBlockReason() {
        return blockReason;
    }

    public void setBlockReason(String blockReason) {
        this.blockReason = blockReason;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public SectorMasterEntity getSector() {
        return sector;
    }

    // --- Utility Methods ---

    public boolean isBlocked() {
        return Boolean.TRUE.equals(tradingBlocked);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
