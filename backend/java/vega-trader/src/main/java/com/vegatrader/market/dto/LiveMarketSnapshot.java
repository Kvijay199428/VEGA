package com.vegatrader.market.dto;

public class LiveMarketSnapshot {
    private String instrumentKey;
    private double ltp;
    private double open;
    private double high;
    private double low;
    private double close;
    private long volume;
    private long oi; // Open Interest
    private long exchangeTimestamp;
    private long receiveTimestamp;

    public LiveMarketSnapshot() {
    }

    // Getters
    public String getInstrumentKey() {
        return instrumentKey;
    }

    public double getLtp() {
        return ltp;
    }

    public double getOpen() {
        return open;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getClose() {
        return close;
    }

    public long getVolume() {
        return volume;
    }

    public long getOi() {
        return oi;
    }

    public long getExchangeTimestamp() {
        return exchangeTimestamp;
    }

    public long getReceiveTimestamp() {
        return receiveTimestamp;
    }

    // Computed Getters
    public double getChange() {
        return ltp - close;
    }

    public double getChangePercent() {
        return (close == 0) ? 0 : ((ltp - close) / close) * 100;
    }

    // Setters
    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public void setLtp(double ltp) {
        this.ltp = ltp;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }

    public void setOi(long oi) {
        this.oi = oi;
    }

    public void setExchangeTimestamp(long exchangeTimestamp) {
        this.exchangeTimestamp = exchangeTimestamp;
    }

    public void setReceiveTimestamp(long receiveTimestamp) {
        this.receiveTimestamp = receiveTimestamp;
    }

    // Builder
    public static LiveMarketSnapshotBuilder builder() {
        return new LiveMarketSnapshotBuilder();
    }

    public static class LiveMarketSnapshotBuilder {
        private LiveMarketSnapshot snapshot = new LiveMarketSnapshot();

        public LiveMarketSnapshotBuilder instrumentKey(String key) {
            snapshot.setInstrumentKey(key);
            return this;
        }

        public LiveMarketSnapshotBuilder ltp(double ltp) {
            snapshot.setLtp(ltp);
            return this;
        }

        public LiveMarketSnapshotBuilder open(double open) {
            snapshot.setOpen(open);
            return this;
        }

        public LiveMarketSnapshotBuilder high(double high) {
            snapshot.setHigh(high);
            return this;
        }

        public LiveMarketSnapshotBuilder low(double low) {
            snapshot.setLow(low);
            return this;
        }

        public LiveMarketSnapshotBuilder close(double close) {
            snapshot.setClose(close);
            return this;
        }

        public LiveMarketSnapshotBuilder volume(long volume) {
            snapshot.setVolume(volume);
            return this;
        }

        public LiveMarketSnapshotBuilder oi(long oi) {
            snapshot.setOi(oi);
            return this;
        }

        public LiveMarketSnapshotBuilder exchangeTimestamp(long ts) {
            snapshot.setExchangeTimestamp(ts);
            return this;
        }

        public LiveMarketSnapshotBuilder receiveTimestamp(long ts) {
            snapshot.setReceiveTimestamp(ts);
            return this;
        }

        public LiveMarketSnapshot build() {
            return snapshot;
        }
    }
}
