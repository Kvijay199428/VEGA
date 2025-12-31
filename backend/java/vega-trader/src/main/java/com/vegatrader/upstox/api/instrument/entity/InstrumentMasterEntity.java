package com.vegatrader.upstox.api.instrument.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity for instrument_master table.
 * Represents BOD (Beginning of Day) instrument reference data.
 * 
 * <p>
 * This entity is:
 * <ul>
 * <li>Immutable per trading day</li>
 * <li>The single source of truth for instrument identification</li>
 * <li>Indexed for fast symbol search and autocomplete</li>
 * </ul>
 * 
 * @since 4.0.0
 */
@Entity
@Table(name = "instrument_master", indexes = {
        @Index(name = "idx_instrument_symbol_search", columnList = "trading_symbol, segment, instrument_type"),
        @Index(name = "idx_instrument_underlying", columnList = "underlying_key"),
        @Index(name = "idx_instrument_expiry", columnList = "expiry"),
        @Index(name = "idx_instrument_isin", columnList = "isin")
})
public class InstrumentMasterEntity {

    @Id
    @Column(name = "instrument_key", length = 64)
    private String instrumentKey;

    @Column(name = "segment", nullable = false, length = 16)
    private String segment;

    @Column(name = "exchange", nullable = false, length = 8)
    private String exchange;

    @Column(name = "instrument_type", nullable = false, length = 8)
    private String instrumentType;

    @Column(name = "trading_symbol", nullable = false, length = 64)
    private String tradingSymbol;

    @Column(name = "name", length = 256)
    private String name;

    @Column(name = "short_name", length = 64)
    private String shortName;

    @Column(name = "isin", length = 16)
    private String isin;

    @Column(name = "underlying_key", length = 64)
    private String underlyingKey;

    @Column(name = "underlying_symbol", length = 64)
    private String underlyingSymbol;

    @Column(name = "underlying_type", length = 16)
    private String underlyingType;

    @Column(name = "expiry")
    private LocalDate expiry;

    @Column(name = "strike_price")
    private Double strikePrice;

    @Column(name = "lot_size", nullable = false)
    private Integer lotSize = 1;

    @Column(name = "minimum_lot")
    private Integer minimumLot;

    @Column(name = "freeze_quantity")
    private Integer freezeQuantity;

    @Column(name = "tick_size")
    private Double tickSize;

    @Column(name = "exchange_token", length = 32)
    private String exchangeToken;

    @Column(name = "weekly")
    private Boolean weekly = false;

    @Column(name = "security_type", length = 16)
    private String securityType;

    @Column(name = "trading_date", nullable = false)
    private LocalDate tradingDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // V15 RMS Extension fields
    @Column(name = "equity_security_type", length = 16)
    private String equitySecurityType = "NORMAL";

    @Column(name = "exchange_series", length = 8)
    private String exchangeSeries = "EQ";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public InstrumentMasterEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
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

    public String getTradingSymbol() {
        return tradingSymbol;
    }

    public void setTradingSymbol(String tradingSymbol) {
        this.tradingSymbol = tradingSymbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getUnderlyingKey() {
        return underlyingKey;
    }

    public void setUnderlyingKey(String underlyingKey) {
        this.underlyingKey = underlyingKey;
    }

    public String getUnderlyingSymbol() {
        return underlyingSymbol;
    }

    public void setUnderlyingSymbol(String underlyingSymbol) {
        this.underlyingSymbol = underlyingSymbol;
    }

    public String getUnderlyingType() {
        return underlyingType;
    }

    public void setUnderlyingType(String underlyingType) {
        this.underlyingType = underlyingType;
    }

    public LocalDate getExpiry() {
        return expiry;
    }

    public void setExpiry(LocalDate expiry) {
        this.expiry = expiry;
    }

    public Double getStrikePrice() {
        return strikePrice;
    }

    public void setStrikePrice(Double strikePrice) {
        this.strikePrice = strikePrice;
    }

    public Integer getLotSize() {
        return lotSize;
    }

    public void setLotSize(Integer lotSize) {
        this.lotSize = lotSize;
    }

    public Integer getMinimumLot() {
        return minimumLot;
    }

    public void setMinimumLot(Integer minimumLot) {
        this.minimumLot = minimumLot;
    }

    public Integer getFreezeQuantity() {
        return freezeQuantity;
    }

    public void setFreezeQuantity(Integer freezeQuantity) {
        this.freezeQuantity = freezeQuantity;
    }

    public Double getTickSize() {
        return tickSize;
    }

    public void setTickSize(Double tickSize) {
        this.tickSize = tickSize;
    }

    public String getExchangeToken() {
        return exchangeToken;
    }

    public void setExchangeToken(String exchangeToken) {
        this.exchangeToken = exchangeToken;
    }

    public Boolean getWeekly() {
        return weekly;
    }

    public void setWeekly(Boolean weekly) {
        this.weekly = weekly;
    }

    public String getSecurityType() {
        return securityType;
    }

    public void setSecurityType(String securityType) {
        this.securityType = securityType;
    }

    public LocalDate getTradingDate() {
        return tradingDate;
    }

    public void setTradingDate(LocalDate tradingDate) {
        this.tradingDate = tradingDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getEquitySecurityType() {
        return equitySecurityType;
    }

    public void setEquitySecurityType(String equitySecurityType) {
        this.equitySecurityType = equitySecurityType;
    }

    public String getExchangeSeries() {
        return exchangeSeries;
    }

    public void setExchangeSeries(String exchangeSeries) {
        this.exchangeSeries = exchangeSeries;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // --- Utility Methods ---

    public boolean isOption() {
        return "CE".equalsIgnoreCase(instrumentType) || "PE".equalsIgnoreCase(instrumentType);
    }

    public boolean isFuture() {
        return "FUT".equalsIgnoreCase(instrumentType);
    }

    public boolean isEquity() {
        return "EQ".equalsIgnoreCase(instrumentType);
    }

    public boolean isIndex() {
        return "INDEX".equalsIgnoreCase(instrumentType);
    }

    public boolean isCallOption() {
        return "CE".equalsIgnoreCase(instrumentType);
    }

    public boolean isPutOption() {
        return "PE".equalsIgnoreCase(instrumentType);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("InstrumentMaster{key='%s', symbol='%s', type='%s'}",
                instrumentKey, tradingSymbol, instrumentType);
    }
}
