package com.vegatrader.upstox.api.broker.model;

import java.time.LocalDateTime;

/**
 * Broker order status model.
 * 
 * @since 4.2.0
 */
public record BrokerOrderStatus(
        String orderId,
        String brokerOrderId,
        String instrumentKey,
        String status, // PENDING, OPEN, COMPLETE, CANCELLED, REJECTED
        String product,
        String orderType,
        String transactionType,
        int qty,
        int filledQty,
        int pendingQty,
        double price,
        double averagePrice,
        String rejectionReason,
        LocalDateTime timestamp) {

    public boolean isComplete() {
        return "COMPLETE".equalsIgnoreCase(status);
    }

    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status) || "OPEN".equalsIgnoreCase(status);
    }

    public boolean isRejected() {
        return "REJECTED".equalsIgnoreCase(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equalsIgnoreCase(status);
    }

    public double filledValue() {
        return filledQty * averagePrice;
    }
}
