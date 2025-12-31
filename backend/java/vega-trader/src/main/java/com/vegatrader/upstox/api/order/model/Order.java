package com.vegatrader.upstox.api.order.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Order entity for persistence.
 * Per order-mgmt/a1.md section 3.1.
 * 
 * @since 4.8.0
 */
public record Order(
        Long id,
        String orderId,
        String brokerOrderId,
        String userId,
        String broker,

        // Instrument
        String exchange,
        String symbol,
        String instrumentKey,

        // Order details
        OrderSide side,
        OrderType orderType,
        ProductType product,
        int quantity,
        BigDecimal price,
        BigDecimal triggerPrice,

        // Status
        OrderStatus status,
        int filledQuantity,
        BigDecimal averagePrice,

        // Timestamps
        Instant placedAt,
        Instant acknowledgedAt,
        Instant finalStatusAt,

        // RMS
        Long rmsSnapshotId) {

    /**
     * Check if order is complete (filled or cancelled).
     */
    public boolean isComplete() {
        return status == OrderStatus.FILLED ||
                status == OrderStatus.CANCELLED ||
                status == OrderStatus.REJECTED;
    }

    /**
     * Check if order is partially filled.
     */
    public boolean isPartiallyFilled() {
        return filledQuantity > 0 && filledQuantity < quantity;
    }

    /**
     * Get pending quantity.
     */
    public int getPendingQuantity() {
        return quantity - filledQuantity;
    }

    /**
     * Order side.
     */
    public enum OrderSide {
        BUY, SELL
    }

    /**
     * Order type.
     */
    public enum OrderType {
        MARKET, LIMIT, SL, SL_M
    }

    /**
     * Product type.
     */
    public enum ProductType {
        I, // Intraday (MIS)
        D, // Delivery (CNC)
        CO, // Cover Order
        MTF // Margin Trading Facility
    }

    /**
     * Order status.
     */
    public enum OrderStatus {
        PENDING,
        ACKNOWLEDGED,
        OPEN,
        FILLED,
        PARTIALLY_FILLED,
        CANCELLED,
        REJECTED,
        EXPIRED
    }

    /**
     * Builder for order.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String orderId;
        private String brokerOrderId;
        private String userId;
        private String broker = "UPSTOX";
        private String exchange;
        private String symbol;
        private String instrumentKey;
        private OrderSide side;
        private OrderType orderType;
        private ProductType product;
        private int quantity;
        private BigDecimal price;
        private BigDecimal triggerPrice;
        private OrderStatus status = OrderStatus.PENDING;
        private int filledQuantity = 0;
        private BigDecimal averagePrice;
        private Instant placedAt;
        private Long rmsSnapshotId;

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder brokerOrderId(String brokerOrderId) {
            this.brokerOrderId = brokerOrderId;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder broker(String broker) {
            this.broker = broker;
            return this;
        }

        public Builder exchange(String exchange) {
            this.exchange = exchange;
            return this;
        }

        public Builder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Builder instrumentKey(String instrumentKey) {
            this.instrumentKey = instrumentKey;
            return this;
        }

        public Builder side(OrderSide side) {
            this.side = side;
            return this;
        }

        public Builder orderType(OrderType orderType) {
            this.orderType = orderType;
            return this;
        }

        public Builder product(ProductType product) {
            this.product = product;
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

        public Builder triggerPrice(BigDecimal triggerPrice) {
            this.triggerPrice = triggerPrice;
            return this;
        }

        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder placedAt(Instant placedAt) {
            this.placedAt = placedAt;
            return this;
        }

        public Builder rmsSnapshotId(Long rmsSnapshotId) {
            this.rmsSnapshotId = rmsSnapshotId;
            return this;
        }

        public Order build() {
            return new Order(
                    null, orderId, brokerOrderId, userId, broker,
                    exchange, symbol, instrumentKey,
                    side, orderType, product, quantity, price, triggerPrice,
                    status, filledQuantity, averagePrice,
                    placedAt != null ? placedAt : Instant.now(),
                    null, null, rmsSnapshotId);
        }
    }
}
