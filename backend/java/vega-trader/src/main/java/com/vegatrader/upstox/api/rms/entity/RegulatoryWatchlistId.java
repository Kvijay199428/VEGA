package com.vegatrader.upstox.api.rms.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for RegulatoryWatchlistEntity.
 */
public class RegulatoryWatchlistId implements Serializable {
    private String exchange;
    private String symbol;
    private String watchType;

    public RegulatoryWatchlistId() {
    }

    public RegulatoryWatchlistId(String exchange, String symbol, String watchType) {
        this.exchange = exchange;
        this.symbol = symbol;
        this.watchType = watchType;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getWatchType() {
        return watchType;
    }

    public void setWatchType(String watchType) {
        this.watchType = watchType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RegulatoryWatchlistId that = (RegulatoryWatchlistId) o;
        return Objects.equals(exchange, that.exchange)
                && Objects.equals(symbol, that.symbol)
                && Objects.equals(watchType, that.watchType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exchange, symbol, watchType);
    }
}
