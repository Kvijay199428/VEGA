package com.vegatrader.upstox.api.expiry.entity;

import jakarta.persistence.*;

/**
 * JPA Entity for exchange_expiry_rule table.
 * Per a1.md section 1.3.
 * 
 * @since 4.5.0
 */
@Entity
@Table(name = "exchange_expiry_rule")
@IdClass(ExchangeExpiryRuleId.class)
public class ExchangeExpiryRuleEntity {

    @Id
    @Column(name = "exchange", length = 10)
    private String exchange;

    @Id
    @Column(name = "instrument_type", length = 20)
    private String instrumentType; // FUTIDX / OPTIDX / FUTSTK / OPTSTK

    @Id
    @Column(name = "cycle_type", length = 20)
    private String cycleType; // WEEKLY / MONTHLY / QUARTERLY / HALF_YEARLY

    @Column(name = "expiry_day", nullable = false, length = 10)
    private String expiryDay; // MONDAY / TUESDAY / THURSDAY / FRIDAY

    @Column(name = "fallback_strategy", nullable = false, length = 30)
    private String fallbackStrategy; // PREVIOUS_TRADING_DAY / NEXT_TRADING_DAY

    @Column(name = "active")
    private Boolean active = true;

    // --- Getters and Setters ---

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(String instrumentType) {
        this.instrumentType = instrumentType;
    }

    public String getCycleType() {
        return cycleType;
    }

    public void setCycleType(String cycleType) {
        this.cycleType = cycleType;
    }

    public String getExpiryDay() {
        return expiryDay;
    }

    public void setExpiryDay(String expiryDay) {
        this.expiryDay = expiryDay;
    }

    public String getFallbackStrategy() {
        return fallbackStrategy;
    }

    public void setFallbackStrategy(String fallbackStrategy) {
        this.fallbackStrategy = fallbackStrategy;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    // --- Utility ---

    public boolean isWeekly() {
        return "WEEKLY".equalsIgnoreCase(cycleType);
    }

    public boolean isMonthly() {
        return "MONTHLY".equalsIgnoreCase(cycleType);
    }

    public boolean usePreviousTradingDay() {
        return "PREVIOUS_TRADING_DAY".equalsIgnoreCase(fallbackStrategy);
    }
}
