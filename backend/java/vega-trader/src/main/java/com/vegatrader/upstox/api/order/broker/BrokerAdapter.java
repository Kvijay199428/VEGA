package com.vegatrader.upstox.api.order.broker;

import java.math.BigDecimal;
import java.util.List;

/**
 * Broker Adapter interface for multi-broker support.
 * Per order-mgmt/b4.md section 5.
 * 
 * Implementations: UpstoxBrokerAdapter, ZerodhaBrokerAdapter,
 * FyersBrokerAdapter, etc.
 * 
 * @since 4.9.0
 */
public interface BrokerAdapter {

        /**
         * Get broker name.
         */
        String getBrokerName();

        /**
         * Get broker capabilities.
         */
        BrokerCapability getCapabilities();

        /**
         * Place a single order.
         */
        OrderResult placeOrder(OrderRequest request);

        /**
         * Place multiple orders (if supported).
         */
        MultiOrderResult placeMultiOrder(List<OrderRequest> orders);

        /**
         * Modify an existing order.
         */
        OrderResult modifyOrder(ModifyRequest request);

        /**
         * Cancel an order.
         */
        OrderResult cancelOrder(String orderId);

        /**
         * Cancel multiple orders.
         */
        MultiOrderResult cancelMultiOrder(List<String> orderIds);

        /**
         * Get order status from broker.
         */
        OrderStatus getOrderStatus(String orderId);

        /**
         * Get order book from broker.
         */
        List<BrokerOrder> getOrderBook();

        /**
         * Get trades for day.
         */
        List<BrokerTrade> getTradesForDay();

        /**
         * Get trades for order.
         */
        List<BrokerTrade> getOrderTrades(String orderId);

        /**
         * Exit all positions.
         */
        MultiOrderResult exitAllPositions(String segment, String tag);

        /**
         * Check if broker is available.
         */
        boolean isAvailable();

        /**
         * Get current rate limit status.
         */
        RateLimitStatus getRateLimitStatus();

        // ==================== Request/Response Records ====================

        record OrderRequest(
                        String correlationId,
                        String instrumentToken,
                        String side, // BUY, SELL
                        String orderType, // MARKET, LIMIT, SL, SL-M
                        String product, // I, D, CO, MTF
                        int quantity,
                        BigDecimal price,
                        BigDecimal triggerPrice,
                        String validity, // DAY, IOC
                        int disclosedQuantity,
                        String tag,
                        boolean isAmo,
                        boolean slice) {
        }

        record ModifyRequest(
                        String orderId,
                        String correlationId,
                        Integer quantity,
                        BigDecimal price,
                        String orderType,
                        String validity,
                        Integer disclosedQuantity,
                        BigDecimal triggerPrice) {
        }

        record OrderResult(
                        boolean success,
                        String orderId,
                        String brokerOrderId,
                        String correlationId,
                        String errorCode,
                        String errorMessage,
                        long latencyMs) {
                public static OrderResult success(String orderId, String brokerOrderId, String correlationId,
                                long latencyMs) {
                        return new OrderResult(true, orderId, brokerOrderId, correlationId, null, null, latencyMs);
                }

                public static OrderResult error(String correlationId, String errorCode, String errorMessage) {
                        return new OrderResult(false, null, null, correlationId, errorCode, errorMessage, 0);
                }
        }

        record MultiOrderResult(
                        String status, // success, partial_success, error
                        List<OrderResult> results,
                        int totalOrders,
                        int successCount,
                        int errorCount,
                        long latencyMs) {
        }

        record OrderStatus(
                        String orderId,
                        String status,
                        int filledQuantity,
                        int pendingQuantity,
                        BigDecimal averagePrice,
                        String statusMessage) {
        }

        record BrokerOrder(
                        String orderId,
                        String exchangeOrderId,
                        String instrument,
                        String side,
                        String orderType,
                        int quantity,
                        int filledQuantity,
                        BigDecimal price,
                        BigDecimal averagePrice,
                        String status,
                        String statusMessage) {
        }

        record BrokerTrade(
                        String tradeId,
                        String orderId,
                        String exchangeOrderId,
                        String instrument,
                        String side,
                        int quantity,
                        BigDecimal price,
                        String timestamp) {
        }

        record RateLimitStatus(
                        int remainingRequests,
                        int maxRequests,
                        long resetTimeMs) {
        }
}
