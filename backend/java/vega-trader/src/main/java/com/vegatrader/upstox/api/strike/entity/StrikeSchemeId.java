package com.vegatrader.upstox.api.strike.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for StrikeSchemeEntity.
 */
public class StrikeSchemeId implements Serializable {
    private String exchange;
    private String underlying;

    public StrikeSchemeId() {
    }

    public StrikeSchemeId(String exchange, String underlying) {
        this.exchange = exchange;
        this.underlying = underlying;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        StrikeSchemeId that = (StrikeSchemeId) o;
        return Objects.equals(exchange, that.exchange) && Objects.equals(underlying, that.underlying);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exchange, underlying);
    }
}
