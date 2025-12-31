package com.vegatrader.upstox.api.response.market;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO for OHLC (Open, High, Low, Close) data.
 *
 * @since 2.0.0
 */
public class OHLCResponse {

    @SerializedName("instrument_key")
    private String instrumentKey;

    @SerializedName("ohlc")
    private OHLC ohlc;

    public OHLCResponse() {
    }

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public OHLC getOhlc() {
        return ohlc;
    }

    public void setOhlc(OHLC ohlc) {
        this.ohlc = ohlc;
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

        @SerializedName("prev_close")
        private Double previousClose;

        @SerializedName("volume")
        private Long volume;

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

        public Double getPreviousClose() {
            return previousClose;
        }

        public void setPreviousClose(Double previousClose) {
            this.previousClose = previousClose;
        }

        public Long getVolume() {
            return volume;
        }

        public void setVolume(Long volume) {
            this.volume = volume;
        }

        public Double getChange() {
            if (close != null && previousClose != null) {
                return close - previousClose;
            }
            return null;
        }

        public Double getChangePercent() {
            if (close != null && previousClose != null && previousClose != 0) {
                return ((close - previousClose) / previousClose) * 100;
            }
            return null;
        }

        public Double getRange() {
            if (high != null && low != null) {
                return high - low;
            }
            return null;
        }

        public boolean isPositiveDay() {
            return close != null && previousClose != null && close > previousClose;
        }

        @Override
        public String toString() {
            return String.format("OHLC{O:%.2f, H:%.2f, L:%.2f, C:%.2f, Chg:%.2f%%}",
                    open, high, low, close, getChangePercent());
        }
    }
}
