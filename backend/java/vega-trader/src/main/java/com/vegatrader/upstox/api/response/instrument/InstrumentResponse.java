package com.vegatrader.upstox.api.response.instrument;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

/**
 * Response DTO for instrument data.
 * Used for parsing instrument master CSV/JSON files.
 * 
 * <p>
 * Updated in v4.0.0 with:
 * <ul>
 * <li>Additional fields from Upstox BOD data</li>
 * <li>Overlay flags (mtfEnabled, misAllowed, suspended)</li>
 * <li>Jackson annotations for API responses</li>
 * </ul>
 *
 * @since 2.0.0
 */
public class InstrumentResponse {

    @SerializedName("instrument_key")
    @JsonProperty("instrument_key")
    private String instrumentKey;

    @SerializedName("exchange_token")
    @JsonProperty("exchange_token")
    private String exchangeToken;

    @SerializedName("trading_symbol")
    @JsonProperty("trading_symbol")
    private String tradingSymbol;

    @SerializedName("name")
    @JsonProperty("name")
    private String name;

    @SerializedName("short_name")
    @JsonProperty("short_name")
    private String shortName;

    @SerializedName("segment")
    @JsonProperty("segment")
    private String segment;

    @SerializedName("exchange")
    @JsonProperty("exchange")
    private String exchange;

    @SerializedName("isin")
    @JsonProperty("isin")
    private String isin;

    @SerializedName("expiry")
    @JsonProperty("expiry")
    private String expiry;

    @SerializedName("strike")
    @JsonProperty("strike")
    private Double strike;

    @SerializedName("lot_size")
    @JsonProperty("lot_size")
    private Integer lotSize;

    @SerializedName("minimum_lot")
    @JsonProperty("minimum_lot")
    private Integer minimumLot;

    @SerializedName("freeze_quantity")
    @JsonProperty("freeze_quantity")
    private Integer freezeQuantity;

    @SerializedName("instrument_type")
    @JsonProperty("instrument_type")
    private String instrumentType;

    @SerializedName("option_type")
    @JsonProperty("option_type")
    private String optionType;

    @SerializedName("tick_size")
    @JsonProperty("tick_size")
    private Double tickSize;

    @SerializedName("last_price")
    @JsonProperty("last_price")
    private Double lastPrice;

    @SerializedName("underlying_key")
    @JsonProperty("underlying_key")
    private String underlyingKey;

    @SerializedName("underlying_symbol")
    @JsonProperty("underlying_symbol")
    private String underlyingSymbol;

    @SerializedName("underlying_type")
    @JsonProperty("underlying_type")
    private String underlyingType;

    @SerializedName("weekly")
    @JsonProperty("weekly")
    private Boolean weekly;

    @SerializedName("security_type")
    @JsonProperty("security_type")
    private String securityType;

    // --- Overlay flags (populated from overlay tables) ---

    @JsonProperty("mtf_enabled")
    private Boolean mtfEnabled;

    @JsonProperty("mis_allowed")
    private Boolean misAllowed;

    @JsonProperty("suspended")
    private Boolean suspended;

    public InstrumentResponse() {
    }

    // --- Getters and Setters ---

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public String getExchangeToken() {
        return exchangeToken;
    }

    public void setExchangeToken(String exchangeToken) {
        this.exchangeToken = exchangeToken;
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

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public Double getStrike() {
        return strike;
    }

    public void setStrike(Double strike) {
        this.strike = strike;
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

    public String getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(String instrumentType) {
        this.instrumentType = instrumentType;
    }

    public String getOptionType() {
        return optionType;
    }

    public void setOptionType(String optionType) {
        this.optionType = optionType;
    }

    public Double getTickSize() {
        return tickSize;
    }

    public void setTickSize(Double tickSize) {
        this.tickSize = tickSize;
    }

    public Double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(Double lastPrice) {
        this.lastPrice = lastPrice;
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

    public Boolean getMtfEnabled() {
        return mtfEnabled;
    }

    public void setMtfEnabled(Boolean mtfEnabled) {
        this.mtfEnabled = mtfEnabled;
    }

    public Boolean getMisAllowed() {
        return misAllowed;
    }

    public void setMisAllowed(Boolean misAllowed) {
        this.misAllowed = misAllowed;
    }

    public Boolean getSuspended() {
        return suspended;
    }

    public void setSuspended(Boolean suspended) {
        this.suspended = suspended;
    }

    // --- Utility Methods ---

    public boolean isOption() {
        return "CE".equalsIgnoreCase(instrumentType) || "PE".equalsIgnoreCase(instrumentType);
    }

    public boolean isCallOption() {
        return "CE".equalsIgnoreCase(instrumentType);
    }

    public boolean isPutOption() {
        return "PE".equalsIgnoreCase(instrumentType);
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

    public boolean isDerivative() {
        return isOption() || isFuture();
    }

    public boolean isTradable() {
        return suspended == null || !suspended;
    }

    public boolean isExpired() {
        // For proper expiry check, parse 'expiry' string and compare with today
        return false;
    }

    @Override
    public String toString() {
        return String.format("Instrument{key='%s', symbol='%s', type='%s', exchange='%s'}",
                instrumentKey, tradingSymbol, instrumentType, exchange);
    }
}
