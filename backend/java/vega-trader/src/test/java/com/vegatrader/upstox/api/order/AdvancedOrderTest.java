package com.vegatrader.upstox.api.order;

import com.vegatrader.upstox.api.order.model.*;
import com.vegatrader.upstox.api.order.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Advanced Order APIs per order-mgmt/main1/a1-a7.md.
 */
class AdvancedOrderTest {

    private OrderPersistenceOrchestrator orchestrator;
    private MultiOrderService multiOrderService;
    private OrderModifyService modifyService;

    @BeforeEach
    void setUp() {
        orchestrator = new OrderPersistenceOrchestrator();
        multiOrderService = new MultiOrderService(orchestrator);
        modifyService = new OrderModifyService(orchestrator);
    }

    // === MultiOrderRequest Tests ===

    @Test
    @DisplayName("MultiOrderRequest: validate order line")
    void multiOrderValidation() {
        var validLine = new MultiOrderRequest.OrderLine(
                "CORR001", 100, "D", "DAY", new BigDecimal("100"),
                "LIMIT", "BUY", "NSE_EQ|RELIANCE", "STRAT_A",
                0, BigDecimal.ZERO, false, false);

        assertTrue(validLine.validate().valid());

        var invalidLine = new MultiOrderRequest.OrderLine(
                "CORRELATION_TOO_LONG_MORE_THAN_20_CHARS", 100, "D", "DAY",
                new BigDecimal("100"), "LIMIT", "BUY", "NSE_EQ|RELIANCE",
                null, 0, BigDecimal.ZERO, false, false);

        assertFalse(invalidLine.validate().valid());
    }

    // === MultiOrderResponse Tests ===

    @Test
    @DisplayName("MultiOrderResponse: build success")
    void multiOrderResponseSuccess() {
        var response = MultiOrderResponse.success(List.of(
                new MultiOrderResponse.OrderResult("CORR1", "ORD1"),
                new MultiOrderResponse.OrderResult("CORR2", "ORD2")));

        assertEquals("success", response.status());
        assertEquals(2, response.data().size());
        assertEquals(2, response.summary().success());
        assertEquals(0, response.summary().error());
    }

    @Test
    @DisplayName("MultiOrderResponse: build partial success")
    void multiOrderResponsePartial() {
        var response = MultiOrderResponse.partialSuccess(
                List.of(new MultiOrderResponse.OrderResult("CORR1", "ORD1")),
                List.of(new MultiOrderResponse.OrderError("CORR2", "INVALID_QTY", "Quantity exceeds limit")));

        assertEquals("partial_success", response.status());
        assertEquals(1, response.summary().success());
        assertEquals(1, response.summary().error());
    }

    @Test
    @DisplayName("MultiOrderResponse: builder pattern")
    void multiOrderResponseBuilder() {
        var builder = new MultiOrderResponse.Builder();
        builder.addSuccess("C1", "O1");
        builder.addSuccess("C2", "O2");
        builder.addError("C3", "ERR", "Failed");

        var response = builder.build();
        assertEquals("partial_success", response.status());
        assertEquals(2, response.data().size());
        assertEquals(1, response.errors().size());
    }

    // === MultiOrderService Tests ===

    @Test
    @DisplayName("MultiOrderService: place batch orders - BUY before SELL")
    void multiOrderPlaceBatch() {
        var request = new MultiOrderRequest(List.of(
                new MultiOrderRequest.OrderLine("SELL1", 100, "I", "DAY", BigDecimal.ZERO,
                        "MARKET", "SELL", "NSE_EQ|INFY", null, 0, BigDecimal.ZERO, false, false),
                new MultiOrderRequest.OrderLine("BUY1", 50, "I", "DAY", BigDecimal.ZERO,
                        "MARKET", "BUY", "NSE_EQ|TCS", null, 0, BigDecimal.ZERO, false, false)));

        var response = multiOrderService.placeMultiOrder(request, "USER1");

        assertEquals("success", response.status());
        assertEquals(2, response.data().size());

        // BUY should be processed first (appears first in results)
        assertTrue(response.data().get(0).correlationId().equals("BUY1"));
    }

    @Test
    @DisplayName("MultiOrderService: batch size limit")
    void multiOrderBatchLimit() {
        List<MultiOrderRequest.OrderLine> tooMany = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            tooMany.add(new MultiOrderRequest.OrderLine(
                    "C" + i, 100, "I", "DAY", BigDecimal.ZERO,
                    "MARKET", "BUY", "NSE_EQ|TEST", null, 0, BigDecimal.ZERO, false, false));
        }

        var response = multiOrderService.placeMultiOrder(new MultiOrderRequest(tooMany), "USER1");

