package com.vegatrader.upstox.auth.controller;

import com.vegatrader.upstox.auth.service.CooldownService;
import com.vegatrader.upstox.auth.entity.UpstoxTokenEntity;
import com.vegatrader.upstox.auth.repository.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    private TokenRepository tokenRepository;

    @Autowired
    private com.vegatrader.upstox.auth.state.AuthSessionState authSessionState;

    @Autowired
    private CooldownService cooldownService;

    /**
     * GET /api/auth/session
     * Checks if valid Upstox tokens exist to determine "session" validity.
     */
    @GetMapping("/session")
    public ResponseEntity<?> getSession() {
        // Always return SUCCESS for the Admin User session (Identity layer),
        // but expose readiness flags for access control (Authorization layer).

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS"); // Always success for Admin

        response.put("configuredApis", authSessionState.getConfiguredApis());
        response.put("generatedTokens", authSessionState.getGeneratedCount());
        response.put("primaryReady", authSessionState.isPrimaryReady());
        response.put("fullyReady", authSessionState.isFullyReady());
        response.put("canProceed", authSessionState.isPrimaryReady()); // Legacy flag for dashboard access
        response.put("validTokens", authSessionState.getValidApis());
        response.put("missingApis", authSessionState.getMissingApis());

        // Mock user details
        Map<String, String> user = new HashMap<>();
        user.put("name", "Vega Admin");
        user.put("role", "ADMIN");
        user.put("email", "admin@vegatrader.com");

        response.put("user", user);
        response.put("message", "Session active (Readiness: " + authSessionState.getGeneratedCount() + "/"
                + authSessionState.getRequiredCount() + ")");

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/auth/status
     * Returns current authentication status for multi-API token generation.
     */
    @GetMapping("/status")
    public ResponseEntity<com.vegatrader.upstox.auth.response.AuthStatusResponse> getAuthStatus() {
        String state = "GENERATING_TOKENS";
        if (authSessionState.isFullyReady()) {
            state = "AUTH_CONFIRMED";
        } else if (authSessionState.isPrimaryReady()) {
            state = "PRIMARY_VALIDATED";
        } else if (authSessionState.getGeneratedCount() > 0) {
            state = "PARTIAL_AUTH";
        }

        return ResponseEntity.ok(
                com.vegatrader.upstox.auth.response.AuthStatusResponse.builder()
                        .state(state)
                        .authenticated(authSessionState.isPrimaryReady())
                        .primaryReady(authSessionState.isPrimaryReady())
                        .fullyReady(authSessionState.isFullyReady())
                        .generatedTokens(authSessionState.getGeneratedCount())
                        .requiredTokens(authSessionState.getRequiredCount())
                        .validTokens(authSessionState.getValidApis())
                        .missingApis(authSessionState.getMissingApis())
                        .cooldownActive(cooldownService.isTokenGenerationCooldownActive())
                        .remainingSeconds(cooldownService.remainingSeconds())
                        .build());
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

    /**
     * Get token expiry timeline.
     * Used for dashboard risk visualization (Bloomberg style).
     */
    @GetMapping("/tokens/timeline")
    public List<TokenTimelineDTO> getTokenTimeline() {
        // Use findAllActive() to get all configured tokens, even if expired
        // This ensures the timeline shows the complete picture
        List<UpstoxTokenEntity> tokens = tokenRepository.findAllActive();
        LocalDateTime now = LocalDateTime.now();

        // Use standard Upstox format as per TokenExpiryCalculator
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return tokens.stream()
                .map(token -> {
                    long minutes = 0;
                    Duration remaining = Duration.ZERO;
                    String status;

                    try {
                        LocalDateTime validityAt = LocalDateTime.parse(token.getValidityAt(), formatter);
                        remaining = Duration.between(now, validityAt);
                        minutes = remaining.toMinutes();

                        if (remaining.isNegative() || remaining.isZero()) {
                            status = "EXPIRED";
                            minutes = 0; // consistent 0 for sorting
                        } else if (minutes < 30) {
                            status = "CRITICAL";
                        } else if (minutes < 60) {
                            status = "WARNING";
                        } else {
                            status = "VALID";
                        }
                    } catch (Exception e) {
                        logger.error("Error parsing validity for {}: {}", token.getApiName(), e.getMessage());
                        status = "ERROR";
                        minutes = -1;
                    }

                    return new TokenTimelineDTO(
                            token.getApiName(),
                            token.getValidityAt(),
                            status,
                            minutes,
                            formatDuration(remaining));
                })
                .sorted(Comparator.comparingLong(TokenTimelineDTO::getRemainingMinutes))
                .collect(Collectors.toList());
    }

    private String formatDuration(Duration duration) {
        if (duration.isNegative() || duration.isZero())
            return "Expired";
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%dh %dm", hours, minutes);
    }

    /**
     * Token Timeline DTO
     */
    public static class TokenTimelineDTO {
        private String apiName;
        private String validityAt;
        private String status;
        private long remainingMinutes;
        private String durationString;

        public TokenTimelineDTO(String apiName, String validityAt, String status, long remainingMinutes,
                String durationString) {
            this.apiName = apiName;
            this.validityAt = validityAt;
            this.status = status;
            this.remainingMinutes = remainingMinutes;
            this.durationString = durationString;
        }

        public String getApiName() {
            return apiName;
        }

        public String getValidityAt() {
            return validityAt;
        }

        public String getStatus() {
            return status;
        }

        public long getRemainingMinutes() {
            return remainingMinutes;
        }

        public String getDurationString() {
            return durationString;
        }
    }
}
