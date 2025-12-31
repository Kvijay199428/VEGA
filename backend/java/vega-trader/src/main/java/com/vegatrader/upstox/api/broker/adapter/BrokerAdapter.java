package com.vegatrader.upstox.api.broker.adapter;

import com.vegatrader.upstox.api.broker.model.*;
import java.util.List;

/**
 * Broker adapter interface.
 * Implement for each broker (Upstox, Fyers, Zerodha).
 * 
 * @since 4.2.0
 */
public interface BrokerAdapter {

    /**
     * Gets broker ID.
     */
    String getBrokerId();

    /**
     * Places an order.
     */
    BrokerOrderResponse placeOrder(OrderRequest request);

    /**
     * Modifies an existing order.
     */
    BrokerOrderResponse modifyOrder(String orderId, OrderRequest request);

    /**
     * Cancels an order.
     */
    void cancelOrder(String orderId);

    /**
     * Gets order status.
     */
    BrokerOrderStatus getOrderStatus(String orderId);

    /**
     * Gets all orders for the day.
     */
    List<BrokerOrderStatus> getOrders();

    /**
     * Gets current positions.
     */
    List<Position> getPositions();

    /**
     * Gets holdings.
     */
    List<Holding> getHoldings();

    /**
     * Subscribes to market data.
     */
    void subscribeMarketData(List<String> instrumentKeys);

    /**
     * Unsubscribes from market data.
     */
    void unsubscribeMarketData(List<String> instrumentKeys);

    /**
     * Checks connection health.
     */
    boolean isConnected();

    /**
     * Reconnects if disconnected.
     */
    void reconnect();
}
