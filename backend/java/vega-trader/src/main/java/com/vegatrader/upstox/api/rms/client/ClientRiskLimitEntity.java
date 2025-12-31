package com.vegatrader.upstox.api.rms.client;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity for client_risk_limits table.
 * 
 * @since 4.1.0
 */
@Entity
@Table(name = "client_risk_limits")
public class ClientRiskLimitEntity {

    @Id
    @Column(name = "client_id", length = 32)
    private String clientId;

    @Column(name = "max_gross_exposure", nullable = false)
    private Double maxGrossExposure;

    @Column(name = "max_net_exposure", nullable = false)
    private Double maxNetExposure;

    @Column(name = "max_order_value", nullable = false)
    private Double maxOrderValue;

    @Column(name = "max_intraday_turnover", nullable = false)
    private Double maxIntradayTurnover;

    @Column(name = "max_open_positions", nullable = false)
    private Integer maxOpenPositions;

    @Column(name = "max_intraday_loss", nullable = false)
    private Double maxIntradayLoss;

    @Column(name = "trading_enabled", nullable = false)
    private Boolean tradingEnabled = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ClientRiskLimitEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Double getMaxGrossExposure() {
        return maxGrossExposure;
    }

    public void setMaxGrossExposure(Double maxGrossExposure) {
        this.maxGrossExposure = maxGrossExposure;
    }

    public Double getMaxNetExposure() {
        return maxNetExposure;
    }

    public void setMaxNetExposure(Double maxNetExposure) {
        this.maxNetExposure = maxNetExposure;
    }

    public Double getMaxOrderValue() {
        return maxOrderValue;
    }

    public void setMaxOrderValue(Double maxOrderValue) {
        this.maxOrderValue = maxOrderValue;
    }

    public Double getMaxIntradayTurnover() {
        return maxIntradayTurnover;
    }

    public void setMaxIntradayTurnover(Double maxIntradayTurnover) {
        this.maxIntradayTurnover = maxIntradayTurnover;
    }

    public Integer getMaxOpenPositions() {
        return maxOpenPositions;
    }

    public void setMaxOpenPositions(Integer maxOpenPositions) {
        this.maxOpenPositions = maxOpenPositions;
    }

    public Double getMaxIntradayLoss() {
        return maxIntradayLoss;
    }

    public void setMaxIntradayLoss(Double maxIntradayLoss) {
        this.maxIntradayLoss = maxIntradayLoss;
    }

    public Boolean getTradingEnabled() {
        return tradingEnabled;
    }

    public void setTradingEnabled(Boolean tradingEnabled) {
        this.tradingEnabled = tradingEnabled;
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

    // --- Conversion ---

    public ClientRiskLimit toRecord() {
        return new ClientRiskLimit(
                clientId,
                maxGrossExposure,
                maxNetExposure,
                maxOrderValue,
                maxIntradayTurnover,
                maxOpenPositions,
                maxIntradayLoss,
                Boolean.TRUE.equals(tradingEnabled));
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
