package com.vegatrader.upstox.api.instrument.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity for instrument_suspension table.
 * Represents instruments blocked from trading.
 * 
 * @since 4.0.0
 */
@Entity
@Table(name = "instrument_suspension")
public class InstrumentSuspensionEntity {

    @Id
    @Column(name = "instrument_key", length = 64)
    private String instrumentKey;

    @Column(name = "trading_date", nullable = false)
    private LocalDate tradingDate;

    @Column(name = "reason", length = 256)
    private String reason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public InstrumentSuspensionEntity() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public LocalDate getTradingDate() {
        return tradingDate;
    }

    public void setTradingDate(LocalDate tradingDate) {
        this.tradingDate = tradingDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("InstrumentSuspension{key='%s', reason='%s'}",
                instrumentKey, reason);
    }
}
