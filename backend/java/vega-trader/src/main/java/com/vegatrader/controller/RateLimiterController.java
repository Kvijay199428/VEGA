package com.vegatrader.controller;

import com.vegatrader.upstox.api.websocket.ratelimiter.EnterpriseRateLimiterService;
import com.vegatrader.upstox.api.websocket.ratelimiter.RateLimitExceededException;
import com.vegatrader.upstox.api.websocket.settings.UserType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for rate limiter management.
 * 
 * <p>
 * Provides endpoints for:
 * <ul>
 * <li>GET /api/v1/ratelimiter/{userId}/usage - Get current usage</li>
 * <li>POST /api/v1/ratelimiter/{userId}/subscribe - Attempt subscription</li>
 * <li>POST /api/v1/ratelimiter/{userId}/release - Release tokens</li>
 * <li>PUT /api/v1/ratelimiter/{userId}/type - Set user type</li>
 * </ul>
 * 
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/ratelimiter")
public class RateLimiterController {

    private final EnterpriseRateLimiterService rateLimiterService;

    public RateLimiterController(EnterpriseRateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    /**
     * Gets current usage for a user.
     */
    @GetMapping("/{userId}/usage")
    public ResponseEntity<Map<String, Object>> getCurrentUsage(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("usage", rateLimiterService.currentUsage(userId));
        response.put("totalKeys", rateLimiterService.getTotalActiveKeys(userId));
        return ResponseEntity.ok(response);
    }

    /**
     * Attempts to subscribe to a category.
     */
    @PostMapping("/{userId}/subscribe")
    public ResponseEntity<Map<String, Object>> subscribe(
            @PathVariable String userId,
            @RequestParam String category,
            @RequestParam int keys) {

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("category", category);
        response.put("keysRequested", keys);

        boolean allowed = rateLimiterService.trySubscribe(userId, category, keys);
        response.put("allowed", allowed);

        if (allowed) {
            response.put("currentUsage", rateLimiterService.currentUsage(userId));
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Rate limit exceeded");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
        }
    }

    /**
     * Releases subscription tokens.
     */
    @PostMapping("/{userId}/release")
    public ResponseEntity<Map<String, Object>> release(
            @PathVariable String userId,
            @RequestParam String category,
            @RequestParam int keys) {

        rateLimiterService.release(userId, category, keys);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("category", category);
        response.put("keysReleased", keys);
        response.put("currentUsage", rateLimiterService.currentUsage(userId));
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
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid user type: " + type);
            error.put("validTypes", UserType.values());
            return ResponseEntity.badRequest().body(error);
        }

        rateLimiterService.setUserType(userId, userType);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("userType", userType);
        response.put("limits", rateLimiterService.getUserLimits(userId));
        return ResponseEntity.ok(response);
    }

    /**
     * Removes a user from tracking.
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> removeUser(@PathVariable String userId) {
        rateLimiterService.removeUser(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("removed", true);
        return ResponseEntity.ok(response);
    }

    /**
     * Exception handler for rate limit exceeded.
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimitExceeded(RateLimitExceededException e) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Rate limit exceeded");
        error.put("message", e.getMessage());
        error.put("category", e.getCategory());
        error.put("requested", e.getRequested());
        error.put("available", e.getAvailable());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }
}
