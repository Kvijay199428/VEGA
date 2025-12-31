package com.vegatrader.upstox.api.order.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Trade record representing an executed portion of an order.
 * Per order-mgmt/main2/b1.md and b2.md.
 * 
 * A single order may consist of multiple trades (partial executions).
 * 
 * @since 4.8.0
 */
public record Trade(
        Long id,
        String tradeId,
        String orderId,
        String exchangeOrderId,
        String exchange,
        String segment, // EQ, FO, COM, CD, MF
        String tradingSymbol,
        String instrumentToken,
        String transactionType, // BUY, SELL
        String product, // D, I, CO, MTF
        String orderType, // MARKET, LIMIT, SL, SL-M
        int quantity,
        BigDecimal price,
        BigDecimal averagePrice,
        Instant exchangeTimestamp,
        Instant orderTimestamp,
        String orderRefId,

        // Charge enrichment (per b2.md)
        BigDecimal brokerage,
        BigDecimal stt,
        BigDecimal exchangeFees,
        BigDecimal gst,
        BigDecimal stampDuty,
        BigDecimal totalCharges,

        // P&L hooks
        BigDecimal grossValue,
        BigDecimal netValue) {

    /**
     * Builder pattern.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String tradeId;
        private String orderId;
        private String exchangeOrderId;
        private String exchange;
        private String segment;
        private String tradingSymbol;
        private String instrumentToken;
        private String transactionType;
        private String product;
        private String orderType;
        private int quantity;
        private BigDecimal price;
        private BigDecimal averagePrice;
        private Instant exchangeTimestamp;
        private Instant orderTimestamp;
        private String orderRefId;
        private BigDecimal brokerage;
        private BigDecimal stt;
        private BigDecimal exchangeFees;
        private BigDecimal gst;
        private BigDecimal stampDuty;
        private BigDecimal totalCharges;
        private BigDecimal grossValue;
        private BigDecimal netValue;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder tradeId(String tradeId) {
            this.tradeId = tradeId;
            return this;
        }

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder exchangeOrderId(String exchangeOrderId) {
            this.exchangeOrderId = exchangeOrderId;
            return this;
        }

        public Builder exchange(String exchange) {
            this.exchange = exchange;
            return this;
        }

        public Builder segment(String segment) {
            this.segment = segment;
            return this;
        }

        public Builder tradingSymbol(String tradingSymbol) {
            this.tradingSymbol = tradingSymbol;
            return this;
        }

        public Builder instrumentToken(String instrumentToken) {
            this.instrumentToken = instrumentToken;
            return this;
        }

        public Builder transactionType(String transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        public Builder product(String product) {
            this.product = product;
            return this;
        }

        public Builder orderType(String orderType) {
            this.orderType = orderType;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Builder averagePrice(BigDecimal averagePrice) {
            this.averagePrice = averagePrice;
            return this;
        }

        public Builder exchangeTimestamp(Instant exchangeTimestamp) {
            this.exchangeTimestamp = exchangeTimestamp;
            return this;
        }

        public Builder orderTimestamp(Instant orderTimestamp) {
            this.orderTimestamp = orderTimestamp;
            return this;
        }

        public Builder orderRefId(String orderRefId) {
            this.orderRefId = orderRefId;
            return this;
        }

        public Builder brokerage(BigDecimal brokerage) {
            this.brokerage = brokerage;
            return this;
        }

        public Builder stt(BigDecimal stt) {
            this.stt = stt;
            return this;
        }

        public Builder exchangeFees(BigDecimal exchangeFees) {
            this.exchangeFees = exchangeFees;
            return this;
        }

        public Builder gst(BigDecimal gst) {
            this.gst = gst;
            return this;
        }

        public Builder stampDuty(BigDecimal stampDuty) {
            this.stampDuty = stampDuty;
            return this;
        }

        public Builder totalCharges(BigDecimal totalCharges) {
            this.totalCharges = totalCharges;
            return this;
        }

        public Builder grossValue(BigDecimal grossValue) {
            this.grossValue = grossValue;
            return this;
        }

        public Builder netValue(BigDecimal netValue) {
            this.netValue = netValue;
            return this;
        }

        public Trade build() {
            return new Trade(id, tradeId, orderId, exchangeOrderId, exchange, segment,
                    tradingSymbol, instrumentToken, transactionType, product, orderType,
                    quantity, price, averagePrice, exchangeTimestamp, orderTimestamp,
                    orderRefId, brokerage, stt, exchangeFees, gst, stampDuty, totalCharges,
                    grossValue, netValue);
        }
    }
}
