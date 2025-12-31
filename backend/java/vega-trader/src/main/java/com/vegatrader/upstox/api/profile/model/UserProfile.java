package com.vegatrader.upstox.api.profile.model;

import java.time.Instant;
import java.util.Set;

/**
 * User profile domain model.
 * Per profile/a1.md section 4.2.
 * 
 * Used for:
 * - Instrument universe gating (exchanges)
 * - MIS/CNC/MTF enforcement (products)
 * - Order form validation (orderTypes)
 * - Sell permission checks (poa/ddpi)
 * - Global kill-switch (active)
 * 
 * @since 4.8.0
 */
public record UserProfile(
        String userId,
        String broker,
        String name,
        String email,
        String pan,
        Set<Exchange> exchanges,
        Set<ProductType> products,
        Set<OrderType> orderTypes,
        boolean poa,
        boolean ddpi,
        boolean active,
        Instant fetchedAt) {

    /**
     * Check if exchange is enabled for this user.
     */
    public boolean hasExchange(Exchange exchange) {
        return exchanges != null && exchanges.contains(exchange);
    }

    /**
     * Check if product type is enabled for this user.
     */
    public boolean hasProduct(ProductType product) {
        return products != null && products.contains(product);
    }

    /**
     * Check if order type is enabled for this user.
     */
    public boolean hasOrderType(OrderType orderType) {
        return orderTypes != null && orderTypes.contains(orderType);
    }

    /**
     * Check if user can sell (requires POA or DDPI).
     */
    public boolean canSell() {
        return poa || ddpi;
    }

    /**
     * Check if profile is stale (older than TTL).
     */
    public boolean isStale(int ttlSeconds) {
        if (fetchedAt == null)
            return true;
        return Instant.now().minusSeconds(ttlSeconds).isAfter(fetchedAt);
    }

    /**
     * Exchange enum.
     */
    public enum Exchange {
        NSE, BSE, NFO, BFO, MCX, CDS
    }

    /**
     * Product type enum.
     */
    public enum ProductType {
        I, // Intraday (MIS)
        D, // Delivery (CNC)
        CO, // Cover Order
        MTF // Margin Trading Facility
    }

    /**
     * Order type enum.
     */
    public enum OrderType {
        MARKET, LIMIT, SL, SL_M
    }
}
