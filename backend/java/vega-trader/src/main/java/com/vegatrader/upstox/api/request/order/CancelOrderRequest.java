package com.vegatrader.upstox.api.request.order;

/**
 * Request DTO for cancelling an order.
 *
 * @since 2.0.0
 */
public class CancelOrderRequest {

    private String orderId;

    public CancelOrderRequest() {
    }

    public CancelOrderRequest(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void validate() {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID is required");
        }
    }

    public static CancelOrderRequest of(String orderId) {
        return new CancelOrderRequest(orderId);
    }
}
