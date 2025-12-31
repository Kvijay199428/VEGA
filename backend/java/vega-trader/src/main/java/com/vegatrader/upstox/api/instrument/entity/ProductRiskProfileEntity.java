package com.vegatrader.upstox.api.instrument.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity for product_risk_profile table.
 * Defines risk parameters for different product types (CNC, MIS, MTF).
 * 
 * @since 4.0.0
 */
@Entity
@Table(name = "product_risk_profile")
public class ProductRiskProfileEntity {

    @Id
    @Column(name = "product_type", length = 8)
    private String productType;

    @Column(name = "leverage", nullable = false)
    private Double leverage;

    @Column(name = "intraday", nullable = false)
    private Boolean intraday;

    @Column(name = "carry_forward", nullable = false)
    private Boolean carryForward;

    @Column(name = "squareoff_time", length = 8)
    private String squareoffTime;

    @Column(name = "margin_pct")
    private Double marginPct;

    @Column(name = "description", length = 256)
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ProductRiskProfileEntity() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public Double getLeverage() {
        return leverage;
    }

    public void setLeverage(Double leverage) {
        this.leverage = leverage;
    }

    public Boolean getIntraday() {
        return intraday;
    }

    public void setIntraday(Boolean intraday) {
        this.intraday = intraday;
    }

    public Boolean getCarryForward() {
        return carryForward;
    }

    public void setCarryForward(Boolean carryForward) {
        this.carryForward = carryForward;
    }

    public String getSquareoffTime() {
        return squareoffTime;
    }

    public void setSquareoffTime(String squareoffTime) {
        this.squareoffTime = squareoffTime;
    }

    public Double getMarginPct() {
        return marginPct;
    }

    public void setMarginPct(Double marginPct) {
        this.marginPct = marginPct;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Calculates required margin for this product type.
     * 
     * @param ltp last traded price
     * @param qty quantity
     * @return required margin
     */
    public double calculateMargin(double ltp, int qty) {
        double marginMultiplier = marginPct != null ? marginPct / 100.0 : 1.0;
        return ltp * qty * marginMultiplier;
    }

    @Override
    public String toString() {
        return String.format("ProductRiskProfile{type='%s', leverage=%.1fx, intraday=%s}",
                productType, leverage, intraday);
    }
}
