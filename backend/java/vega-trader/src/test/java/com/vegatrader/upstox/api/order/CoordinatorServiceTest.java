package com.vegatrader.upstox.api.order;

import com.vegatrader.upstox.api.order.model.*;
import com.vegatrader.upstox.api.order.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Coordinator Service per order-mgmt/main2/b1-b4.md.
 */
class CoordinatorServiceTest {

    private OrderPersistenceOrchestrator orchestrator;
    private MultiOrderService multiOrderService;
    private OrderModifyService modifyService;
    private CoordinatorService coordinatorService;

    @BeforeEach
    void setUp() {
        orchestrator = new OrderPersistenceOrchestrator();
        multiOrderService = new MultiOrderService(orchestrator);
        modifyService = new OrderModifyService(orchestrator);
        coordinatorService = new CoordinatorService(orchestrator, multiOrderService, modifyService);
    }

    // === Trade Model Tests ===

    @Test
    @DisplayName("Trade: build with charges")
    void tradeBuild() {
        var trade = Trade.builder()
                .tradeId("TRD001")
                .orderId("ORD001")
                .exchange("NSE")
                .segment("EQ")
                .tradingSymbol("RELIANCE-EQ")
                .instrumentToken("NSE_EQ|INE002A01018")
                .transactionType("BUY")
                .quantity(100)
                .price(new BigDecimal("2500.00"))
                .averagePrice(new BigDecimal("2500.50"))
                .brokerage(new BigDecimal("20.00"))
                .stt(new BigDecimal("12.50"))
                .totalCharges(new BigDecimal("40.00"))
                .grossValue(new BigDecimal("250000.00"))
                .netValue(new BigDecimal("250040.00"))
                .build();

        assertEquals("TRD001", trade.tradeId());
        assertEquals("NSE", trade.exchange());
        assertEquals(100, trade.quantity());
        assertEquals(new BigDecimal("40.00"), trade.totalCharges());
    }

    // === CoordinatorService - Idempotency Tests ===

    @Test
    @DisplayName("Coordinator: idempotent place multi order")
    void coordinatorIdempotentPlace() {
        var request = new MultiOrderRequest(List.of(
                new MultiOrderRequest.OrderLine("C1", 50, "I", "DAY", BigDecimal.ZERO,
                        "MARKET", "BUY", "NSE_EQ|SBIN", null, 0, BigDecimal.ZERO, false, false)));

        String idempotencyKey = "IDEM-" + UUID.randomUUID();

        // First call
        var response1 = coordinatorService.placeMultiOrder(request, "USER1", idempotencyKey);
        assertEquals("success", response1.status());
        String orderId = response1.data().get(0).orderId();

        // Second call with same key - should return cached response
        var response2 = coordinatorService.placeMultiOrder(request, "USER1", idempotencyKey);
        assertEquals("success", response2.status());
        assertEquals(orderId, response2.data().get(0).orderId()); // Same order ID
    }

    @Test
    @DisplayName("Coordinator: idempotent modify order")
    void coordinatorIdempotentModify() {
        // Place order first
        orchestrator.persist(Order.builder()
                .orderId("ORD-IDEM-MOD")
                .userId("USER1")
                .quantity(100)
                .price(new BigDecimal("500"))
                .status(Order.OrderStatus.OPEN)
                .build(), null, null);

        String idempotencyKey = "IDEM-MOD-" + UUID.randomUUID();

        var modifyRequest = new OrderModifyService.ModifyRequest(
                "ORD-IDEM-MOD", null, 150, "DAY", new BigDecimal("510"), null, 0, null);

        // First call
        var result1 = coordinatorService.modifyOrder(modifyRequest, idempotencyKey);
        assertEquals("success", result1.status());

        // Second call with same key
        var result2 = coordinatorService.modifyOrder(modifyRequest, idempotencyKey);
        assertEquals("success", result2.status());
        assertEquals(result1.latencyMs(), result2.latencyMs()); // Same cached result
    }

    // === CoordinatorService - Read-Side Tests ===

    @Test
    @DisplayName("Coordinator: order book with cache")
    void coordinatorOrderBook() {
        // Place some orders
        orchestrator.persist(Order.builder()
                .orderId("ORD-BOOK-1")
                .userId("USER1")
                .status(Order.OrderStatus.OPEN)
                .placedAt(Instant.now())
                .build(), null, null);

        orchestrator.persist(Order.builder()
                .orderId("ORD-BOOK-2")
                .userId("USER1")
                .status(Order.OrderStatus.FILLED)
                .placedAt(Instant.now())
                .build(), null, null);

        var response = coordinatorService.getOrderBook("USER1");

        assertEquals("success", response.status());
        assertEquals(2, response.data().size());
        assertNotNull(response.lastUpdate());
    }

