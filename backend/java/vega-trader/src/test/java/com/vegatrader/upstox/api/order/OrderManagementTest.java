package com.vegatrader.upstox.api.order;

import com.vegatrader.upstox.api.order.model.*;
import com.vegatrader.upstox.api.order.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Order Management Module per order-mgmt/a1.md and a2.md.
 */
class OrderManagementTest {

    private OrderPersistenceOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new OrderPersistenceOrchestrator();
    }

    // === Order Model Tests ===

    @Test
    @DisplayName("Order: build and verify")
    void orderBuild() {
        var order = Order.builder()
                .orderId("ORD123")
                .brokerOrderId("BRK123")
                .userId("USER1")
                .exchange("NSE")
                .symbol("RELIANCE")
                .side(Order.OrderSide.BUY)
                .orderType(Order.OrderType.LIMIT)
                .product(Order.ProductType.I)
                .quantity(100)
                .price(new BigDecimal("2500.00"))
                .status(Order.OrderStatus.ACKNOWLEDGED)
                .build();

        assertEquals("ORD123", order.orderId());
        assertEquals(100, order.quantity());
        assertFalse(order.isComplete());
    }

    @Test
    @DisplayName("Order: status checks")
    void orderStatusChecks() {
        var filled = Order.builder()
                .orderId("ORD1")
                .userId("U1")
                .status(Order.OrderStatus.FILLED)
                .build();

        var cancelled = Order.builder()
                .orderId("ORD2")
                .userId("U1")
                .status(Order.OrderStatus.CANCELLED)
                .build();

        var pending = Order.builder()
                .orderId("ORD3")
                .userId("U1")
                .status(Order.OrderStatus.PENDING)
                .build();

        assertTrue(filled.isComplete());
        assertTrue(cancelled.isComplete());
        assertFalse(pending.isComplete());
    }

    @Test
    @DisplayName("Order: pending quantity")
    void orderPendingQuantity() {
        var order = new Order(
                1L, "ORD1", "BRK1", "U1", "UPSTOX",
                "NSE", "SBIN", "NSE_EQ|3045",
                Order.OrderSide.BUY, Order.OrderType.LIMIT, Order.ProductType.D,
                100, new BigDecimal("500.00"), null,
                Order.OrderStatus.PARTIALLY_FILLED, 40, new BigDecimal("499.50"),
                Instant.now(), Instant.now(), null, null);

        assertEquals(60, order.getPendingQuantity());
        assertTrue(order.isPartiallyFilled());
    }

    // === OrderCharges Tests ===

    @Test
    @DisplayName("OrderCharges: build with total calculation")
    void orderChargesBuild() {
        var charges = OrderCharges.builder()
                .orderId("ORD123")
                .brokerage(new BigDecimal("20.00"))
                .gst(new BigDecimal("3.60"))
                .stt(new BigDecimal("12.00"))
                .exchangeTxnCharge(new BigDecimal("3.40"))
                .stampDuty(new BigDecimal("1.50"))
                .sebiCharge(new BigDecimal("0.35"))
                .build();

        assertEquals("ORD123", charges.orderId());
        // Total = 20 + 3.60 + 12 + 3.40 + 1.50 + 0.35 = 40.85
        assertEquals(new BigDecimal("40.85"), charges.totalCharges());
    }

    @Test
    @DisplayName("OrderCharges: from brokerage estimate")
    void orderChargesFromEstimate() {
        var estimate = new OrderCharges.BrokerageEstimate(
                new BigDecimal("20.00"),
                new BigDecimal("3.40"),
                new BigDecimal("0.35"),
                new BigDecimal("12.00"),
                new BigDecimal("1.50"),
                new BigDecimal("4.27"),
                new BigDecimal("41.52"));

        var charges = OrderCharges.from("ORD999", estimate);

        assertEquals("ORD999", charges.orderId());
        assertEquals(new BigDecimal("20.00"), charges.brokerage());
        assertEquals(new BigDecimal("41.52"), charges.totalCharges());
    }

    // === OrderPersistenceOrchestrator Tests ===

    @Test
    @DisplayName("Orchestrator: persist order after ACK")
    void orchestratorPersistAfterAck() {
        var order = Order.builder()
                .orderId("ORD-PERSIST-1")
                .brokerOrderId("BRK-001")
                .userId("USER1")
                .status(Order.OrderStatus.ACKNOWLEDGED)
                .build();

        var estimate = new OrderCharges.BrokerageEstimate(
                new BigDecimal("20"), new BigDecimal("3"), new BigDecimal("0.5"),
                new BigDecimal("10"), new BigDecimal("1"), new BigDecimal("4"),
                new BigDecimal("38.5"));

        orchestrator.persist(order, estimate, null);

        var retrieved = orchestrator.getOrder("ORD-PERSIST-1");
        assertTrue(retrieved.isPresent());
        assertEquals("BRK-001", retrieved.get().brokerOrderId());
    }

    @Test
    @DisplayName("Orchestrator: charges stored correctly")
    void orchestratorChargesStored() {
        var order = Order.builder()
                .orderId("ORD-CHARGES-1")
                .userId("USER1")
                .build();

        var estimate = new OrderCharges.BrokerageEstimate(
                new BigDecimal("25"), new BigDecimal("4"), new BigDecimal("0.6"),
                new BigDecimal("15"), new BigDecimal("2"), new BigDecimal("5"),
                new BigDecimal("51.6"));

        orchestrator.persist(order, estimate, null);

        var charges = orchestrator.getCharges("ORD-CHARGES-1");
        assertTrue(charges.isPresent());
        assertEquals(new BigDecimal("51.6"), charges.get().totalCharges());
    }

    @Test
    @DisplayName("Orchestrator: latency captured")
    void orchestratorLatencyCaptured() {
        var order = Order.builder()
                .orderId("ORD-LATENCY-1")
                .userId("USER1")
                .build();

        var latency = OrderPersistenceOrchestrator.LatencyMetrics.capture(
                "ORD-LATENCY-1", System.currentTimeMillis() - 50);

        orchestrator.persist(order, null, latency);

        var retrieved = orchestrator.getLatency("ORD-LATENCY-1");
        assertTrue(retrieved.isPresent());
        assertTrue(retrieved.get().totalLatencyMs() >= 50);
    }

    @Test
    @DisplayName("Orchestrator: status update")
    void orchestratorStatusUpdate() {
        var order = Order.builder()
                .orderId("ORD-STATUS-1")
                .userId("USER1")
                .status(Order.OrderStatus.ACKNOWLEDGED)
                .build();

        orchestrator.persist(order, null, null);

        orchestrator.updateStatus("ORD-STATUS-1", Order.OrderStatus.FILLED);

        var updated = orchestrator.getOrder("ORD-STATUS-1");
        assertTrue(updated.isPresent());
        assertEquals(Order.OrderStatus.FILLED, updated.get().status());
    }

    @Test
    @DisplayName("Orchestrator: audit log created")
    void orchestratorAuditLog() {
        var order = Order.builder()
                .orderId("ORD-AUDIT-1")
                .userId("USER1")
                .build();

        orchestrator.persist(order, null, null);
        orchestrator.updateStatus("ORD-AUDIT-1", Order.OrderStatus.CANCELLED);

        var auditLog = orchestrator.getAuditLog("ORD-AUDIT-1");
        assertEquals(2, auditLog.size());
        assertEquals("ORDER_PERSISTED", auditLog.get(0).eventType());
        assertEquals("STATUS_CHANGED", auditLog.get(1).eventType());
    }

    @Test
    @DisplayName("Orchestrator: get orders by user")
    void orchestratorOrdersByUser() {
        orchestrator.persist(Order.builder().orderId("ORD-U1-1").userId("USER1").build(), null, null);
        orchestrator.persist(Order.builder().orderId("ORD-U1-2").userId("USER1").build(), null, null);
        orchestrator.persist(Order.builder().orderId("ORD-U2-1").userId("USER2").build(), null, null);

        var user1Orders = orchestrator.getOrdersByUser("USER1");
        assertEquals(2, user1Orders.size());

        var user2Orders = orchestrator.getOrdersByUser("USER2");
        assertEquals(1, user2Orders.size());
    }

    @Test
    @DisplayName("Orchestrator: recent orders limit")
    void orchestratorRecentOrdersLimit() {
        for (int i = 0; i < 10; i++) {
            orchestrator.persist(
                    Order.builder().orderId("ORD-RECENT-" + i).userId("USER1").build(),
                    null, null);
        }

        var recent = orchestrator.getRecentOrders("USER1", 5);
        assertEquals(5, recent.size());
    }
}
