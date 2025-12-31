package com.vegatrader.upstox.api.examples;

import com.vegatrader.upstox.api.endpoints.OrderEndpoints;
import com.vegatrader.upstox.api.ratelimit.*;
import com.vegatrader.upstox.api.request.order.PlaceOrderRequest;
import com.vegatrader.upstox.api.response.common.ApiResponse;
import com.vegatrader.upstox.api.response.order.OrderResponse;
import com.vegatrader.upstox.api.sectoral.*;

import java.util.List;

/**
 * Comprehensive examples for Phase 2 enhancements.
 * <p>
 * This class demonstrates practical usage of:
 * <ul>
 * <li>Common Response DTOs</li>
 * <li>Rate Limiting System</li>
 * <li>Sectoral Indices Integration</li>
 * <li>Order Request/Response DTOs</li>
 * </ul>
 * </p>
 *
 * @since 2.0.0
 */
public class Phase2Examples {

    public static void main(String[] args) {
        System.out.println("=== Upstox API Phase 2 Examples ===\n");

        example1_ResponseDTOs();
        example2_RateLimiting();
        example3_SectoralIndices();
        example4_OrderDTOs();
    }

    /**
     * Example 1: Using Response DTOs
     */
    private static void example1_ResponseDTOs() {
        System.out.println("Example 1: Response DTOs");
        System.out.println("------------------------");

        // Create a successful API response
        OrderResponse orderData = new OrderResponse();
        orderData.setOrderId("240127000123456");
        orderData.setOrderState("PENDING");
        orderData.setStatusMessage("Order placed successfully");

        ApiResponse<OrderResponse> successResponse = ApiResponse.success(orderData);

        if (successResponse.isSuccess()) {
            System.out.println("✓ Order placed: " + successResponse.getData().getOrderId());
        }

        // Create an error response
        ApiResponse<OrderResponse> errorResponse = ApiResponse.error(
                "insufficient_funds",
                "Not enough balance to place order");

        if (errorResponse.isError()) {
            System.out.println("✗ Error: " + errorResponse.getErrors().getMessage());
        }

        System.out.println();
    }

    /**
     * Example 2: Rate Limiting
     */
    private static void example2_RateLimiting() {
        System.out.println("Example 2: Rate Limiting");
        System.out.println("------------------------");

        // Get the rate limit manager
        RateLimitManager manager = RateLimitManager.getInstance();

        // Check if we can make a request
        RateLimitStatus status = manager.checkLimit(OrderEndpoints.PLACE_ORDER);

        if (status.isAllowed()) {
            System.out.println("✓ Request allowed");

            // Simulate making the API call
            manager.recordRequest(OrderEndpoints.PLACE_ORDER);

            // Get current usage
            RateLimitUsage usage = manager.getCurrentUsage(OrderEndpoints.PLACE_ORDER);
            System.out.println("  Current usage: " + usage);
        } else {
            System.out.println("✗ Rate limit exceeded: " + status.getMessage());

            // Wait and retry
            boolean allowed = manager.waitAndRetry(OrderEndpoints.PLACE_ORDER, 3);
            System.out.println("  Retry successful: " + allowed);
        }

        // Direct usage of specific limiters
        StandardAPIRateLimiter standardLimiter = new StandardAPIRateLimiter();
        System.out.println("\nStandard API Limiter:");
        System.out.println("  Config: " + standardLimiter.getConfig());
        System.out.println("  Usage: " + standardLimiter.getCurrentUsage());

        MultiOrderAPIRateLimiter multiOrderLimiter = new MultiOrderAPIRateLimiter();
        System.out.println("\nMulti-Order API Limiter:");
        System.out.println("  Config: " + multiOrderLimiter.getConfig());
        System.out.println("  Max orders per batch: " + multiOrderLimiter.getMaxOrdersPerRequest());

        System.out.println();
    }

