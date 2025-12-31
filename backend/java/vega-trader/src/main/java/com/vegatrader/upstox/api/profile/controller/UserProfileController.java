package com.vegatrader.upstox.api.profile.controller;

import com.vegatrader.upstox.api.profile.model.*;
import com.vegatrader.upstox.api.profile.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * User profile REST controller.
 * Per profile/a1.md section 10.
 * 
 * These endpoints proxy cached backend data, not broker directly.
 * 
 * @since 4.8.0
 */
@RestController
@RequestMapping("/api/user")
public class UserProfileController {

    private final UserProfileService profileService;
    private final FundsMarginService fundsService;

    public UserProfileController(
            UserProfileService profileService,
            FundsMarginService fundsService) {
        this.profileService = profileService;
        this.fundsService = fundsService;
    }

    /**
     * GET /api/user/profile - Get user profile.
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfile> getProfile(
            @RequestHeader(value = "X-User-Id", defaultValue = "demo") String userId) {

        UserProfile profile = profileService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    /**
     * POST /api/user/profile/refresh - Force refresh profile.
     */
    @PostMapping("/profile/refresh")
    public ResponseEntity<UserProfile> refreshProfile(
            @RequestHeader(value = "X-User-Id", defaultValue = "demo") String userId) {

        UserProfile profile = profileService.refreshProfile(userId);
        return ResponseEntity.ok(profile);
    }

    /**
     * GET /api/user/funds - Get funds and margin.
     */
    @GetMapping("/funds")
    public ResponseEntity<?> getFunds(
            @RequestHeader(value = "X-User-Id", defaultValue = "demo") String userId) {

        try {
            FundsMargin funds = fundsService.getFunds(userId);
            return ResponseEntity.ok(funds);
        } catch (FundsMarginService.FundsMaintenanceException e) {
            return ResponseEntity.status(503).body(Map.of(
                    "error", "MAINTENANCE_WINDOW",
                    "message", e.getMessage()));
        }
    }

    /**
     * POST /api/user/funds/refresh - Force refresh funds.
     */
    @PostMapping("/funds/refresh")
    public ResponseEntity<?> refreshFunds(
            @RequestHeader(value = "X-User-Id", defaultValue = "demo") String userId) {

        try {
            FundsMargin funds = fundsService.refreshFunds(userId);
            return ResponseEntity.ok(funds);
        } catch (FundsMarginService.FundsMaintenanceException e) {
            return ResponseEntity.status(503).body(Map.of(
                    "error", "MAINTENANCE_WINDOW",
                    "message", e.getMessage()));
        }
    }

    /**
     * GET /api/user/margin/check - Check margin sufficiency.
     */
    @GetMapping("/margin/check")
    public ResponseEntity<Map<String, Object>> checkMargin(
            @RequestHeader(value = "X-User-Id", defaultValue = "demo") String userId,
            @RequestParam double required) {

        try {
            FundsMargin funds = fundsService.getFunds(userId);
            boolean sufficient = funds.hasSufficientMargin(required);

            return ResponseEntity.ok(Map.of(
                    "required", required,
                    "available", funds.availableMargin(),
                    "sufficient", sufficient,
                    "combinedMargin", funds.combinedMargin()));
        } catch (FundsMarginService.FundsMaintenanceException e) {
            return ResponseEntity.status(503).body(Map.of(
                    "error", "MAINTENANCE_WINDOW",
                    "message", e.getMessage()));
        }
    }

    /**
     * GET /api/user/eligibility - Check user eligibility for action.
     */
    @GetMapping("/eligibility")
    public ResponseEntity<Map<String, Object>> checkEligibility(
            @RequestHeader(value = "X-User-Id", defaultValue = "demo") String userId,
            @RequestParam(required = false) String exchange,
            @RequestParam(required = false) String product,
            @RequestParam(required = false) String orderType) {

        UserProfile profile = profileService.getProfile(userId);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("userId", userId);
        result.put("active", profile.active());
        result.put("canSell", profile.canSell());

        if (exchange != null) {
            try {
                boolean enabled = profile.hasExchange(UserProfile.Exchange.valueOf(exchange));
                result.put("exchangeEnabled", enabled);
            } catch (IllegalArgumentException e) {
                result.put("exchangeEnabled", false);
            }
        }

        if (product != null) {
            try {
                boolean enabled = profile.hasProduct(UserProfile.ProductType.valueOf(product));
                result.put("productEnabled", enabled);
            } catch (IllegalArgumentException e) {
                result.put("productEnabled", false);
            }
        }

        if (orderType != null) {
            try {
                boolean enabled = profile.hasOrderType(UserProfile.OrderType.valueOf(orderType.replace("-", "_")));
                result.put("orderTypeEnabled", enabled);
            } catch (IllegalArgumentException e) {
                result.put("orderTypeEnabled", false);
            }
        }

        return ResponseEntity.ok(result);
    }
}
