package com.vegatrader.upstox.auth;

/**
 * Defines specific capabilities required for Upstox tokens.
 * Used for capability-based token validation and auditing.
 */
public enum TokenCapability {
    /**
     * General REST API access (Orders, Funds, User Profile).
     * Maps to: PRIMARY token.
     */
    CORE_REST,

    /**
     * Market Data WebSocket Feed.
     * Maps to: WEBSOCKET1..N tokens (Load Balanced).
     */
    MARKET_DATA_WS,

    /**
     * Portfolio WebSocket Feed.
     * Maps to: WEBSOCKET1..N tokens (Shared Infrastructure).
     */
    PORTFOLIO_WS,

    /**
     * Option Chain REST / Snapshot calls.
     * Maps to: OPTIONCHAIN1..N tokens (Isolated Burst Capacity).
     */
    OPTION_CHAIN
}
