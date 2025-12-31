package com.vegatrader.upstox.api.examples;

import com.vegatrader.upstox.api.config.UpstoxEnvironment;
import com.vegatrader.upstox.api.config.UpstoxApiVersion;
import com.vegatrader.upstox.api.config.UpstoxWebSocketConfig;
import com.vegatrader.upstox.api.endpoints.*;
import com.vegatrader.upstox.api.errors.UpstoxErrorCode;
import com.vegatrader.upstox.api.errors.UpstoxHttpStatus;
import com.vegatrader.upstox.api.generator.UpstoxEndpointGenerator;

import java.util.Map;

/**
 * Comprehensive examples demonstrating how to use the Upstox API Endpoints
 * Generator.
 * <p>
 * This class contains runnable examples for:
 * <ul>
 * <li>Basic URL generation</li>
 * <li>Environment switching</li>
 * <li>Order management</li>
 * <li>Market data queries</li>
 * <li>Option chain URLs</li>
 * <li>WebSocket URLs</li>
 * <li>Error code handling</li>
 * </ul>
 * </p>
 *
 * @since 1.0.0
 */
public class UpstoxApiExamples {

    public static void main(String[] args) {
        System.out.println("=== Upstox API Endpoints Generator Examples ===\n");

        // Run all examples
        example1_BasicUsage();
        example2_EnvironmentSwitching();
        example3_OrderManagement();
        example4_MarketData();
        example5_OptionChain();
        example6_Portfolio();
        example7_WebSockets();
        example8_ErrorHandling();
        example9_EndpointRegistry();
    }

    /**
     * Example 1: Basic URL Generation
     */
    private static void example1_BasicUsage() {
        System.out.println("Example 1: Basic URL Generation");
        System.out.println("--------------------------------");

        // Create generator for production
        UpstoxEndpointGenerator generator = UpstoxEndpointGenerator.forProduction();

        // Generate simple URLs
        String profileUrl = generator.generateUrl(UserProfileEndpoints.USER_PROFILE);
        String fundsUrl = generator.generateUrl(UserProfileEndpoints.USER_FUNDS);

        System.out.println("User Profile URL: " + profileUrl);
        System.out.println("User Funds URL: " + fundsUrl);
        System.out.println();
    }

    /**
     * Example 2: Environment Switching
     */
    private static void example2_EnvironmentSwitching() {
        System.out.println("Example 2: Environment Switching");
        System.out.println("---------------------------------");

        // Production environment
        UpstoxEndpointGenerator prodGen = UpstoxEndpointGenerator.forProduction();
        System.out.println("Production URL: " + prodGen.generateUrl(UserProfileEndpoints.USER_PROFILE));

        // Sandbox environment (for testing)
        UpstoxEndpointGenerator sandboxGen = UpstoxEndpointGenerator.forSandbox();
        System.out.println("Sandbox URL: " + sandboxGen.generateUrl(UserProfileEndpoints.USER_PROFILE));

        // Production HFT API
        UpstoxEndpointGenerator hftGen = UpstoxEndpointGenerator.forProductionHft();
        System.out.println("HFT URL: " + hftGen.generateUrl(UserProfileEndpoints.USER_PROFILE));

        // Custom environment
        UpstoxEndpointGenerator customGen = UpstoxEndpointGenerator.forEnvironment(
                UpstoxEnvironment.SANDBOX,
                UpstoxApiVersion.V3_HFT);
        System.out.println("Custom (Sandbox HFT) URL: " + customGen.generateUrl(UserProfileEndpoints.USER_PROFILE));
        System.out.println();
    }

    /**
     * Example 3: Order Management
     */
    private static void example3_OrderManagement() {
        System.out.println("Example 3: Order Management");
        System.out.println("---------------------------");

        UpstoxEndpointGenerator generator = UpstoxEndpointGenerator.forProduction();

        // Place order
        System.out.println("Place Order: " + generator.generateUrl(OrderEndpoints.PLACE_ORDER));

        // Get order details (with path parameter)
        Map<String, String> pathParams = Map.of("order_id", "240127000123456");
        System.out.println("Get Order: " + generator.generateUrl(OrderEndpoints.GET_ORDER_DETAILS, pathParams, null));

        // Get all orders
        System.out.println("All Orders: " + generator.generateUrl(OrderEndpoints.GET_ALL_ORDERS));

        // GTT orders
        System.out.println("Create GTT: " + generator.generateUrl(OrderEndpoints.CREATE_GTT));
        System.out.println("Get GTT Orders: " + generator.generateUrl(OrderEndpoints.GET_GTT_ORDERS));

        // Trade P&L
        System.out.println("Trade P&L: " + generator.generateUrl(OrderEndpoints.GET_TRADE_PNL));
        System.out.println();
    }

