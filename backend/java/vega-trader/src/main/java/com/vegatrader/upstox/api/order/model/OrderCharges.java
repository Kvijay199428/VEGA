package com.vegatrader.upstox.api.order.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Order charges model.
 * Per order-mgmt/a1.md section 3.2.
 * 
 * Charges are append-only (never updated post-finalization).
 * 
 * @since 4.8.0
 */
public record OrderCharges(
        Long id,
        String orderId,

        // Brokerage
        BigDecimal brokerage,

        // Exchange charges
        BigDecimal exchangeTxnCharge,

        // Statutory charges
        BigDecimal sebiCharge,
        BigDecimal stt,
        BigDecimal stampDuty,
        BigDecimal gst,
        BigDecimal ipf,

        // Total
        BigDecimal totalCharges,
        String currency,

        // Metadata
        Instant computedAt) {

    /**
     * Create charges from brokerage estimate.
     */
    public static OrderCharges from(String orderId, BrokerageEstimate estimate) {
        return new OrderCharges(
                null,
                orderId,
                estimate.brokerage(),
                estimate.exchangeCharge(),
                estimate.sebiCharge(),
                estimate.stt(),
                estimate.stampDuty(),
                estimate.gst(),
                BigDecimal.ZERO,
                estimate.total(),
                "INR",
                Instant.now());
    }

    /**
     * Builder for charges.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String orderId;
        private BigDecimal brokerage = BigDecimal.ZERO;
        private BigDecimal exchangeTxnCharge = BigDecimal.ZERO;
        private BigDecimal sebiCharge = BigDecimal.ZERO;
        private BigDecimal stt = BigDecimal.ZERO;
        private BigDecimal stampDuty = BigDecimal.ZERO;
        private BigDecimal gst = BigDecimal.ZERO;
        private BigDecimal ipf = BigDecimal.ZERO;

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder brokerage(BigDecimal brokerage) {
            this.brokerage = brokerage;
            return this;
        }

        public Builder exchangeTxnCharge(BigDecimal exchangeTxnCharge) {
            this.exchangeTxnCharge = exchangeTxnCharge;
            return this;
        }

        public Builder sebiCharge(BigDecimal sebiCharge) {
            this.sebiCharge = sebiCharge;
            return this;
        }

        public Builder stt(BigDecimal stt) {
            this.stt = stt;
            return this;
        }

        public Builder stampDuty(BigDecimal stampDuty) {
            this.stampDuty = stampDuty;
            return this;
        }

        public Builder gst(BigDecimal gst) {
            this.gst = gst;
            return this;
        }

        public Builder ipf(BigDecimal ipf) {
            this.ipf = ipf;
            return this;
        }

        public OrderCharges build() {
            BigDecimal total = brokerage
                    .add(exchangeTxnCharge)
                    .add(sebiCharge)
                    .add(stt)
                    .add(stampDuty)
                    .add(gst)
                    .add(ipf);

            return new OrderCharges(
                    null, orderId,
                    brokerage, exchangeTxnCharge, sebiCharge, stt, stampDuty, gst, ipf,
                    total, "INR", Instant.now());
        }
    }

    /**
     * Brokerage estimate from broker API.
     */
    public record BrokerageEstimate(
            BigDecimal brokerage,
            BigDecimal exchangeCharge,
            BigDecimal sebiCharge,
            BigDecimal stt,
            BigDecimal stampDuty,
            BigDecimal gst,
            BigDecimal total) {
    }
}
