package com.vegatrader.upstox.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for logging API requests and responses.
 *
 * @since 2.0.0
 */
public final class ApiLogger {

    private static final Logger logger = LoggerFactory.getLogger(ApiLogger.class);

    private ApiLogger() {
        // Utility class - no instantiation
    }

    /**
     * Logs API request.
     *
     * @param endpoint the endpoint
     * @param method   the HTTP method
     * @param params   the parameters
     */
    public static void logRequest(String endpoint, String method, Object params) {
        if (logger.isDebugEnabled()) {
            logger.debug("API Request: {} {} - Params: {}", method, endpoint, params);
        }
    }

    /**
     * Logs API response.
     *
     * @param endpoint   the endpoint
     * @param statusCode the HTTP status code
     * @param response   the response
     */
    public static void logResponse(String endpoint, int statusCode, Object response) {
        if (logger.isDebugEnabled()) {
            logger.debug("API Response: {} - Status: {} - Response: {}",
                    endpoint, statusCode, response);
        }
    }

    /**
     * Logs API error.
     *
     * @param endpoint the endpoint
     * @param error    the error
     */
    public static void logError(String endpoint, Exception error) {
        logger.error("API Error: {} - Error: {}", endpoint, error.getMessage(), error);
    }

    /**
     * Logs rate limit event.
     *
     * @param endpoint the endpoint
     * @param status   the rate limit status
     */
    public static void logRateLimit(String endpoint, String status) {
        logger.warn("Rate Limit: {} - Status: {}", endpoint, status);
    }

    /**
     * Logs order placement.
     *
     * @param orderId       the order ID
     * @param instrumentKey the instrument
     * @param orderType     the order type
     * @param quantity      the quantity
     */
    public static void logOrderPlaced(String orderId, String instrumentKey,
            String orderType, int quantity) {
        logger.info("Order Placed: ID={}, Instrument={}, Type={}, Quantity={}",
                orderId, instrumentKey, orderType, quantity);
    }

    /**
     * Logs order execution.
     *
     * @param orderId      the order ID
     * @param executedQty  the executed quantity
     * @param averagePrice the average price
     */
    public static void logOrderExecuted(String orderId, int executedQty, double averagePrice) {
        logger.info("Order Executed: ID={}, Qty={}, Avg Price={}",
                orderId, executedQty, averagePrice);
    }
}
