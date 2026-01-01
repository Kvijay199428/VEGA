package com.vegatrader.market.dto;

import java.util.List;

public class OrderBookSnapshot {
    private String instrumentKey;
    private long timestamp;
    private List<DepthLevel> bids;
    private List<DepthLevel> asks;

    public OrderBookSnapshot() {
    }

    // Getters
    public String getInstrumentKey() {
        return instrumentKey;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<DepthLevel> getBids() {
        return bids;
    }

    public List<DepthLevel> getAsks() {
        return asks;
    }

    // Setters
    public void setInstrumentKey(String key) {
        this.instrumentKey = key;
    }

    public void setTimestamp(long ts) {
        this.timestamp = ts;
    }

    public void setBids(List<DepthLevel> bids) {
        this.bids = bids;
    }

    public void setAsks(List<DepthLevel> asks) {
        this.asks = asks;
    }

    // Builder
    public static OrderBookSnapshotBuilder builder() {
        return new OrderBookSnapshotBuilder();
    }

    public static class OrderBookSnapshotBuilder {
        private OrderBookSnapshot snapshot = new OrderBookSnapshot();

        public OrderBookSnapshotBuilder instrumentKey(String key) {
            snapshot.setInstrumentKey(key);
            return this;
        }

        public OrderBookSnapshotBuilder timestamp(long ts) {
            snapshot.setTimestamp(ts);
            return this;
        }

        public OrderBookSnapshotBuilder bids(List<DepthLevel> bids) {
            snapshot.setBids(bids);
            return this;
        }

        public OrderBookSnapshotBuilder asks(List<DepthLevel> asks) {
            snapshot.setAsks(asks);
            return this;
        }

        public OrderBookSnapshot build() {
            return snapshot;
        }
    }
}
