package com.vegatrader.upstox.auth.controller;

import com.vegatrader.upstox.auth.service.TokenStorageService;
import com.vegatrader.upstox.auth.service.BatchAuthenticationService;
import com.vegatrader.upstox.auth.service.BatchAuthenticationService.AuthStatus;
import com.vegatrader.upstox.auth.entity.UpstoxTokenEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Basic Auth Controller.
 * Handles session checks and manual login/logout for Frontend Auth.
 *
 * Base URL: /api/auth
 */
@RestController
@RequestMapping("/api/auth")
public class BasicAuthController {

    private static final Logger logger = LoggerFactory.getLogger(BasicAuthController.class);

    @Autowired
    private TokenStorageService tokenStorageService;

    @Autowired
    private BatchAuthenticationService batchAuthService;

    /**
     * GET /api/auth/session
     * Checks if valid Upstox tokens exist to determine "session" validity.
     */
    @GetMapping("/session")
    public ResponseEntity<?> getSession() {
        // Check if UPSTOX token exists and is valid
        Optional<UpstoxTokenEntity> token = tokenStorageService.getToken("UPSTOX");
        boolean isValid = token.isPresent() && token.get().isActive();

        Map<String, Object> response = new HashMap<>();
        if (isValid) {
            response.put("status", "SUCCESS");

            // Mock user details since we don't have a user DB yet
            Map<String, String> user = new HashMap<>();
            user.put("name", "Vega Admin");
            user.put("role", "ADMIN");
            user.put("email", "admin@vegatrader.com");

            response.put("user", user);
            response.put("message", "Session active");
        } else {
            response.put("status", "FAILED");
            response.put("message", "No active session");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/auth/status
     * Returns current authentication status for multi-API token generation.
     * Includes database lock, cache status, valid tokens list, missing APIs, and
     * cooldown.
     */
    @GetMapping("/status")
    public ResponseEntity<?> getAuthStatus() {
        AuthStatus status = batchAuthService.getStatus();

        Map<String, Object> response = new HashMap<>();
        response.put("requiredTokens", status.getRequiredTokens());
        response.put("generatedTokens", status.getGeneratedTokens());
        response.put("authenticated", status.isAuthenticated());
        response.put("inProgress", status.isInProgress());
        response.put("currentApi", status.getCurrentApi());

        // Database lock resilience fields
        response.put("dbLocked", status.isDbLocked());
        response.put("pendingInCache", status.getPendingInCache());
        response.put("recoveryInProgress", status.isRecoveryInProgress());

        // Valid tokens and missing APIs
        response.put("validTokens", status.getValidTokens());
        response.put("missingApis", status.getMissingApis());

        // Rate limit cooldown fields
        response.put("rateLimited", status.isRateLimited());
        response.put("cooldownEndsAt", status.getCooldownEndsAt());
        response.put("cooldownMessage", status.getCooldownMessage());

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/login
     * Manual login stub. Actual login happens via Selenium/OAuth.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        // Since this is a local desktop app, we might check a hardcoded PIN or just
        // allow.
        // For now, we delegate to Selenium or just return success if password matches
        // 'admin'.

        String password = payload.get("password");
        if ("admin".equals(password)) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            Map<String, String> user = new HashMap<>();
            user.put("name", "Vega Admin");
            user.put("role", "ADMIN");
            response.put("user", user);
            return ResponseEntity.ok(response);
        }

        Map<String, Object> error = new HashMap<>();
        error.put("status", "FAILED");
        error.put("message", "Invalid credentials");
        return ResponseEntity.status(401).body(error);
    }

    /**
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // No server-side session to clear in stateless JWT/Token auth,
        // but we can log it.
        logger.info("User logged out");

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        return ResponseEntity.ok(response);
    }
}