    /**
     * Example 4: Market Data
     */
    private static void example4_MarketData() {
        System.out.println("Example 4: Market Data");
        System.out.println("----------------------");

        UpstoxEndpointGenerator generator = UpstoxEndpointGenerator.forProduction();

        // Full quote with instrument key
        Map<String, String> quoteParams = Map.of(
                "instrument_key", "NSE_EQ|INE848E01016");
        System.out.println("Full Quote: " + generator.generateUrl(MarketDataEndpoints.FULL_QUOTE, quoteParams));

        // Option Greeks
        Map<String, String> greeksParams = Map.of(
                "instrument_key", "NSE_FO|51059");
        System.out.println("Option Greeks: " + generator.generateUrl(MarketDataEndpoints.OPTION_GREEKS, greeksParams));

        // Historical candlestick data
        Map<String, String> candleParams = Map.of(
                "instrument_key", "NSE_EQ|INE848E01016",
                "interval", "1day",
                "from_date", "2025-01-01",
                "to_date", "2025-01-31");
        System.out.println("Candlestick: " + generator.generateUrl(MarketDataEndpoints.CANDLESTICK_DATA, candleParams));

        // Market status
        System.out.println("Market Status: " + generator.generateUrl(MarketDataEndpoints.MARKET_STATUS));
        System.out.println();
    }

    /**
     * Example 5: Option Chain
     */
    private static void example5_OptionChain() {
        System.out.println("Example 5: Option Chain");
        System.out.println("-----------------------");

        UpstoxEndpointGenerator generator = UpstoxEndpointGenerator.forProduction();

        // Option chain with expiry
        String chainUrl1 = generator.generateOptionChainUrl(
                "NSE_INDEX|Nifty 50",
                "2025-02-27");
        System.out.println("Option Chain (with expiry): " + chainUrl1);

        // Option chain without expiry (all available expiries)
        String chainUrl2 = generator.generateOptionChainUrl("NSE_INDEX|Nifty 50");
        System.out.println("Option Chain (all expiries): " + chainUrl2);

        // Using endpoint directly
        Map<String, String> chainParams = Map.of(
                "instrument_key", "NSE_INDEX|Bank Nifty",
                "expiry_date", "2025-03-05");
        System.out.println("Option Chain (via endpoint): " +
                generator.generateUrl(OptionChainEndpoints.GET_OPTION_CHAIN, chainParams));
        System.out.println();
    }

    /**
     * Example 6: Portfolio
     */
    private static void example6_Portfolio() {
        System.out.println("Example 6: Portfolio");
        System.out.println("--------------------");

        UpstoxEndpointGenerator generator = UpstoxEndpointGenerator.forProduction();

        // Holdings
        System.out.println("Holdings: " + generator.generateUrl(PortfolioEndpoints.GET_HOLDINGS));

        // Positions
        System.out.println("Positions: " + generator.generateUrl(PortfolioEndpoints.GET_POSITIONS));

        // Net positions
        System.out.println("Net Positions: " + generator.generateUrl(PortfolioEndpoints.GET_NET_POSITIONS));

        // Convert position
        System.out.println("Convert Position: " + generator.generateUrl(PortfolioEndpoints.CONVERT_POSITION));
        System.out.println();
    }

    /**
     * Example 7: WebSocket URLs
     */
    private static void example7_WebSockets() {
        System.out.println("Example 7: WebSocket URLs");
        System.out.println("-------------------------");

        UpstoxEndpointGenerator generator = UpstoxEndpointGenerator.forProduction();

        // Market data stream
        String marketWs = generator.generateMarketStreamUrl();
        System.out.println("Market Stream: " + marketWs);

        // Portfolio stream
        String portfolioWs = generator.generatePortfolioStreamUrl();
        System.out.println("Portfolio Stream: " + portfolioWs);

        // Direct config access
        String sandboxMarketWs = UpstoxWebSocketConfig.getMarketStreamUrl(UpstoxEnvironment.SANDBOX);
        System.out.println("Sandbox Market Stream: " + sandboxMarketWs);
        System.out.println();
    }

    /**
     * Example 8: Error Code Handling
     */
    private static void example8_ErrorHandling() {
        System.out.println("Example 8: Error Code Handling");
        System.out.println("------------------------------");

        // Get error details
        UpstoxErrorCode error = UpstoxErrorCode.INSUFFICIENT_FUNDS;
        System.out.println("Error Code: " + error.getCode());
        System.out.println("HTTP Status: " + error.getHttpStatus());
        System.out.println("Description: " + error.getDescription());
        System.out.println("Resolution: " + error.getResolution());
        System.out.println();

        // Find error by code
        UpstoxErrorCode foundError = UpstoxErrorCode.fromCode("rate_limit_exceeded");
        if (foundError != null) {
            System.out.println("Found Error: " + foundError);
        }

        // HTTP Status
        UpstoxHttpStatus status = UpstoxHttpStatus.UNPROCESSABLE_ENTITY;
        System.out.println("\nHTTP Status: " + status);
        System.out.println("Is Error: " + status.isError());
        System.out.println("Is Client Error: " + status.isClientError());
        System.out.println();
    }

    /**
     * Example 9: Endpoint Registry
     */
    private static void example9_EndpointRegistry() {
        System.out.println("Example 9: Endpoint Registry");
        System.out.println("----------------------------");

        // Print statistics
        UpstoxEndpointRegistry.printStatistics();

        // Get endpoints by category
        var orderEndpoints = UpstoxEndpointRegistry.getEndpointsByCategory("Orders");
        System.out.println("\nFound " + orderEndpoints.size() + " order endpoints");

        // Find specific endpoint
        var endpoint = UpstoxEndpointRegistry.findEndpoint(
                "/order/place",
                UpstoxEndpoint.HttpMethod.POST);
        endpoint.ifPresent(e -> System.out.println("Found endpoint: " + e.getDescription()));
    }
}
