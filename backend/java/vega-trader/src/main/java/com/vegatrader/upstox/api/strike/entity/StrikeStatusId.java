package com.vegatrader.upstox.api.strike.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for StrikeStatusEntity.
 */
public class StrikeStatusId implements Serializable {
    private String exchange;
    private String underlying;
    private Double strikePrice;
    private String optionType;

    public StrikeStatusId() {
    }

    public StrikeStatusId(String exchange, String underlying, Double strikePrice, String optionType) {
        this.exchange = exchange;
        this.underlying = underlying;
        this.strikePrice = strikePrice;
        this.optionType = optionType;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getUnderlying() {
        return underlying;
    }

    public void setUnderlying(String underlying) {
        this.underlying = underlying;
    }

    public Double getStrikePrice() {
        return strikePrice;
    }

    public void setStrikePrice(Double strikePrice) {
        this.strikePrice = strikePrice;
    }

    public String getOptionType() {
        return optionType;
    }

    public void setOptionType(String optionType) {
        this.optionType = optionType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        StrikeStatusId that = (StrikeStatusId) o;
        return Objects.equals(exchange, that.exchange)
                && Objects.equals(underlying, that.underlying)
                && Objects.equals(strikePrice, that.strikePrice)
                && Objects.equals(optionType, that.optionType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exchange, underlying, strikePrice, optionType);
    }
}
