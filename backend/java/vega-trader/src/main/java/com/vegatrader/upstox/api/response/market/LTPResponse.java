package com.vegatrader.upstox.api.response.market;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO for Last Traded Price (LTP).
 *
 * @since 2.0.0
 */
public class LTPResponse {

    @SerializedName("instrument_key")
    private String instrumentKey;

    @SerializedName("last_price")
    private Double lastPrice;

    @SerializedName("last_traded_time")
    private String lastTradedTime;

    @SerializedName("volume")
    private Long volume;

    public LTPResponse() {
    }

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public Double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(Double lastPrice) {
        this.lastPrice = lastPrice;
    }

    public String getLastTradedTime() {
        return lastTradedTime;
    }

    public void setLastTradedTime(String lastTradedTime) {
        this.lastTradedTime = lastTradedTime;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    @Override
    public String toString() {
        return String.format("LTP{%.2f @ %s}", lastPrice, lastTradedTime);
    }
}
