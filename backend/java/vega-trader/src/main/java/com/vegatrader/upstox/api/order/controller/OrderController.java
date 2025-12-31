package com.vegatrader.upstox.api.order.controller;

import com.vegatrader.upstox.api.order.model.*;
import com.vegatrader.upstox.api.order.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Order management REST controller.
 * Per order-mgmt/a1.md section 7.
 * 
 * @since 4.8.0
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderPersistenceOrchestrator orchestrator;

    public OrderController(OrderPersistenceOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    /**
     * GET /api/orders - Get user's orders.
     */
    @GetMapping
    public ResponseEntity<List<Order>> getOrders(
            @RequestHeader(value = "X-User-Id", defaultValue = "demo") String userId,
            @RequestParam(defaultValue = "20") int limit) {

        List<Order> orders = orchestrator.getRecentOrders(userId, limit);
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/orders/{orderId} - Get order details.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable String orderId) {
        return orchestrator.getOrder(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/orders/{orderId}/full - Get order with charges and latency.
     */
    @GetMapping("/{orderId}/full")
    public ResponseEntity<Map<String, Object>> getFullOrder(@PathVariable String orderId) {
        var order = orchestrator.getOrder(orderId);
        if (order.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("order", order.get());
        orchestrator.getCharges(orderId).ifPresent(c -> result.put("charges", c));
        orchestrator.getLatency(orderId).ifPresent(l -> result.put("latency", l));
        result.put("audit", orchestrator.getAuditLog(orderId));

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/orders/{orderId}/charges - Get order charges.
     */
    @GetMapping("/{orderId}/charges")
    public ResponseEntity<?> getCharges(@PathVariable String orderId) {
        return orchestrator.getCharges(orderId)
                .map(charges -> ResponseEntity.ok(Map.of(
                        "brokerage", charges.brokerage(),
                        "gst", charges.gst(),
                        "stt", charges.stt(),
                        "exchange", charges.exchangeTxnCharge(),
                        "stampDuty", charges.stampDuty(),
                        "sebi", charges.sebiCharge(),
                        "total", charges.totalCharges())))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/orders - Place order (demo).
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> placeOrder(
            @RequestBody PlaceOrderRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "demo") String userId) {

        long startTime = System.currentTimeMillis();

        // Generate order ID
        String orderId = "ORD" + System.currentTimeMillis();
        String brokerOrderId = "BRK" + System.currentTimeMillis();

        // Build order
        Order order = Order.builder()
                .orderId(orderId)
                .brokerOrderId(brokerOrderId)
                .userId(userId)
                .exchange(request.exchange())
                .symbol(request.symbol())
                .instrumentKey(request.instrumentKey())
                .side(Order.OrderSide.valueOf(request.side()))
                .orderType(Order.OrderType.valueOf(request.orderType()))
                .product(Order.ProductType.valueOf(request.product()))
                .quantity(request.quantity())
                .price(request.price())
                .status(Order.OrderStatus.ACKNOWLEDGED)
                .build();

        // Estimate charges (mock)
        var charges = new OrderCharges.BrokerageEstimate(
                new BigDecimal("20.00"),
                new BigDecimal("3.40"),
                new BigDecimal("0.35"),
                new BigDecimal("12.00"),
                new BigDecimal("1.50"),
                new BigDecimal("4.27"),
                new BigDecimal("41.52"));

        // Capture latency
        var latency = OrderPersistenceOrchestrator.LatencyMetrics.capture(orderId, startTime);

        // Persist
        orchestrator.persist(order, charges, latency);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "orderId", orderId,
                "brokerOrderId", brokerOrderId,
                "status", order.status().name(),
                "charges", Map.of(
                        "total", charges.total())));
    }

    /**
     * POST /api/orders/{orderId}/cancel - Cancel order.
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable String orderId) {
        var order = orchestrator.getOrder(orderId);
        if (order.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        orchestrator.updateStatus(orderId, Order.OrderStatus.CANCELLED);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "orderId", orderId,
                "status", "CANCELLED"));
    }

    /**
     * Request DTO.
     */
    public record PlaceOrderRequest(
            String exchange,
            String symbol,
            String instrumentKey,
            String side,
            String orderType,
            String product,
            int quantity,
            BigDecimal price) {
    }
}
