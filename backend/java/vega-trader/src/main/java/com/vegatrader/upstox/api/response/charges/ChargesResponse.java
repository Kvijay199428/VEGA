package com.vegatrader.upstox.api.response.charges;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO for order charges breakdown.
 *
 * @since 2.0.0
 */
public class ChargesResponse {

    @SerializedName("brokerage")
    private Double brokerage;

    @SerializedName("stt")
    private Double stt;

    @SerializedName("exchange_charges")
    private Double exchangeCharges;

    @SerializedName("clearing_charges")
    private Double clearingCharges;

    @SerializedName("gst")
    private Double gst;

    @SerializedName("sebi_charges")
    private Double sebiCharges;

    @SerializedName("stamp_duty")
    private Double stampDuty;

    @SerializedName("total_charges")
    private Double totalCharges;

    @SerializedName("net_amount")
    private Double netAmount;

    public ChargesResponse() {
    }

    // Getters/Setters
    public Double getBrokerage() {
        return brokerage;
    }

    public void setBrokerage(Double brokerage) {
        this.brokerage = brokerage;
    }

    public Double getStt() {
        return stt;
    }

    public void setStt(Double stt) {
        this.stt = stt;
    }

    public Double getExchangeCharges() {
        return exchangeCharges;
    }

    public void setExchangeCharges(Double exchangeCharges) {
        this.exchangeCharges = exchangeCharges;
    }

    public Double getClearingCharges() {
        return clearingCharges;
    }

    public void setClearingCharges(Double clearingCharges) {
        this.clearingCharges = clearingCharges;
    }

    public Double getGst() {
        return gst;
    }

    public void setGst(Double gst) {
        this.gst = gst;
    }

    public Double getSebiCharges() {
        return sebiCharges;
    }

    public void setSebiCharges(Double sebiCharges) {
        this.sebiCharges = sebiCharges;
    }

    public Double getStampDuty() {
        return stampDuty;
    }

    public void setStampDuty(Double stampDuty) {
        this.stampDuty = stampDuty;
    }

    public Double getTotalCharges() {
        return totalCharges;
    }

    public void setTotalCharges(Double totalCharges) {
        this.totalCharges = totalCharges;
    }

    public Double getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(Double netAmount) {
        this.netAmount = netAmount;
    }

    public Double getTotalTax() {
        double total = 0.0;
        if (gst != null)
            total += gst;
        if (stt != null)
            total += stt;
        if (stampDuty != null)
            total += stampDuty;
        return total;
    }

    @Override
    public String toString() {
        return String.format("Charges{total=%.2f, tax=%.2f, net=%.2f}",
                totalCharges, getTotalTax(), netAmount);
    }
}
