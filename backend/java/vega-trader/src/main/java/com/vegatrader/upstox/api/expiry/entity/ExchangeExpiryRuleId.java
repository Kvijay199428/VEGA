package com.vegatrader.upstox.api.expiry.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for ExchangeExpiryRuleEntity.
 */
public class ExchangeExpiryRuleId implements Serializable {
    private String exchange;
    private String instrumentType;
    private String cycleType;

    public ExchangeExpiryRuleId() {
    }

    public ExchangeExpiryRuleId(String exchange, String instrumentType, String cycleType) {
        this.exchange = exchange;
        this.instrumentType = instrumentType;
        this.cycleType = cycleType;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ExchangeExpiryRuleId that = (ExchangeExpiryRuleId) o;
        return Objects.equals(exchange, that.exchange)
                && Objects.equals(instrumentType, that.instrumentType)
                && Objects.equals(cycleType, that.cycleType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exchange, instrumentType, cycleType);
    }
}
