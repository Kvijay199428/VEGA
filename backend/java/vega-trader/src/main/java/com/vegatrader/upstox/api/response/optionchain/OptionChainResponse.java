package com.vegatrader.upstox.api.response.optionchain;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response DTO for option chain data.
 *
 * @since 2.0.0
 */
public class OptionChainResponse {

    @SerializedName("expiry_date")
    private String expiryDate;

    @SerializedName("underlying_key")
    private String underlyingKey;

    @SerializedName("underlying_spot_price")
    private Double underlyingSpotPrice;

    @SerializedName("data")
    private List<OptionStrikeData> data;

    public OptionChainResponse() {
    }

    // Getters/Setters
    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getUnderlyingKey() {
        return underlyingKey;
    }

    public void setUnderlyingKey(String underlyingKey) {
        this.underlyingKey = underlyingKey;
    }

    public Double getUnderlyingSpotPrice() {
        return underlyingSpotPrice;
    }

    public void setUnderlyingSpotPrice(Double underlyingSpotPrice) {
        this.underlyingSpotPrice = underlyingSpotPrice;
    }

    public List<OptionStrikeData> getData() {
        return data;
    }

    public void setData(List<OptionStrikeData> data) {
        this.data = data;
    }

    public int getStrikeCount() {
        return data != null ? data.size() : 0;
    }

    public static class OptionStrikeData {
        @SerializedName("strike_price")
        private Double strikePrice;

        @SerializedName("call_options")
        private OptionData callOptions;

        @SerializedName("put_options")
        private OptionData putOptions;

        public Double getStrikePrice() {
            return strikePrice;
        }

        public void setStrikePrice(Double strikePrice) {
            this.strikePrice = strikePrice;
        }

        public OptionData getCallOptions() {
            return callOptions;
        }

        public void setCallOptions(OptionData callOptions) {
            this.callOptions = callOptions;
        }

        public OptionData getPutOptions() {
            return putOptions;
        }

        public void setPutOptions(OptionData putOptions) {
            this.putOptions = putOptions;
        }
    }

    public static class OptionData {
        @SerializedName("instrument_key")
        private String instrumentKey;

        @SerializedName("last_price")
        private Double lastPrice;

        @SerializedName("volume")
        private Long volume;

        @SerializedName("oi")
        private Long openInterest;

        @SerializedName("bid_price")
        private Double bidPrice;

        @SerializedName("ask_price")
        private Double askPrice;

        @SerializedName("iv")
        private Double impliedVolatility;

        @SerializedName("delta")
        private Double delta;

        @SerializedName("gamma")
        private Double gamma;

        @SerializedName("theta")
        private Double theta;

        @SerializedName("vega")
        private Double vega;

        // Getters/Setters
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

        public Long getVolume() {
            return volume;
        }

        public void setVolume(Long volume) {
            this.volume = volume;
        }

        public Long getOpenInterest() {
            return openInterest;
        }

        public void setOpenInterest(Long openInterest) {
            this.openInterest = openInterest;
        }

        public Double getBidPrice() {
            return bidPrice;
        }

        public void setBidPrice(Double bidPrice) {
            this.bidPrice = bidPrice;
        }

        public Double getAskPrice() {
            return askPrice;
        }

        public void setAskPrice(Double askPrice) {
            this.askPrice = askPrice;
        }

        public Double getImpliedVolatility() {
            return impliedVolatility;
        }

        public void setImpliedVolatility(Double impliedVolatility) {
            this.impliedVolatility = impliedVolatility;
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

        public boolean hasLiquidity() {
            return volume != null && volume > 0;
        }
    }
}
