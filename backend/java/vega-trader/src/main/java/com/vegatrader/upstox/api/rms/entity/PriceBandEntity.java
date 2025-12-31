package com.vegatrader.upstox.api.rms.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity for price_band table.
 * Dynamic daily price limits from exchange.
 * 
 * @since 4.1.0
 */
@Entity
@Table(name = "price_band")
public class PriceBandEntity {

    @Id
    @Column(name = "instrument_key", length = 64)
    private String instrumentKey;

    @Column(name = "lower_price", nullable = false)
    private Double lowerPrice;

    @Column(name = "upper_price", nullable = false)
    private Double upperPrice;

    @Column(name = "lower_pct")
    private Double lowerPct;

    @Column(name = "upper_pct")
    private Double upperPct;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public PriceBandEntity() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public Double getLowerPrice() {
        return lowerPrice;
    }

    public void setLowerPrice(Double lowerPrice) {
        this.lowerPrice = lowerPrice;
    }

    public Double getUpperPrice() {
        return upperPrice;
    }

    public void setUpperPrice(Double upperPrice) {
        this.upperPrice = upperPrice;
    }

    public Double getLowerPct() {
        return lowerPct;
    }

    public void setLowerPct(Double lowerPct) {
        this.lowerPct = lowerPct;
    }

    public Double getUpperPct() {
        return upperPct;
    }

    public void setUpperPct(Double upperPct) {
        this.upperPct = upperPct;
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

    // --- Utility Methods ---

    public boolean isWithinBand(double price) {
        return price >= lowerPrice && price <= upperPrice;
    }

    public boolean isOutsideBand(double price) {
        return price < lowerPrice || price > upperPrice;
    }
}
