package com.vegatrader.market.depth.model;

import java.util.List;

public class L30OrderBook {
    private String instrumentKey;
    private List<BookLevel> bids;
    private List<BookLevel> asks;
    private double ltp; // Last Traded Price
    private double cp; // Close Price
    private double atp; // Average Traded Price
    private long oi; // Open Interest
    private long tbq; // Total Bid Qty
    private long tsq; // Total Ask Qty
    private long lastTradeTs; // Last Trade Timestamp
    private long exchangeTs; // Exchange Feed Timestamp
    private Greeks greeks; // Added Greeks

    public L30OrderBook() {
    }

    // Getters
    public String getInstrumentKey() {
        return instrumentKey;
    }

    public List<BookLevel> getBids() {
        return bids;
    }

    public List<BookLevel> getAsks() {
        return asks;
    }

    public double getLtp() {
        return ltp;
    }

    public double getCp() {
        return cp;
    }

    public double getAtp() {
        return atp;
    }

    public long getOi() {
        return oi;
    }

    public long getTbq() {
        return tbq;
    }

    public long getTsq() {
        return tsq;
    }

    public long getLastTradeTs() {
        return lastTradeTs;
    }

    public long getExchangeTs() {
        return exchangeTs;
    }

    public Greeks getGreeks() {
        return greeks;
    }

    // Setters
    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public void setBids(List<BookLevel> bids) {
        this.bids = bids;
    }

    public void setAsks(List<BookLevel> asks) {
        this.asks = asks;
    }

    public void setLtp(double ltp) {
        this.ltp = ltp;
    }

    public void setCp(double cp) {
        this.cp = cp;
    }

    public void setAtp(double atp) {
        this.atp = atp;
    }

    public void setOi(long oi) {
        this.oi = oi;
    }

    public void setTbq(long tbq) {
        this.tbq = tbq;
    }

    public void setTsq(long tsq) {
        this.tsq = tsq;
    }

    public void setLastTradeTs(long lastTradeTs) {
        this.lastTradeTs = lastTradeTs;
    }

    public void setExchangeTs(long exchangeTs) {
        this.exchangeTs = exchangeTs;
    }

    public void setGreeks(Greeks greeks) {
        this.greeks = greeks;
    }

    // For compatibility if needed
    public void setBids(Object bids) {
        if (bids instanceof List) {
            this.bids = (List<BookLevel>) bids;
        }
    }

    public void setAsks(Object asks) {
        if (asks instanceof List) {
            this.asks = (List<BookLevel>) asks;
        }
    }

    // Builder
    public static L30OrderBookBuilder builder() {
        return new L30OrderBookBuilder();
    }

    public static class L30OrderBookBuilder {
        private L30OrderBook book = new L30OrderBook();

        public L30OrderBookBuilder instrumentKey(String k) {
            book.setInstrumentKey(k);
            return this;
        }

        public L30OrderBookBuilder bids(List<BookLevel> b) {
            book.setBids(b);
            return this;
        }

        public L30OrderBookBuilder asks(List<BookLevel> a) {
            book.setAsks(a);
            return this;
        }

        public L30OrderBookBuilder ltp(double l) {
            book.setLtp(l);
            return this;
        }

        public L30OrderBookBuilder cp(double c) {
            book.setCp(c);
            return this;
        }

        public L30OrderBookBuilder atp(double a) {
            book.setAtp(a);
            return this;
        }

        public L30OrderBookBuilder oi(long o) {
            book.setOi(o);
            return this;
        }

        public L30OrderBookBuilder tbq(long t) {
            book.setTbq(t);
            return this;
        }

        public L30OrderBookBuilder tsq(long t) {
            book.setTsq(t);
            return this;
        }

        public L30OrderBookBuilder lastTradeTs(long ts) {
            book.setLastTradeTs(ts);
            return this;
        }

        public L30OrderBookBuilder exchangeTs(long ts) {
            book.setExchangeTs(ts);
            return this;
        }

        public L30OrderBookBuilder greeks(Greeks g) {
            book.setGreeks(g);
            return this;
        }

        public L30OrderBook build() {
            return book;
        }
    }
}
