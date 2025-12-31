package com.vegatrader.upstox.api.response.portfolio;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO for Profit & Loss information.
 *
 * @since 2.0.0
 */
public class PnLResponse {

    @SerializedName("realized_profit")
    private Double realizedProfit;

    @SerializedName("unrealized_profit")
    private Double unrealizedProfit;

    @SerializedName("total_profit")
    private Double totalProfit;

    @SerializedName("day_profit")
    private Double dayProfit;

    @SerializedName("charges")
    private Double charges;

    @SerializedName("net_profit")
    private Double netProfit;

    public PnLResponse() {
    }

    // Getters/Setters
    public Double getRealizedProfit() {
        return realizedProfit;
    }

    public void setRealizedProfit(Double realizedProfit) {
        this.realizedProfit = realizedProfit;
    }

    public Double getUnrealizedProfit() {
        return unrealizedProfit;
    }

    public void setUnrealizedProfit(Double unrealizedProfit) {
        this.unrealizedProfit = unrealizedProfit;
    }

    public Double getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(Double totalProfit) {
        this.totalProfit = totalProfit;
    }

    public Double getDayProfit() {
        return dayProfit;
    }

    public void setDayProfit(Double dayProfit) {
        this.dayProfit = dayProfit;
    }

    public Double getCharges() {
        return charges;
    }

    public void setCharges(Double charges) {
        this.charges = charges;
    }

    public Double getNetProfit() {
        return netProfit;
    }

    public void setNetProfit(Double netProfit) {
        this.netProfit = netProfit;
    }

    public boolean isProfitable() {
        return netProfit != null && netProfit > 0;
    }

    public boolean isLoss() {
        return netProfit != null && netProfit < 0;
    }

    public Double getProfitPercent() {
        if (totalProfit != null && charges != null && charges != 0) {
            return (totalProfit / charges) * 100;
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("PnL{realized=%.2f, unrealized=%.2f, net=%.2f, charges=%.2f}",
                realizedProfit, unrealizedProfit, netProfit, charges);
    }
}
