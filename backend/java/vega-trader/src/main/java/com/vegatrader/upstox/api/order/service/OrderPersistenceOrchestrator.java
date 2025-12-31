package com.vegatrader.upstox.api.order.service;

import com.vegatrader.upstox.api.order.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Order persistence orchestrator.
 * Per order-mgmt/a1.md section 5.1.
 * 
 * Persistence happens ONLY after broker ACK, not before.
 * 
 * @since 4.8.0
 */
@Service
public class OrderPersistenceOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(OrderPersistenceOrchestrator.class);

    // In-memory stores (production would use JPA repositories)
    private final Map<String, Order> orderStore = new ConcurrentHashMap<>();
    private final Map<String, OrderCharges> chargesStore = new ConcurrentHashMap<>();
    private final Map<String, LatencyMetrics> latencyStore = new ConcurrentHashMap<>();
    private final List<AuditEvent> auditLog = Collections.synchronizedList(new ArrayList<>());

    /**
     * Persist order after broker ACK.
     */
    public void persist(
            Order order,
            OrderCharges.BrokerageEstimate charges,
            LatencyMetrics latency) {

        logger.info("Persisting order: {}", order.orderId());

        // Save order
        orderStore.put(order.orderId(), order);

        // Save charges
        if (charges != null) {
            OrderCharges orderCharges = OrderCharges.from(order.orderId(), charges);
            chargesStore.put(order.orderId(), orderCharges);
        }

        // Save latency
        if (latency != null) {
            latencyStore.put(order.orderId(), latency);
        }

        // Audit
        auditLog.add(new AuditEvent(
                order.orderId(),
                "ORDER_PERSISTED",
                Map.of("status", order.status().name()),
                Instant.now()));

        logger.info("Order persisted: {} (status={})", order.orderId(), order.status());
    }

    /**
     * Get order by ID.
     */
    public Optional<Order> getOrder(String orderId) {
        return Optional.ofNullable(orderStore.get(orderId));
    }

    /**
     * Get orders by user.
     */
    public List<Order> getOrdersByUser(String userId) {
        return orderStore.values().stream()
                .filter(o -> userId.equals(o.userId()))
                .sorted((a, b) -> b.placedAt().compareTo(a.placedAt()))
                .toList();
    }

    /**
     * Get charges for order.
     */
    public Optional<OrderCharges> getCharges(String orderId) {
        return Optional.ofNullable(chargesStore.get(orderId));
    }

    /**
     * Get latency for order.
     */
    public Optional<LatencyMetrics> getLatency(String orderId) {
        return Optional.ofNullable(latencyStore.get(orderId));
    }

    /**
     * Update order status.
     */
    public void updateStatus(String orderId, Order.OrderStatus newStatus) {
        Order existing = orderStore.get(orderId);
        if (existing == null) {
            logger.warn("Order not found for status update: {}", orderId);
            return;
        }

        Order updated = new Order(
                existing.id(), existing.orderId(), existing.brokerOrderId(),
                existing.userId(), existing.broker(),
                existing.exchange(), existing.symbol(), existing.instrumentKey(),
                existing.side(), existing.orderType(), existing.product(),
                existing.quantity(), existing.price(), existing.triggerPrice(),
                newStatus, existing.filledQuantity(), existing.averagePrice(),
                existing.placedAt(), existing.acknowledgedAt(),
                Instant.now(),
                existing.rmsSnapshotId());

        orderStore.put(orderId, updated);

        auditLog.add(new AuditEvent(
                orderId,
                "STATUS_CHANGED",
                Map.of("oldStatus", existing.status().name(), "newStatus", newStatus.name()),
                Instant.now()));

        logger.info("Order status updated: {} -> {}", orderId, newStatus);
    }

    /**
     * Get recent orders (for cache).
     */
    public List<Order> getRecentOrders(String userId, int limit) {
        return orderStore.values().stream()
                .filter(o -> userId.equals(o.userId()))
                .sorted((a, b) -> b.placedAt().compareTo(a.placedAt()))
                .limit(limit)
                .toList();
    }

    /**
     * Get audit log for order.
     */
    public List<AuditEvent> getAuditLog(String orderId) {
        return auditLog.stream()
                .filter(e -> orderId.equals(e.orderId()))
                .toList();
    }

    /**
     * Get full order history (for audit export).
     */
    public List<Order> getAllOrders() {
        return new ArrayList<>(orderStore.values());
    }

    /**
     * Latency metrics.
     */
    public record LatencyMetrics(
            String orderId,
            int brokerLatencyMs,
            int systemLatencyMs,
            int networkLatencyMs,
            int totalLatencyMs,
            Instant recordedAt) {
        public static LatencyMetrics capture(String orderId, long startTime) {
            long total = System.currentTimeMillis() - startTime;
            return new LatencyMetrics(
                    orderId,
                    (int) (total * 0.6), // Estimate broker latency
                    (int) (total * 0.3), // Estimate system latency
                    (int) (total * 0.1), // Estimate network latency
                    (int) total,
                    Instant.now());
        }
    }

    /**
     * Audit event.
     */
    public record AuditEvent(
            String orderId,
            String eventType,
            Map<String, String> payload,
            Instant createdAt) {
    }
}
