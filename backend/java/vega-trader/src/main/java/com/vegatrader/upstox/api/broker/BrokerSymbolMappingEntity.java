package com.vegatrader.upstox.api.broker;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity for broker_symbol_mapping table.
 * 
 * @since 4.2.0
 */
@Entity
@Table(name = "broker_symbol_mapping")
@IdClass(BrokerSymbolMappingId.class)
public class BrokerSymbolMappingEntity {

    @Id
    @Column(name = "broker_id", length = 32)
    private String brokerId;

    @Id
    @Column(name = "instrument_key", length = 64)
    private String instrumentKey;

    @Column(name = "broker_symbol", nullable = false)
    private String brokerSymbol;

    @Column(name = "broker_token", length = 32)
    private String brokerToken;

    @Column(name = "tradeable", nullable = false)
    private Boolean tradeable = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public BrokerSymbolMappingEntity() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

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

    public String getBrokerSymbol() {
        return brokerSymbol;
    }

    public void setBrokerSymbol(String brokerSymbol) {
        this.brokerSymbol = brokerSymbol;
    }

    public String getBrokerToken() {
        return brokerToken;
    }

    public void setBrokerToken(String brokerToken) {
        this.brokerToken = brokerToken;
    }

    public Boolean getTradeable() {
        return tradeable;
    }

    public void setTradeable(Boolean tradeable) {
        this.tradeable = tradeable;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BrokerSymbolMapping toRecord() {
        return new BrokerSymbolMapping(brokerId, instrumentKey, brokerSymbol, brokerToken,
                Boolean.TRUE.equals(tradeable));
    }
}
