package com.vegatrader.execution;

import com.vegatrader.execution.dto.OrderRequest;
import com.vegatrader.execution.dto.OrderResponse;
import com.vegatrader.execution.dto.OrderStatus;

import java.util.List;

/**
 * Broker-agnostic execution gateway interface.
 * Abstraction layer for order management.
 */
public interface ExecutionGateway {

    /**
     * Place a new order.
     */
    OrderResponse placeOrder(OrderRequest request);

    /**
     * Modify an existing order.
     */
    OrderResponse modifyOrder(String orderId, OrderRequest request);

    /**
     * Cancel an existing order.
     */
    OrderResponse cancelOrder(String orderId);

    /**
     * Get current status of an order.
     */
    OrderStatus getOrderStatus(String orderId);

    /**
     * Get all open orders.
     */
    List<OrderResponse> getOpenOrders();

    /**
     * Get execution provider name.
     */
    String getProviderName();
}
