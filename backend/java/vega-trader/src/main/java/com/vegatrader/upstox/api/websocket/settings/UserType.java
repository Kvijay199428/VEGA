package com.vegatrader.upstox.api.websocket.settings;

/**
 * User type for subscription limit enforcement.
 * 
 * @since 2.0.0
 */
public enum UserType {
    /**
     * Normal user with basic limits:
     * - 2 connections max
     * - LTPC: 5000 individual / 2000 combined
     * - Option Greeks: 3000 individual / 2000 combined
     * - Full: 2000 individual / 1500 combined
     */
    NORMAL,

    /**
     * Upstox Plus user with enhanced limits:
     * - 5 connections max
     * - Full D30: 50 individual / 1500 combined
     */
    PLUS
}
