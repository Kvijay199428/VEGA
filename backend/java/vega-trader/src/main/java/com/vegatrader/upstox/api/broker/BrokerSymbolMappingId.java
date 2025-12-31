package com.vegatrader.upstox.api.broker;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for BrokerSymbolMappingEntity.
 */
public class BrokerSymbolMappingId implements Serializable {
    private String brokerId;
    private String instrumentKey;

    public BrokerSymbolMappingId() {
    }

    public BrokerSymbolMappingId(String brokerId, String instrumentKey) {
        this.brokerId = brokerId;
        this.instrumentKey = instrumentKey;
    }

    public String getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(String brokerId) {
        this.brokerId = brokerId;
    }

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BrokerSymbolMappingId that = (BrokerSymbolMappingId) o;
        return Objects.equals(brokerId, that.brokerId) && Objects.equals(instrumentKey, that.instrumentKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(brokerId, instrumentKey);
    }
}
