package com.vegatrader.controller;

import com.vegatrader.upstox.api.websocket.config.MarketDataProperties;
import com.vegatrader.upstox.api.websocket.ratelimiter.EnterpriseRateLimiterService;
import com.vegatrader.upstox.api.websocket.settings.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for market data settings.
 * 
 * <p>
 * Provides endpoints for:
 * <ul>
 * <li>GET /api/v1/marketdata/settings - Get current settings</li>
 * <li>GET /api/v1/marketdata/limits - Get subscription limits</li>
 * <li>GET /api/v1/marketdata/limits/{userType} - Get limits for specific user
 * type</li>
 * </ul>
 * 
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/marketdata")
public class MarketDataSettingsController {

    private final MarketDataProperties properties;
    private final EnterpriseRateLimiterService rateLimiterService;

    public MarketDataSettingsController(MarketDataProperties properties,
            EnterpriseRateLimiterService rateLimiterService) {
        this.properties = properties;
        this.rateLimiterService = rateLimiterService;
    }

    /**
     * Gets current market data settings.
     */
    @GetMapping("/settings")
    public ResponseEntity<MarketDataProperties> getSettings() {
        return ResponseEntity.ok(properties);
    }

    /**
     * Gets all subscription limits.
     */
    @GetMapping("/limits")
    public ResponseEntity<Map<String, Object>> getLimits() {
        Map<String, Object> response = new HashMap<>();

        // Connection limits
        Map<String, Integer> connectionLimits = new HashMap<>();
        connectionLimits.put("normal", LimitsConfig.NORMAL_CONNECTIONS);
        connectionLimits.put("plus", LimitsConfig.PLUS_CONNECTIONS);
        response.put("connectionLimits", connectionLimits);

        // Subscription limits - Normal users
        Map<String, Map<String, Integer>> normalLimits = new HashMap<>();
        for (Map.Entry<SubscriptionCategory, SubscriptionLimits> entry : LimitsConfig.NORMAL_LIMITS.entrySet()) {
            Map<String, Integer> limit = new HashMap<>();
            limit.put("individual", entry.getValue().getIndividualLimit());
            limit.put("combined", entry.getValue().getCombinedLimit());
            normalLimits.put(entry.getKey().name(), limit);
        }
        response.put("normalLimits", normalLimits);

        // Subscription limits - Plus users
        Map<String, Map<String, Integer>> plusLimits = new HashMap<>();
        for (Map.Entry<SubscriptionCategory, SubscriptionLimits> entry : LimitsConfig.PLUS_LIMITS.entrySet()) {
            Map<String, Integer> limit = new HashMap<>();
            limit.put("individual", entry.getValue().getIndividualLimit());
            limit.put("combined", entry.getValue().getCombinedLimit());
            plusLimits.put(entry.getKey().name(), limit);
        }
        response.put("plusLimits", plusLimits);

        return ResponseEntity.ok(response);
    }

    /**
     * Gets limits for a specific user type.
     */
    @GetMapping("/limits/{userType}")
    public ResponseEntity<Map<String, Object>> getLimitsForUserType(
            @PathVariable String userType) {
        UserType type;
        try {
            type = UserType.valueOf(userType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("userType", type);
        response.put("connectionLimit", LimitsConfig.getConnectionLimit(type));

        Map<String, Map<String, Integer>> subscriptionLimits = new HashMap<>();
        for (Map.Entry<SubscriptionCategory, SubscriptionLimits> entry : LimitsConfig.getLimits(type).entrySet()) {
            Map<String, Integer> limit = new HashMap<>();
            limit.put("individual", entry.getValue().getIndividualLimit());
            limit.put("combined", entry.getValue().getCombinedLimit());
            subscriptionLimits.put(entry.getKey().name(), limit);
        }
        response.put("subscriptionLimits", subscriptionLimits);

        return ResponseEntity.ok(response);
    }

    /**
     * Gets active users count from rate limiter.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeUsers", rateLimiterService.getActiveUserCount());
        stats.put("enabled", properties.isEnabled());
        stats.put("autoStart", properties.isAutoStart());
        stats.put("bufferCapacity", properties.getBufferCapacity());
        return ResponseEntity.ok(stats);
    }
}
