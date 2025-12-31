package com.vegatrader.upstox.api.broker.model;

/**
 * Broker order response.
 * 
 * @since 4.2.0
 */
public record BrokerOrderResponse(
        boolean success,
        String orderId,
        String brokerOrderId,
        String message) {

    public static BrokerOrderResponse success(String orderId, String brokerOrderId) {
        return new BrokerOrderResponse(true, orderId, brokerOrderId, "Order placed successfully");
    }

    public static BrokerOrderResponse failure(String message) {
        return new BrokerOrderResponse(false, null, null, message);
    }
}