        assertEquals("error", response.status());
        assertTrue(response.errors().get(0).message().contains("25"));
    }

    @Test
    @DisplayName("MultiOrderService: correlation ID mapping")
    void multiOrderCorrelationMapping() {
        var request = new MultiOrderRequest(List.of(
                new MultiOrderRequest.OrderLine("MY_CORR_ID", 10, "D", "DAY", new BigDecimal("100"),
                        "LIMIT", "BUY", "NSE_EQ|SBIN", null, 0, BigDecimal.ZERO, false, false)));

        multiOrderService.placeMultiOrder(request, "USER1");

        var orderId = multiOrderService.getOrderIdByCorrelation("MY_CORR_ID");
        assertTrue(orderId.isPresent());
        assertTrue(orderId.get().contains("MY_CORR_ID"));
    }

    @Test
    @DisplayName("MultiOrderService: cancel multi order")
    void multiOrderCancel() {
        // Place orders first
        var placeRequest = new MultiOrderRequest(List.of(
                new MultiOrderRequest.OrderLine("C1", 10, "I", "DAY", BigDecimal.ZERO,
                        "MARKET", "BUY", "NSE_EQ|TCS", null, 0, BigDecimal.ZERO, false, false),
                new MultiOrderRequest.OrderLine("C2", 20, "I", "DAY", BigDecimal.ZERO,
                        "MARKET", "BUY", "NSE_EQ|INFY", null, 0, BigDecimal.ZERO, false, false)));
        var placeResponse = multiOrderService.placeMultiOrder(placeRequest, "USER1");

        // Get order IDs
        List<String> orderIds = placeResponse.data().stream()
                .map(MultiOrderResponse.OrderResult::orderId)
                .toList();

        // Cancel them
        var cancelResponse = multiOrderService.cancelMultiOrder(orderIds, "USER1");

        assertEquals("success", cancelResponse.status());
        assertEquals(2, cancelResponse.data().size());
    }

    // === OrderModifyService Tests ===

    @Test
    @DisplayName("OrderModifyService: modify existing order")
    void modifyOrder() {
        // Place order first
        var order = Order.builder()
                .orderId("ORD-MODIFY-1")
                .userId("USER1")
                .quantity(100)
                .price(new BigDecimal("500.00"))
                .status(Order.OrderStatus.OPEN)
                .build();
        orchestrator.persist(order, null, null);

        // Modify it
        var modifyRequest = new OrderModifyService.ModifyRequest(
                "ORD-MODIFY-1", null, 150, "DAY",
                new BigDecimal("510.00"), "LIMIT", 0, null);

        var result = modifyService.modifyOrder(modifyRequest);

        assertEquals("success", result.status());
        assertTrue(result.updatedFields().contains("quantity"));
        assertTrue(result.updatedFields().contains("price"));
        assertTrue(result.latencyMs() >= 0);
    }

    @Test
    @DisplayName("OrderModifyService: cannot modify complete order")
    void modifyCompleteOrder() {
        var order = Order.builder()
                .orderId("ORD-FILLED-1")
                .userId("USER1")
                .status(Order.OrderStatus.FILLED)
                .build();
        orchestrator.persist(order, null, null);

        var result = modifyService.modifyOrder(new OrderModifyService.ModifyRequest(
                "ORD-FILLED-1", null, 200, "DAY", null, null, 0, null));

        assertEquals("error", result.status());
        assertEquals("MODIFY_NOT_ALLOWED", result.errorCode());
    }

    @Test
    @DisplayName("OrderModifyService: cancel order")
    void cancelOrder() {
        var order = Order.builder()
                .orderId("ORD-CANCEL-1")
                .userId("USER1")
                .status(Order.OrderStatus.OPEN)
                .build();
        orchestrator.persist(order, null, null);

        var result = modifyService.cancelOrder("ORD-CANCEL-1");

        assertEquals("success", result.status());
        assertTrue(result.latencyMs() >= 0);

        // Verify cancelled
        var cancelled = orchestrator.getOrder("ORD-CANCEL-1");
        assertEquals(Order.OrderStatus.CANCELLED, cancelled.get().status());
    }

    @Test
    @DisplayName("OrderModifyService: cancel non-existent order")
    void cancelNonExistent() {
        var result = modifyService.cancelOrder("NON_EXISTENT");

        assertEquals("error", result.status());
        assertEquals("ORDER_NOT_FOUND", result.errorCode());
    }

    // === Exit Positions Tests ===

    @Test
    @DisplayName("MultiOrderService: exit all positions")
    void exitAllPositions() {
        // Create open positions
        orchestrator.persist(Order.builder()
                .orderId("POS-BUY-1")
                .userId("USER1")
                .side(Order.OrderSide.BUY)
                .status(Order.OrderStatus.OPEN)
                .build(), null, null);

        orchestrator.persist(Order.builder()
                .orderId("POS-SELL-1")
                .userId("USER1")
                .side(Order.OrderSide.SELL)
                .status(Order.OrderStatus.OPEN)
                .build(), null, null);

        var response = multiOrderService.exitAllPositions(null, null, "USER1");

        assertEquals("success", response.status());
        assertEquals(2, response.data().size());
    }
}
