package com.vegatrader.upstox.api.response.market;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO for option Greeks.
 *
 * @since 2.0.0
 */
public class GreeksResponse {

    @SerializedName("instrument_key")
    private String instrumentKey;

    @SerializedName("delta")
    private Double delta;

    @SerializedName("gamma")
    private Double gamma;

    @SerializedName("theta")
    private Double theta;

    @SerializedName("vega")
    private Double vega;

    @SerializedName("iv")
    private Double impliedVolatility;

    @SerializedName("underlying_price")
    private Double underlyingPrice;

    @SerializedName("strike_price")
    private Double strikePrice;

    @SerializedName("option_type")
    private String optionType;

    @SerializedName("expiry_date")
    private String expiryDate;

    public GreeksResponse() {
    }

    // Getters/Setters
    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public Double getDelta() {
        return delta;
    }

    public void setDelta(Double delta) {
        this.delta = delta;
    }

    public Double getGamma() {
        return gamma;
    }

    public void setGamma(Double gamma) {
        this.gamma = gamma;
    }

    public Double getTheta() {
        return theta;
    }

    public void setTheta(Double theta) {
        this.theta = theta;
    }

    public Double getVega() {
        return vega;
    }

    public void setVega(Double vega) {
        this.vega = vega;
    }

    public Double getImpliedVolatility() {
        return impliedVolatility;
    }

    public void setImpliedVolatility(Double impliedVolatility) {
        this.impliedVolatility = impliedVolatility;
    }

    public Double getUnderlyingPrice() {
        return underlyingPrice;
    }

    public void setUnderlyingPrice(Double underlyingPrice) {
        this.underlyingPrice = underlyingPrice;
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

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isITM() {
        if (underlyingPrice == null || strikePrice == null)
            return false;
        if ("CE".equals(optionType)) {
            return underlyingPrice > strikePrice;
        } else if ("PE".equals(optionType)) {
            return underlyingPrice < strikePrice;
        }
        return false;
    }

    public boolean isOTM() {
        return !isITM() && !isATM();
    }

    public boolean isATM() {
        if (underlyingPrice == null || strikePrice == null)
            return false;
        return Math.abs(underlyingPrice - strikePrice) < 0.01;
    }

    @Override
    public String toString() {
        return String.format("Greeks{strike=%.2f, delta=%.4f, gamma=%.4f, theta=%.4f, vega=%.4f, iv=%.2f%%}",
                strikePrice, delta, gamma, theta, vega, impliedVolatility);
    }
}
