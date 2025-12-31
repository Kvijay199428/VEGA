package com.vegatrader.upstox.api.response.market;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response DTO for historical candlestick data.
 *
 * @since 2.0.0
 */
public class CandlestickResponse {

    @SerializedName("candles")
    private List<Candle> candles;

    @SerializedName("instrument_key")
    private String instrumentKey;

    @SerializedName("interval")
    private String interval;

    public CandlestickResponse() {
    }

    public List<Candle> getCandles() {
        return candles;
    }

    public void setCandles(List<Candle> candles) {
        this.candles = candles;
    }

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public int getCandleCount() {
        return candles != null ? candles.size() : 0;
    }

    public static class Candle {
        @SerializedName("timestamp")
        private Long timestamp;

        @SerializedName("open")
        private Double open;

        @SerializedName("high")
        private Double high;

        @SerializedName("low")
        private Double low;

        @SerializedName("close")
        private Double close;

        @SerializedName("volume")
        private Long volume;

        @SerializedName("oi")
        private Long openInterest;

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

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

        public Double getRange() {
            if (high != null && low != null) {
                return high - low;
            }
            return null;
        }

        public Double getBodySize() {
            if (open != null && close != null) {
                return Math.abs(close - open);
            }
            return null;
        }

        public boolean isBullish() {
            return close != null && open != null && close > open;
        }

        public boolean isBearish() {
            return close != null && open != null && close < open;
        }

        @Override
        public String toString() {
            return String.format("Candle{O:%.2f, H:%.2f, L:%.2f, C:%.2f, V:%d}",
                    open, high, low, close, volume);
        }
    }
}
