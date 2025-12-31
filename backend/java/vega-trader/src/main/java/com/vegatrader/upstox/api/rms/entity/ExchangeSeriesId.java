package com.vegatrader.upstox.api.rms.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for ExchangeSeriesEntity.
 */
public class ExchangeSeriesId implements Serializable {
    private String exchange;
    private String seriesCode;

    public ExchangeSeriesId() {
    }

    public ExchangeSeriesId(String exchange, String seriesCode) {
        this.exchange = exchange;
        this.seriesCode = seriesCode;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ExchangeSeriesId that = (ExchangeSeriesId) o;
        return Objects.equals(exchange, that.exchange) && Objects.equals(seriesCode, that.seriesCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exchange, seriesCode);
    }
}
