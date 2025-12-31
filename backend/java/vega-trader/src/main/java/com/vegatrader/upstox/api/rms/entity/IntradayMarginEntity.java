package com.vegatrader.upstox.api.rms.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity for intraday_margin_by_series table.
 * Series-based margin % and leverage.
 * 
 * @since 4.1.0
 */
@Entity
@Table(name = "intraday_margin_by_series")
@IdClass(ExchangeSeriesId.class)
public class IntradayMarginEntity {

    @Id
    @Column(name = "exchange", length = 8)
    private String exchange;

    @Id
    @Column(name = "series_code", length = 8)
    private String seriesCode;

    @Column(name = "intraday_margin_pct", nullable = false)
    private Double intradayMarginPct;

    @Column(name = "intraday_leverage", nullable = false)
    private Double intradayLeverage;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public IntradayMarginEntity() {
        this.createdAt = LocalDateTime.now();
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

    public Double getIntradayMarginPct() {
        return intradayMarginPct;
    }

    public void setIntradayMarginPct(Double intradayMarginPct) {
        this.intradayMarginPct = intradayMarginPct;
    }

    public Double getIntradayLeverage() {
        return intradayLeverage;
    }

    public void setIntradayLeverage(Double intradayLeverage) {
        this.intradayLeverage = intradayLeverage;
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

    public double calculateMargin(double price, int qty) {
        return price * qty * (intradayMarginPct / 100.0);
    }
}
