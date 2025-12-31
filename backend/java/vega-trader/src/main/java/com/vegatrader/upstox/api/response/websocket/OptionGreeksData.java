package com.vegatrader.upstox.api.response.websocket;

import com.google.gson.annotations.SerializedName;

/**
 * Option Greeks data for Market Data Feed V3.
 * 
 * <p>
 * Contains option sensitivity metrics (Greeks) and implied volatility.
 * 
 * @since 3.0.0
 */
public class OptionGreeksData {

    /**
     * Delta - Rate of change of option price with respect to underlying price.
     */
    @SerializedName("delta")
    private Double delta;

    /**
     * Theta - Rate of change of option price with respect to time decay.
     */
    @SerializedName("theta")
    private Double theta;

    /**
     * Gamma - Rate of change of delta with respect to underlying price.
     */
    @SerializedName("gamma")
    private Double gamma;

    /**
     * Vega - Rate of change of option price with respect to volatility.
     */
    @SerializedName("vega")
    private Double vega;

    /**
     * Implied Volatility.
     */
    @SerializedName("iv")
    private Double iv;

    public OptionGreeksData() {
    }

    // Getters and Setters

    public Double getDelta() {
        return delta;
    }

    public void setDelta(Double delta) {
        this.delta = delta;
    }

    public Double getTheta() {
        return theta;
    }

    public void setTheta(Double theta) {
        this.theta = theta;
    }

    public Double getGamma() {
        return gamma;
    }

    public void setGamma(Double gamma) {
        this.gamma = gamma;
    }

    public Double getVega() {
        return vega;
    }

    public void setVega(Double vega) {
        this.vega = vega;
    }

    public Double getIv() {
        return iv;
    }

    public void setIv(Double iv) {
        this.iv = iv;
    }

    /**
     * Checks if this is a call option based on delta.
     * 
     * @return true if delta is positive (call option), false otherwise
     */
    public boolean isCall() {
        return delta != null && delta > 0;
    }

    /**
     * Checks if this is a put option based on delta.
     * 
     * @return true if delta is negative (put option), false otherwise
     */
    public boolean isPut() {
        return delta != null && delta < 0;
    }

    @Override
    public String toString() {
        return String.format("OptionGreeks{delta=%.4f, gamma=%.4f, theta=%.4f, vega=%.4f, iv=%.2f%%}",
                delta, gamma, theta, vega, iv);
    }
}
