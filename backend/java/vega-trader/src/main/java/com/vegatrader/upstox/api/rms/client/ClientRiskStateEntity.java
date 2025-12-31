package com.vegatrader.upstox.api.rms.client;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity for client_risk_state table.
 * 
 * @since 4.1.0
 */
@Entity
@Table(name = "client_risk_state")
public class ClientRiskStateEntity {

    @Id
    @Column(name = "client_id", length = 32)
    private String clientId;

    @Column(name = "gross_exposure", nullable = false)
    private Double grossExposure = 0.0;

    @Column(name = "net_exposure", nullable = false)
    private Double netExposure = 0.0;

    @Column(name = "intraday_turnover", nullable = false)
    private Double intradayTurnover = 0.0;

    @Column(name = "current_mtm", nullable = false)
    private Double currentMtm = 0.0;

    @Column(name = "open_positions", nullable = false)
    private Integer openPositions = 0;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    public ClientRiskStateEntity() {
        this.lastUpdated = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Double getGrossExposure() {
        return grossExposure;
    }

    public void setGrossExposure(Double grossExposure) {
        this.grossExposure = grossExposure;
    }

    public Double getNetExposure() {
        return netExposure;
    }

    public void setNetExposure(Double netExposure) {
        this.netExposure = netExposure;
    }

    public Double getIntradayTurnover() {
        return intradayTurnover;
    }

    public void setIntradayTurnover(Double intradayTurnover) {
        this.intradayTurnover = intradayTurnover;
    }

    public Double getCurrentMtm() {
        return currentMtm;
    }

    public void setCurrentMtm(Double currentMtm) {
        this.currentMtm = currentMtm;
    }

    public Integer getOpenPositions() {
        return openPositions;
    }

    public void setOpenPositions(Integer openPositions) {
        this.openPositions = openPositions;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // --- Conversion ---

    public ClientRiskState toRecord() {
        return new ClientRiskState(
                clientId,
                grossExposure,
                netExposure,
                intradayTurnover,
                currentMtm,
                openPositions);
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}