    @Test
    @DisplayName("Coordinator: order history with events")
    void coordinatorOrderHistory() {
        // Place and update order
        orchestrator.persist(Order.builder()
                .orderId("ORD-HIST-1")
                .userId("USER1")
                .status(Order.OrderStatus.ACKNOWLEDGED)
                .placedAt(Instant.now())
                .build(), null, null);

        orchestrator.updateStatus("ORD-HIST-1", Order.OrderStatus.FILLED);

        var response = coordinatorService.getOrderHistory("ORD-HIST-1", null);

        assertEquals("success", response.status());
        assertEquals(1, response.orders().size());
        assertEquals(2, response.events().size()); // PERSISTED + STATUS_CHANGED
    }

    @Test
    @DisplayName("Coordinator: trades for day")
    void coordinatorTradesForDay() {
        // Place order and register trade
        orchestrator.persist(Order.builder()
                .orderId("ORD-TRADE-1")
                .userId("USER1")
                .status(Order.OrderStatus.FILLED)
                .placedAt(Instant.now())
                .build(), null, null);

        coordinatorService.registerTrade(Trade.builder()
                .tradeId("TRD-1")
                .orderId("ORD-TRADE-1")
                .exchange("NSE")
                .quantity(100)
                .price(new BigDecimal("500"))
                .build());

        var response = coordinatorService.getTradesForDay("USER1");

        assertEquals("success", response.status());
        assertEquals(1, response.data().size());
    }

    @Test
    @DisplayName("Coordinator: trades for order")
    void coordinatorTradesForOrder() {
        // Register multiple trades for same order
        coordinatorService.registerTrade(Trade.builder()
                .tradeId("TRD-A1")
                .orderId("ORD-MULTI-TRADE")
                .quantity(50)
                .build());

        coordinatorService.registerTrade(Trade.builder()
                .tradeId("TRD-A2")
                .orderId("ORD-MULTI-TRADE")
                .quantity(50)
                .build());

        var trades = coordinatorService.getTradesForOrder("ORD-MULTI-TRADE");

        assertEquals(2, trades.size());
    }

    @Test
    @DisplayName("Coordinator: trade history with pagination")
    void coordinatorTradeHistory() {
        // Register multiple trades
        for (int i = 0; i < 10; i++) {
            coordinatorService.registerTrade(Trade.builder()
                    .tradeId("TRD-HIST-" + i)
                    .orderId("ORD-" + i)
                    .segment("EQ")
                    .quantity(100)
                    .build());
        }

        // Page 1
        var page1 = coordinatorService.getTradeHistory("USER1", "EQ",
                LocalDate.now().minusDays(1), LocalDate.now(), 1, 5);

        assertEquals("success", page1.status());
        assertEquals(5, page1.data().size());
        assertEquals(10, page1.metadata().totalRecords());
        assertEquals(2, page1.metadata().totalPages());

        // Page 2
        var page2 = coordinatorService.getTradeHistory("USER1", "EQ",
                LocalDate.now().minusDays(1), LocalDate.now(), 2, 5);

        assertEquals(5, page2.data().size());
    }

    @Test
    @DisplayName("Coordinator: cancel multi order idempotent")
    void coordinatorCancelMultiIdempotent() {
        // Place orders
        var placeRequest = new MultiOrderRequest(List.of(
                new MultiOrderRequest.OrderLine("C1", 10, "I", "DAY", BigDecimal.ZERO,
                        "MARKET", "BUY", "NSE_EQ|TCS", null, 0, BigDecimal.ZERO, false, false)));
        var placeResponse = coordinatorService.placeMultiOrder(placeRequest, "USER1", null);
        String orderId = placeResponse.data().get(0).orderId();

        String idempotencyKey = "IDEM-CANCEL-" + UUID.randomUUID();

        // First cancel
        var cancel1 = coordinatorService.cancelMultiOrder(List.of(orderId), "USER1", idempotencyKey);
        assertEquals("success", cancel1.status());

        // Second cancel with same key
        var cancel2 = coordinatorService.cancelMultiOrder(List.of(orderId), "USER1", idempotencyKey);
        assertEquals("success", cancel2.status());
    }
}
