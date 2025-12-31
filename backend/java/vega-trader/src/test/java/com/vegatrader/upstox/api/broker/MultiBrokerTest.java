package com.vegatrader.upstox.api.broker;

import com.vegatrader.upstox.api.broker.model.*;
import com.vegatrader.upstox.api.broker.engine.MultiBrokerEngine;
import com.vegatrader.upstox.api.broker.adapter.BrokerAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Multi-Broker module.
 * Tests order routing, portfolio aggregation, and adapter logic.
 * 
 * Section 10: Integration Tests
 */
class MultiBrokerTest {

    private MultiBrokerEngine engine;

    @BeforeEach
    void setUp() {
        engine = new MultiBrokerEngine();
    }

    // === 10.1 OrderRequest Tests ===

    @Test
    @DisplayName("OrderRequest: market buy factory")
    void orderRequestMarketBuy() {
        OrderRequest order = OrderRequest.marketBuy("NSE_EQ|INE002A01018", "RELIANCE", "CNC", 100);

        assertEquals("NSE_EQ|INE002A01018", order.instrumentKey());
        assertEquals("RELIANCE", order.brokerSymbol());
        assertEquals("MARKET", order.orderType());
        assertEquals("BUY", order.transactionType());
        assertEquals(100, order.qty());
        assertTrue(order.isBuy());
    }

    @Test
    @DisplayName("OrderRequest: limit order factory")
    void orderRequestLimitOrder() {
        OrderRequest order = OrderRequest.limit("NSE_EQ|INE002A01018", "RELIANCE", "MIS", "SELL", 50, 2500.0);

        assertEquals("LIMIT", order.orderType());
        assertEquals("SELL", order.transactionType());
        assertEquals(2500.0, order.price());
        assertEquals(125000.0, order.orderValue());
        assertFalse(order.isBuy());
    }

    // === 10.2 BrokerOrderStatus Tests ===

    @Test
    @DisplayName("BrokerOrderStatus: status checks")
    void brokerOrderStatusChecks() {
        BrokerOrderStatus complete = new BrokerOrderStatus(
                "ORD1", "UPSTOX_1", "NSE_EQ|INE002A01018", "COMPLETE",
                "CNC", "LIMIT", "BUY", 100, 100, 0, 2500.0, 2498.5, null, LocalDateTime.now());

        BrokerOrderStatus rejected = new BrokerOrderStatus(
                "ORD2", "UPSTOX_2", "NSE_EQ|INE002A01018", "REJECTED",
                "CNC", "LIMIT", "BUY", 100, 0, 100, 2500.0, 0, "Price outside band", LocalDateTime.now());

        assertTrue(complete.isComplete());
        assertFalse(complete.isRejected());
        assertEquals(249850.0, complete.filledValue());

        assertTrue(rejected.isRejected());
        assertFalse(rejected.isComplete());
    }

    // === 10.3 Position Tests ===

    @Test
    @DisplayName("Position: long/short detection")
    void positionLongShortDetection() {
        Position longPos = new Position("NSE_EQ|INE002A01018", "RELIANCE", "NSE", "CNC",
                100, 100, 0, 2500.0, 0, 5000.0, 5000.0, 0, 2550.0);

        Position shortPos = new Position("NSE_FO|NIFTY", "NIFTY25JAN", "NSE", "MIS",
                -50, 0, 50, 0, 24000.0, -2500.0, -2500.0, 0, 24050.0);

        Position flat = new Position("NSE_EQ|INE001A01018", "INFY", "NSE", "CNC",
                0, 50, 50, 1500.0, 1550.0, 2500.0, 0, 2500.0, 1550.0);

        assertTrue(longPos.isLong());
        assertFalse(longPos.isShort());

        assertTrue(shortPos.isShort());
        assertFalse(shortPos.isLong());

        assertTrue(flat.isFlat());
    }

    // === 10.4 MultiBrokerEngine Tests ===

    @Test
    @DisplayName("MultiBrokerEngine: register and get adapters")
    void engineRegisterAndGetAdapters() {
        BrokerAdapter mockAdapter = mock(BrokerAdapter.class);
        when(mockAdapter.getBrokerId()).thenReturn("UPSTOX");

        engine.registerAdapter("UPSTOX", mockAdapter);

        assertTrue(engine.getAvailableBrokers().contains("UPSTOX"));
    }