    /**
     * Example 3: Sectoral Indices
     */
    private static void example3_SectoralIndices() {
        System.out.println("Example 3: Sectoral Indices");
        System.out.println("---------------------------");

        // List all sectors
        System.out.println("All NSE Sectors:");
        for (SectoralIndex sector : SectoralIndex.values()) {
            System.out.println("  " + sector.getSectorKey() + " → " + sector.getDisplayName());
        }

        // Get sectors by group
        System.out.println("\nBanking Sectors:");
        SectoralIndex[] bankingSectors = SectoralIndex.getSectorsByGroup("BANKING");
        for (SectoralIndex sector : bankingSectors) {
            System.out.println("  " + sector.getDisplayName());
            System.out.println("    URL: " + sector.getFullUrl());
        }

        // Fetch sector data (simulated - actual fetch would hit NSE)
        System.out.println("\nFetching Nifty Bank constituents...");
        try {
            SectorDataFetcher fetcher = new SectorDataFetcher();
            SectorCache cache = new SectorCache();

            // This would actually download from NSE
            // List<SectorConstituent> bankStocks = cache.getOrFetch(
            // SectoralIndex.BANK,
            // () -> fetcher.fetchSectorData(SectoralIndex.BANK)
            // );

            // Create sample data for demonstration
            SectorConstituent sample = SectorConstituent.builder()
                    .symbol("HDFCBANK")
                    .companyName("HDFC Bank Limited")
                    .industry("Banking")
                    .isinCode("INE040A01034")
                    .weight(25.30)
                    .build();

            System.out.println("  Sample constituent: " + sample);
            System.out.println("  Instrument key: " + sample.generateInstrumentKey());

        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }

        System.out.println();
    }

    /**
     * Example 4: Order DTOs
     */
    private static void example4_OrderDTOs() {
        System.out.println("Example 4: Order Request/Response DTOs");
        System.out.println("--------------------------------------");

        // Create a market order request
        PlaceOrderRequest marketOrder = PlaceOrderRequest.builder()
                .quantity(1)
                .product("D")
                .validity("DAY")
                .instrumentKey("NSE_EQ|INE528G01035")
                .transactionType("BUY")
                .asMarketOrder()
                .tag("example_order")
                .build();

        System.out.println("Market Order Request: " + marketOrder);

        try {
            marketOrder.validate();
            System.out.println("✓ Validation passed");
        } catch (IllegalArgumentException e) {
            System.out.println("✗ Validation failed: " + e.getMessage());
        }

        // Create a limit order request
        PlaceOrderRequest limitOrder = PlaceOrderRequest.builder()
                .quantity(2)
                .product("MIS")
                .validity("DAY")
                .instrumentKey("NSE_EQ|INE669E01016")
                .transactionType("SELL")
                .asLimitOrder(150.50)
                .build();

        System.out.println("\nLimit Order Request: " + limitOrder);

        // Create a stop-loss order request
        PlaceOrderRequest stopLossOrder = PlaceOrderRequest.builder()
                .quantity(1)
                .product("D")
                .validity("DAY")
                .instrumentKey("NSE_EQ|INE002A01018")
                .transactionType("SELL")
                .asStopLossOrder(2850.00, 2845.00) // trigger at 2850, limit at 2845
                .build();

        System.out.println("\nStop-Loss Order Request: " + stopLossOrder);

        // Simulate order response
        OrderResponse response = new OrderResponse();
        response.setOrderId("240127000123456");
        response.setExchangeOrderId("1234567890");
        response.setOrderState("PENDING");
        response.setStatusMessage("Order placed successfully");
        response.setFilledQuantity(0);
        response.setPendingQuantity(1);

        System.out.println("\nOrder Response:");
        System.out.println("  Order ID: " + response.getOrderId());
        System.out.println("  State: " + response.getOrderState());
        System.out.println("  Is Pending: " + response.isPending());
        System.out.println("  Is Complete: " + response.isComplete());
        System.out.println("  Is Cancelled: " + response.isCancelled());

        System.out.println();
    }
}
