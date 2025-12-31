package com.vegatrader.upstox.api.response.websocket;

import com.google.gson.annotations.SerializedName;

/**
 * LTPC (Last Traded Price and Close Price) data for Market Data Feed V3.
 * 
 * <p>
 * Contains the basic price information for an instrument in LTPC mode.
 * 
 * @since 3.0.0
 */
public class LTPCData {

    /**
     * Last traded price.
     */
    @SerializedName("ltp")
    private Double ltp;

    /**
     * Last traded time (Unix timestamp in milliseconds).
     */
    @SerializedName("ltt")
    private String ltt;

    /**
     * Last traded quantity.
     */
    @SerializedName("ltq")
    private String ltq;

    /**
     * Close price.
     */
    @SerializedName("cp")
    private Double cp;

    public LTPCData() {
    }

    // Getters and Setters

    public Double getLtp() {
        return ltp;
    }

    public void setLtp(Double ltp) {
        this.ltp = ltp;
    }

    public String getLtt() {
        return ltt;
    }

    public void setLtt(String ltt) {
        this.ltt = ltt;
    }

    /**
     * Gets the last traded time as a long value.
     * 
     * @return the timestamp in milliseconds, or null if not set
     */
    public Long getLttAsLong() {
        return ltt != null ? Long.parseLong(ltt) : null;
    }

    public String getLtq() {
        return ltq;
    }

    public void setLtq(String ltq) {
        this.ltq = ltq;
    }

    /**
     * Gets the last traded quantity as a long value.
     * 
     * @return the quantity, or null if not set
     */
    public Long getLtqAsLong() {
        return ltq != null ? Long.parseLong(ltq) : null;
    }

    public Double getCp() {
        return cp;
    }

    public void setCp(Double cp) {
        this.cp = cp;
    }

    /**
     * Calculates the change from close price.
     * 
     * @return the change value, or null if ltp or cp is not available
     */
    public Double getChange() {
        if (ltp != null && cp != null) {
            return ltp - cp;
        }
        return null;
    }

    /**
     * Calculates the percentage change from close price.
     * 
     * @return the percentage change, or null if ltp or cp is not available
     */
    public Double getChangePercent() {
        if (ltp != null && cp != null && cp != 0) {
            return ((ltp - cp) / cp) * 100;
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("LTPC{ltp=%.2f, cp=%.2f, ltq=%s, change=%.2f%%}",
                ltp, cp, ltq, getChangePercent());
    }
}
