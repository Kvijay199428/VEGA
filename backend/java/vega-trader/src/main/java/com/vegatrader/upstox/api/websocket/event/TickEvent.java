package com.vegatrader.upstox.api.websocket.event;

import java.time.Instant;

/**
 * Tick event representing a single price update.
 * 
 * @since 2.0.0
 */
public class TickEvent extends MarketDataEvent {

    private final double ltp; // Last Traded Price
    private final double open;
    private final double high;
    private final double low;
    private final double close;
    private final long volume;
    private final double change;
    private final double changePercent;

    public TickEvent(String instrumentKey, double ltp) {
        super(instrumentKey);
        this.ltp = ltp;
        this.open = 0;
        this.high = 0;
        this.low = 0;
        this.close = 0;
        this.volume = 0;
        this.change = 0;
        this.changePercent = 0;
    }

    public TickEvent(String instrumentKey, double ltp, double open, double high,
            double low, double close, long volume) {
        super(instrumentKey);
        this.ltp = ltp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.change = ltp - close;
        this.changePercent = close > 0 ? ((ltp - close) / close) * 100 : 0;
    }

    public TickEvent(String instrumentKey, Instant timestamp, double ltp,
            double open, double high, double low, double close, long volume) {
        super(instrumentKey, timestamp);
        this.ltp = ltp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.change = ltp - close;
        this.changePercent = close > 0 ? ((ltp - close) / close) * 100 : 0;
    }

    @Override
    public MarketDataEventType getEventType() {
        return MarketDataEventType.TICK;
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

    public double getChange() {
        return change;
    }

    public double getChangePercent() {
        return changePercent;
    }

    @Override
    public String toString() {
        return String.format("TickEvent{key=%s, ltp=%.2f, change=%.2f%%}",
                getInstrumentKey(), ltp, changePercent);
    }
}