    @Test
    @DisplayName("MultiBrokerEngine: throws for unknown broker")
    void engineThrowsForUnknownBroker() {
        OrderRequest order = OrderRequest.marketBuy("NSE_EQ|INE002A01018", "RELIANCE", "CNC", 100);

        assertThrows(IllegalArgumentException.class, () -> engine.routeOrder("UNKNOWN", order));
    }

    @Test
    @DisplayName("MultiBrokerEngine: routes order to correct adapter")
    void engineRoutesOrderCorrectly() {
        BrokerAdapter mockAdapter = mock(BrokerAdapter.class);
        when(mockAdapter.getBrokerId()).thenReturn("UPSTOX");
        when(mockAdapter.placeOrder(any())).thenReturn(BrokerOrderResponse.success("ORD1", "UPSTOX_1"));

        engine.registerAdapter("UPSTOX", mockAdapter);

        OrderRequest order = OrderRequest.marketBuy("NSE_EQ|INE002A01018", "RELIANCE", "CNC", 100);
        BrokerOrderResponse response = engine.routeOrder("UPSTOX", order);

        assertTrue(response.success());
        verify(mockAdapter).placeOrder(order);
    }

    @Test
    @DisplayName("MultiBrokerEngine: aggregates positions from all brokers")
    void engineAggregatesPositions() {
        BrokerAdapter adapter1 = mock(BrokerAdapter.class);
        BrokerAdapter adapter2 = mock(BrokerAdapter.class);

        when(adapter1.getBrokerId()).thenReturn("UPSTOX");
        when(adapter2.getBrokerId()).thenReturn("FYERS");

        when(adapter1.getPositions()).thenReturn(List.of(
                new Position("NSE_EQ|INE002A01018", "RELIANCE", "NSE", "CNC", 100, 100, 0, 2500.0, 0, 5000.0, 5000.0, 0,
                        2550.0)));
        when(adapter2.getPositions()).thenReturn(List.of(
                new Position("NSE_EQ|INE001A01018", "INFY", "NSE", "CNC", 50, 50, 0, 1500.0, 0, 2000.0, 2000.0, 0,
                        1540.0)));

        engine.registerAdapter("UPSTOX", adapter1);
        engine.registerAdapter("FYERS", adapter2);

        List<Position> aggregated = engine.getAggregatedPositions();

        assertEquals(2, aggregated.size());
    }

    @Test
    @DisplayName("MultiBrokerEngine: calculates aggregated P&L")
    void engineCalculatesAggregatedPnl() {
        BrokerAdapter adapter1 = mock(BrokerAdapter.class);
        BrokerAdapter adapter2 = mock(BrokerAdapter.class);

        when(adapter1.getBrokerId()).thenReturn("UPSTOX");
        when(adapter2.getBrokerId()).thenReturn("FYERS");

        when(adapter1.getPositions()).thenReturn(List.of(
                new Position("NSE_EQ|INE002A01018", "RELIANCE", "NSE", "CNC", 100, 100, 0, 2500.0, 0, 5000.0, 5000.0, 0,
                        2550.0)));
        when(adapter2.getPositions()).thenReturn(List.of(
                new Position("NSE_EQ|INE001A01018", "INFY", "NSE", "CNC", 50, 50, 0, 1500.0, 0, -2000.0, -2000.0, 0,
                        1460.0)));

        engine.registerAdapter("UPSTOX", adapter1);
        engine.registerAdapter("FYERS", adapter2);

        double totalPnl = engine.getAggregatedPnl();

        assertEquals(3000.0, totalPnl); // 5000 + (-2000)
    }

    // === 10.5 BrokerOrderResponse Tests ===

    @Test
    @DisplayName("BrokerOrderResponse: success factory")
    void brokerOrderResponseSuccess() {
        BrokerOrderResponse response = BrokerOrderResponse.success("ORD1", "UPSTOX_1");

        assertTrue(response.success());
        assertEquals("ORD1", response.orderId());
        assertEquals("UPSTOX_1", response.brokerOrderId());
    }

    @Test
    @DisplayName("BrokerOrderResponse: failure factory")
    void brokerOrderResponseFailure() {
        BrokerOrderResponse response = BrokerOrderResponse.failure("Insufficient margin");

        assertFalse(response.success());
        assertNull(response.orderId());
        assertEquals("Insufficient margin", response.message());
    }
}
