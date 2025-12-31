package com.vegatrader.upstox.api.response.websocket;

import com.google.gson.annotations.SerializedName;

/**
 * Individual bid/ask quote for market depth data.
 * 
 * <p>
 * Represents a single level in the order book with bid and ask information.
 * 
 * @since 3.0.0
 */
public class BidAskQuote {

    /**
     * Bid quantity.
     */
    @SerializedName("bq")
    private Long bq;

    /**
     * Bid price.
     */
    @SerializedName("bp")
    private Double bp;

    /**
     * Number of bid orders.
     */
    @SerializedName("bno")
    private Integer bno;

    /**
     * Ask quantity.
     */
    @SerializedName("aq")
    private Long aq;

    /**
     * Ask price.
     */
    @SerializedName("ap")
    private Double ap;

    /**
     * Number of ask orders.
     */
    @SerializedName("ano")
    private Integer ano;

    public BidAskQuote() {
    }

    // Getters and Setters

    public Long getBq() {
        return bq;
    }

    public void setBq(Long bq) {
        this.bq = bq;
    }

    public Double getBp() {
        return bp;
    }

    public void setBp(Double bp) {
        this.bp = bp;
    }

    public Integer getBno() {
        return bno;
    }

    public void setBno(Integer bno) {
        this.bno = bno;
    }

    public Long getAq() {
        return aq;
    }

    public void setAq(Long aq) {
        this.aq = aq;
    }

    public Double getAp() {
        return ap;
    }

    public void setAp(Double ap) {
        this.ap = ap;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    /**
     * Calculates the bid-ask spread.
     * 
     * @return the spread (ap - bp), or null if prices are not available
     */
    public Double getSpread() {
        if (ap != null && bp != null) {
            return ap - bp;
        }
        return null;
    }

    /**
     * Calculates the total bid value (quantity * price).
     * 
     * @return the bid value, or null if not available
     */
    public Double getBidValue() {
        if (bq != null && bp != null) {
            return bq * bp;
        }
        return null;
    }

    /**
     * Calculates the total ask value (quantity * price).
     * 
     * @return the ask value, or null if not available
     */
    public Double getAskValue() {
        if (aq != null && ap != null) {
            return aq * ap;
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("Quote{bid: %d@%.2f(%d), ask: %d@%.2f(%d)}",
                bq, bp, bno, aq, ap, ano);
    }
}
