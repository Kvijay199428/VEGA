package com.vegatrader.upstox.api.rms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity for exchange_series table.
 * Defines series/group with settlement and product eligibility flags.
 * 
 * @since 4.1.0
 */
@Entity
@Table(name = "exchange_series")
@IdClass(ExchangeSeriesId.class)
public class ExchangeSeriesEntity {

    @Id
    @Column(name = "exchange", length = 8)
    private String exchange;

    @Id
    @Column(name = "series_code", length = 8)
    private String seriesCode;

    @Column(name = "security_class", length = 16)
    private String securityClass = "EQUITY";

    @Column(name = "rolling_settlement")
    private Boolean rollingSettlement = true;

    @Column(name = "trade_for_trade")
    private Boolean tradeForTrade = false;

    @Column(name = "gross_settlement")
    private Boolean grossSettlement = false;

    @Column(name = "surveillance")
    private Boolean surveillance = false;

    @Column(name = "mis_allowed")
    private Boolean misAllowed = true;

    @Column(name = "mtf_allowed")
    private Boolean mtfAllowed = true;

    @Column(name = "description")
    private String description;

    public ExchangeSeriesEntity() {
    }

    // --- Getters and Setters ---

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getSeriesCode() {
        return seriesCode;
    }

    public void setSeriesCode(String seriesCode) {
        this.seriesCode = seriesCode;
    }

    public String getSecurityClass() {
        return securityClass;
    }

    public void setSecurityClass(String securityClass) {
        this.securityClass = securityClass;
    }

    public Boolean getRollingSettlement() {
        return rollingSettlement;
    }

    public void setRollingSettlement(Boolean rollingSettlement) {
        this.rollingSettlement = rollingSettlement;
    }

    public Boolean getTradeForTrade() {
        return tradeForTrade;
    }

    public void setTradeForTrade(Boolean tradeForTrade) {
        this.tradeForTrade = tradeForTrade;
    }

    public Boolean getGrossSettlement() {
        return grossSettlement;
    }

    public void setGrossSettlement(Boolean grossSettlement) {
        this.grossSettlement = grossSettlement;
    }

    public Boolean getSurveillance() {
        return surveillance;
    }

    public void setSurveillance(Boolean surveillance) {
        this.surveillance = surveillance;
    }

    public Boolean getMisAllowed() {
        return misAllowed;
    }

    public void setMisAllowed(Boolean misAllowed) {
        this.misAllowed = misAllowed;
    }

    public Boolean getMtfAllowed() {
        return mtfAllowed;
    }

    public void setMtfAllowed(Boolean mtfAllowed) {
        this.mtfAllowed = mtfAllowed;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // --- Utility Methods ---

    public boolean isIntradayAllowed() {
        return Boolean.TRUE.equals(rollingSettlement)
                && !Boolean.TRUE.equals(tradeForTrade)
                && Boolean.TRUE.equals(misAllowed);
    }

    @Override
    public String toString() {
        return String.format("ExchangeSeries{%s:%s, T2T=%s}", exchange, seriesCode, tradeForTrade);
    }
}
