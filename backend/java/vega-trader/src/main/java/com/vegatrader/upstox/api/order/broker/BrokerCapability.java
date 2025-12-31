package com.vegatrader.upstox.api.order.broker;

import java.util.Set;

/**
 * Broker capability descriptor.
 * Per order-mgmt/b4.md section 5.2.
 * 
 * @since 4.9.0
 */
public record BrokerCapability(
        String brokerName,
        boolean supportsMultiOrder,
        boolean supportsModify,
        boolean supportsCancelMulti,
        boolean supportsExitAll,
        boolean supportsSlicing,
        int maxOrdersPerBatch,
        int rateLimitPerMinute,
        Set<String> supportedSegments,
        Set<String> supportedOrderTypes,
        Set<String> supportedProducts) {

    /**
     * Upstox capabilities.
     */
    public static BrokerCapability UPSTOX = new BrokerCapability(
            "UPSTOX",
            true, // supportsMultiOrder
            true, // supportsModify
            true, // supportsCancelMulti
            true, // supportsExitAll
            true, // supportsSlicing
            25, // maxOrdersPerBatch
            60, // rateLimitPerMinute
            Set.of("NSE_EQ", "BSE_EQ", "NSE_FO", "BSE_FO", "MCX", "CDS"),
            Set.of("MARKET", "LIMIT", "SL", "SL-M"),
            Set.of("I", "D", "CO", "MTF"));

    /**
     * Zerodha capabilities (example).
     */
    public static BrokerCapability ZERODHA = new BrokerCapability(
            "ZERODHA",
            false, // supportsMultiOrder - Zerodha doesn't have native multi-order
            true, // supportsModify
            false, // supportsCancelMulti
            true, // supportsExitAll
            false, // supportsSlicing
            1, // maxOrdersPerBatch (simulate one-by-one)
            200, // rateLimitPerMinute (higher limit)
            Set.of("NSE", "BSE", "NFO", "BFO", "MCX", "CDS"),
            Set.of("MARKET", "LIMIT", "SL", "SL-M"),
            Set.of("MIS", "CNC", "NRML"));

    /**
     * Fyers capabilities (example).
     */
    public static BrokerCapability FYERS = new BrokerCapability(
            "FYERS",
            false, // supportsMultiOrder
            true, // supportsModify
            true, // supportsCancelMulti
            false, // supportsExitAll
            false, // supportsSlicing
            1, // maxOrdersPerBatch
            100, // rateLimitPerMinute
            Set.of("NSE", "BSE", "NFO", "MCX"),
            Set.of("MARKET", "LIMIT", "SL", "SL-M"),
            Set.of("INTRADAY", "CNC", "MARGIN"));

    /**
     * Check if segment is supported.
     */
    public boolean supportsSegment(String segment) {
        return supportedSegments.contains(segment);
    }

    /**
     * Check if order type is supported.
     */
    public boolean supportsOrderType(String orderType) {
        return supportedOrderTypes.contains(orderType);
    }

    /**
     * Check if product is supported.
     */
    public boolean supportsProduct(String product) {
        return supportedProducts.contains(product);
    }

    /**
     * Get capability by broker name.
     */
    public static BrokerCapability forBroker(String brokerName) {
        return switch (brokerName.toUpperCase()) {
            case "UPSTOX" -> UPSTOX;
            case "ZERODHA" -> ZERODHA;
            case "FYERS" -> FYERS;
            default -> throw new IllegalArgumentException("Unknown broker: " + brokerName);
        };
    }
}
