package com.vegatrader.upstox.api.instrument.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity for instrument_mtf overlay table.
 * Contains Margin Trading Facility rules.
 * 
 * @since 4.0.0
 */
@Entity
@Table(name = "instrument_mtf")
public class InstrumentMtfEntity {

    @Id
    @Column(name = "instrument_key", length = 64)
    private String instrumentKey;

    @Column(name = "mtf_enabled")
    private Boolean mtfEnabled = false;

    @Column(name = "mtf_bracket")
    private Double mtfBracket;

    @Column(name = "trading_date", nullable = false)
    private LocalDate tradingDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public InstrumentMtfEntity() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public Boolean getMtfEnabled() {
        return mtfEnabled;
    }

    public void setMtfEnabled(Boolean mtfEnabled) {
        this.mtfEnabled = mtfEnabled;
    }

    public Double getMtfBracket() {
        return mtfBracket;
    }

    public void setMtfBracket(Double mtfBracket) {
        this.mtfBracket = mtfBracket;
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
     * Calculates required margin for MTF order.
     * 
     * @param ltp last traded price
     * @param qty quantity
     * @return required margin
     */
    public double calculateMargin(double ltp, int qty) {
        if (mtfBracket == null || mtfBracket <= 0) {
            return ltp * qty; // Full margin if no bracket
        }
        return (ltp * qty) / mtfBracket;
    }

    @Override
    public String toString() {
        return String.format("InstrumentMTF{key='%s', enabled=%s, bracket=%.2f}",
                instrumentKey, mtfEnabled, mtfBracket);
    }
}
