package com.vegatrader.controller;

import com.vegatrader.upstox.api.websocket.manager.SubscriptionManager;
import com.vegatrader.upstox.api.websocket.ratelimiter.ApiRateLimiterService;
import com.vegatrader.upstox.api.websocket.settings.SubscriptionCategory;
import com.vegatrader.upstox.api.websocket.settings.UserType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for subscription management.
 * 
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionManagerController {

    private final SubscriptionManager subscriptionManager;
    private final ApiRateLimiterService apiRateLimiterService;

    public SubscriptionManagerController(SubscriptionManager subscriptionManager,
            ApiRateLimiterService apiRateLimiterService) {
        this.subscriptionManager = subscriptionManager;
        this.apiRateLimiterService = apiRateLimiterService;
    }

    /**
     * Checks if a subscription is allowed.
     */
    @GetMapping("/{userId}/can-subscribe")
    public ResponseEntity<Map<String, Object>> canSubscribe(
            @PathVariable String userId,
            @RequestParam String category,
            @RequestParam int keys) {

        SubscriptionCategory cat = parseCategory(category);
        if (cat == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid category: " + category));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("category", category);
        response.put("keysRequested", keys);
        response.put("allowed", subscriptionManager.canSubscribe(userId, cat, keys));
        response.put("remainingCapacity", subscriptionManager.getRemainingCapacity(userId, cat));
        response.put("currentCount", subscriptionManager.getCurrentSubscriptionCount(userId, cat));

        return ResponseEntity.ok(response);
    }

    /**
     * Subscribes to instruments.
     */
    @PostMapping("/{userId}/subscribe")
    public ResponseEntity<Map<String, Object>> subscribe(
            @PathVariable String userId,
            @RequestParam String category,
            @RequestBody List<String> instrumentKeys) {

        SubscriptionCategory cat = parseCategory(category);
        if (cat == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid category: " + category));
        }

        // Check API rate limit first
        if (!apiRateLimiterService.tryAcquireStandard()) {
            return ResponseEntity.status(429).body(Map.of(
                    "error", "API rate limit exceeded",
                    "message", "Please wait and retry"));
        }

        boolean success = subscriptionManager.subscribe(userId, cat, instrumentKeys);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("category", category);
        response.put("keysSubscribed", instrumentKeys.size());
        response.put("success", success);
        response.put("currentCount", subscriptionManager.getCurrentSubscriptionCount(userId, cat));

        return success ? ResponseEntity.ok(response) : ResponseEntity.status(429).body(response);
    }

    /**
     * Unsubscribes from instruments.
     */
    @PostMapping("/{userId}/unsubscribe")
    public ResponseEntity<Map<String, Object>> unsubscribe(
            @PathVariable String userId,
            @RequestParam String category,
            @RequestBody List<String> instrumentKeys) {

        SubscriptionCategory cat = parseCategory(category);
        if (cat == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid category: " + category));
        }

        subscriptionManager.unsubscribe(userId, cat, instrumentKeys);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("category", category);
        response.put("keysUnsubscribed", instrumentKeys.size());
        response.put("currentCount", subscriptionManager.getCurrentSubscriptionCount(userId, cat));

        return ResponseEntity.ok(response);
    }

    /**
     * Unsubscribes from all instruments in a category.
     */
    @DeleteMapping("/{userId}/{category}")
    public ResponseEntity<Map<String, Object>> unsubscribeCategory(
            @PathVariable String userId,
            @PathVariable String category) {

        SubscriptionCategory cat = parseCategory(category);
        if (cat == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid category: " + category));
        }

        subscriptionManager.unsubscribeAll(userId, cat);

        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "category", category,
                "message", "Unsubscribed from all instruments"));
    }

    /**
     * Unsubscribes user from everything.
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> unsubscribeAll(@PathVariable String userId) {
        subscriptionManager.unsubscribeAll(userId);
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "message", "Unsubscribed from all categories"));
    }

    /**
     * Gets user subscription status.
     */
    @GetMapping("/{userId}/status")
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("userType", subscriptionManager.getUserType(userId));
        response.put("activeCategories", subscriptionManager.getActiveCategories(userId));

        Map<String, Integer> subscriptions = new HashMap<>();
        for (SubscriptionCategory cat : subscriptionManager.getActiveCategories(userId)) {
            subscriptions.put(cat.name(), subscriptionManager.getCurrentSubscriptionCount(userId, cat));
        }
        response.put("subscriptions", subscriptions);

        return ResponseEntity.ok(response);
    }

    /**
     * Sets user type.
     */
    @PutMapping("/{userId}/type")
    public ResponseEntity<Map<String, Object>> setUserType(
            @PathVariable String userId,
            @RequestParam String type) {

        UserType userType;
        try {
            userType = UserType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid user type: " + type,
                    "validTypes", UserType.values()));
        }

        subscriptionManager.setUserType(userId, userType);

        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "userType", userType));
    }

    /**
     * Gets global statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> response = new HashMap<>();
        response.put("totalSubscriptions", subscriptionManager.getTotalSubscriptions());
        response.put("subscriptionsByCategory", subscriptionManager.getSubscriptionsByCategory());
        response.put("apiRates", Map.of(
                "standard", apiRateLimiterService.getStandardApiRate(),
                "multiOrder", apiRateLimiterService.getMultiOrderApiRate()));
        return ResponseEntity.ok(response);
    }

    private SubscriptionCategory parseCategory(String category) {
        try {
            return SubscriptionCategory.valueOf(category.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            // Try by mode
            try {
                return SubscriptionCategory.fromMode(category.toLowerCase());
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
    }
}
