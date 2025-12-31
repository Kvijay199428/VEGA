package com.vegatrader.upstox.api.instrument.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity for instrument_mis overlay table.
 * Contains intraday margin and leverage rules.
 * 
 * @since 4.0.0
 */
@Entity
@Table(name = "instrument_mis")
public class InstrumentMisEntity {

    @Id
    @Column(name = "instrument_key", length = 64)
    private String instrumentKey;

    @Column(name = "intraday_margin")
    private Double intradayMargin;

    @Column(name = "intraday_leverage")
    private Double intradayLeverage;

    @Column(name = "qty_multiplier")
    private Double qtyMultiplier = 1.0;

    @Column(name = "trading_date", nullable = false)
    private LocalDate tradingDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public InstrumentMisEntity() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public Double getIntradayMargin() {
        return intradayMargin;
    }

    public void setIntradayMargin(Double intradayMargin) {
        this.intradayMargin = intradayMargin;
    }

    public Double getIntradayLeverage() {
        return intradayLeverage;
    }

    public void setIntradayLeverage(Double intradayLeverage) {
        this.intradayLeverage = intradayLeverage;
    }

    public Double getQtyMultiplier() {
        return qtyMultiplier;
    }

    public void setQtyMultiplier(Double qtyMultiplier) {
        this.qtyMultiplier = qtyMultiplier;
    }

    public LocalDate getTradingDate() {
        return tradingDate;
    }

    public void setTradingDate(LocalDate tradingDate) {
        this.tradingDate = tradingDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Calculates required margin for MIS order.
     * 
     * @param ltp last traded price
     * @param qty quantity
     * @return required margin
     */
    public double calculateMargin(double ltp, int qty) {
        double marginPct = intradayMargin != null ? intradayMargin / 100.0 : 1.0;
        return ltp * qty * marginPct;
    }

    @Override
    public String toString() {
        return String.format("InstrumentMIS{key='%s', margin=%.2f%%, leverage=%.1fx}",
                instrumentKey, intradayMargin, intradayLeverage);
    }
}
