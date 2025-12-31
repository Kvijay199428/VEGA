package com.vegatrader.upstox.api.rms.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for IpoCalendarEntity.
 */
public class IpoCalendarId implements Serializable {
    private String symbol;
    private String exchange;

    public IpoCalendarId() {
    }

    public IpoCalendarId(String symbol, String exchange) {
        this.symbol = symbol;
        this.exchange = exchange;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        IpoCalendarId that = (IpoCalendarId) o;
        return Objects.equals(symbol, that.symbol) && Objects.equals(exchange, that.exchange);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, exchange);
    }
}
