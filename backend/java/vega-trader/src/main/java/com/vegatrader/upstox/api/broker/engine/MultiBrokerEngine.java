package com.vegatrader.upstox.api.broker.engine;

import com.vegatrader.upstox.api.broker.adapter.BrokerAdapter;
import com.vegatrader.upstox.api.broker.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Multi-Broker Engine.
 * 
 * <p>
 * Provides unified API for order routing and portfolio aggregation across
 * brokers.
 * 
 * @since 4.2.0
 */
@Service
public class MultiBrokerEngine {

    private static final Logger logger = LoggerFactory.getLogger(MultiBrokerEngine.class);

    private final Map<String, BrokerAdapter> adapters = new ConcurrentHashMap<>();

    /**
     * Registers a broker adapter.
     */
    public void registerAdapter(String brokerId, BrokerAdapter adapter) {
        adapters.put(brokerId, adapter);
        logger.info("Registered broker adapter: {}", brokerId);
    }

    /**
     * Unregisters a broker adapter.
     */
    public void unregisterAdapter(String brokerId) {
        adapters.remove(brokerId);
        logger.info("Unregistered broker adapter: {}", brokerId);
    }

    /**
     * Gets available broker IDs.
     */
    public Set<String> getAvailableBrokers() {
        return Collections.unmodifiableSet(adapters.keySet());
    }

    /**
     * Routes an order to specific broker.
     */
    public BrokerOrderResponse routeOrder(String brokerId, OrderRequest request) {
        BrokerAdapter adapter = getAdapter(brokerId);
        logger.info("Routing order to {}: {} {} qty={}", brokerId, request.transactionType(),
                request.instrumentKey(), request.qty());
        return adapter.placeOrder(request);
    }

    /**
     * Cancels an order.
     */
    public void cancelOrder(String brokerId, String orderId) {
        BrokerAdapter adapter = getAdapter(brokerId);
        adapter.cancelOrder(orderId);
        logger.info("Cancelled order {} on {}", orderId, brokerId);
    }

    /**
     * Gets order status.
     */
    public BrokerOrderStatus getOrderStatus(String brokerId, String orderId) {
        BrokerAdapter adapter = getAdapter(brokerId);
        return adapter.getOrderStatus(orderId);
    }

    /**
     * Gets all orders from a broker.
     */
    public List<BrokerOrderStatus> getOrders(String brokerId) {
        BrokerAdapter adapter = getAdapter(brokerId);
        return adapter.getOrders();
    }

    /**
     * Gets aggregated positions across all brokers.
     */
    public List<Position> getAggregatedPositions() {
        return adapters.values().stream()
                .flatMap(adapter -> {
                    try {
                        return adapter.getPositions().stream();
                    } catch (Exception e) {
                        logger.warn("Failed to get positions from {}", adapter.getBrokerId());
                        return java.util.stream.Stream.empty();
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Gets positions from specific broker.
     */
    public List<Position> getPositions(String brokerId) {
        BrokerAdapter adapter = getAdapter(brokerId);
        return adapter.getPositions();
    }

    /**
     * Gets aggregated holdings across all brokers.
     */
    public List<Holding> getAggregatedHoldings() {
        return adapters.values().stream()
                .flatMap(adapter -> {
                    try {
                        return adapter.getHoldings().stream();
                    } catch (Exception e) {
                        logger.warn("Failed to get holdings from {}", adapter.getBrokerId());
                        return java.util.stream.Stream.empty();
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Gets holdings from specific broker.
     */
    public List<Holding> getHoldings(String brokerId) {
        BrokerAdapter adapter = getAdapter(brokerId);
        return adapter.getHoldings();
    }

    /**
     * Calculates aggregated P&L.
     */
    public double getAggregatedPnl() {
        return getAggregatedPositions().stream()
                .mapToDouble(Position::pnl)
                .sum();
    }

    /**
     * Subscribes to market data on broker.
     */
    public void subscribeMarketData(String brokerId, List<String> instrumentKeys) {
        BrokerAdapter adapter = getAdapter(brokerId);
        adapter.subscribeMarketData(instrumentKeys);
    }

    /**
     * Checks if broker is connected.
     */
    public boolean isBrokerConnected(String brokerId) {
        BrokerAdapter adapter = adapters.get(brokerId);
        return adapter != null && adapter.isConnected();
    }

    /**
     * Reconnects a broker.
     */
    public void reconnectBroker(String brokerId) {
        BrokerAdapter adapter = getAdapter(brokerId);
        adapter.reconnect();
        logger.info("Reconnected broker: {}", brokerId);
    }

    private BrokerAdapter getAdapter(String brokerId) {
        BrokerAdapter adapter = adapters.get(brokerId);
        if (adapter == null) {
            throw new IllegalArgumentException("Broker not found: " + brokerId);
        }
        return adapter;
    }
}
