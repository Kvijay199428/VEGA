package com.vegatrader.upstox.api.response.market;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO for market quote data.
 *
 * @since 2.0.0
 */
public class QuoteResponse {

    @SerializedName("instrument_key")
    private String instrumentKey;

    @SerializedName("last_price")
    private Double lastPrice;

    @SerializedName("ohlc")
    private OHLC ohlc;

    @SerializedName("volume")
    private Long volume;

    @SerializedName("oi")
    private Long openInterest;

    @SerializedName("bid_price")
    private Double bidPrice;

    @SerializedName("bid_qty")
    private Integer bidQty;

    @SerializedName("ask_price")
    private Double askPrice;

    @SerializedName("ask_qty")
    private Integer askQty;

    @SerializedName("last_traded_time")
    private String lastTradedTime;

    public QuoteResponse() {
    }

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

    public OHLC getOhlc() {
        return ohlc;
    }

    public void setOhlc(OHLC ohlc) {
        this.ohlc = ohlc;
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

    public Integer getBidQty() {
        return bidQty;
    }

    public void setBidQty(Integer bidQty) {
        this.bidQty = bidQty;
    }

    public Double getAskPrice() {
        return askPrice;
    }

    public void setAskPrice(Double askPrice) {
        this.askPrice = askPrice;
    }

    public Integer getAskQty() {
        return askQty;
    }

    public void setAskQty(Integer askQty) {
        this.askQty = askQty;
    }

    public String getLastTradedTime() {
        return lastTradedTime;
    }

    public void setLastTradedTime(String lastTradedTime) {
        this.lastTradedTime = lastTradedTime;
    }

    public Double getSpread() {
        if (askPrice != null && bidPrice != null) {
            return askPrice - bidPrice;
        }
        return null;
    }

    public static class OHLC {
        @SerializedName("open")
        private Double open;

        @SerializedName("high")
        private Double high;

        @SerializedName("low")
        private Double low;

        @SerializedName("close")
        private Double close;

        public Double getOpen() {
            return open;
        }

        public void setOpen(Double open) {
            this.open = open;
        }

        public Double getHigh() {
            return high;
        }

        public void setHigh(Double high) {
            this.high = high;
        }

        public Double getLow() {
            return low;
        }

        public void setLow(Double low) {
            this.low = low;
        }

        public Double getClose() {
            return close;
        }

        public void setClose(Double close) {
            this.close = close;
        }

        public Double getRange() {
            if (high != null && low != null) {
                return high - low;
            }
            return null;
        }
    }
}
