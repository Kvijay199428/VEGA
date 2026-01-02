package com.vegatrader.upstox.auth.service;

import com.vegatrader.upstox.auth.dto.AuthStatus;
import com.vegatrader.upstox.auth.websocket.AuthStatusWebSocketHandler;
import com.vegatrader.util.time.TimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final AuthStatusWebSocketHandler wsHandler;
    private final TimeProvider timeProvider;
    private final TokenStorageService tokenStorageService;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // Maintain current status
    private AuthStatus currentStatus;

    // To avoid duplicate scheduling
    private final AtomicBoolean isSchedulerRunning = new AtomicBoolean(false);

    private static final int TOKEN_DURATION_SEC = 86400; // 24 hours (example) - set by actual logic

    public AuthService(AuthStatusWebSocketHandler wsHandler, TimeProvider timeProvider,
            TokenStorageService tokenStorageService) {
        this.wsHandler = wsHandler;
        this.timeProvider = timeProvider;
        this.tokenStorageService = tokenStorageService;

        // Initialize with LOADING state (will be synced in @PostConstruct)
        this.currentStatus = AuthStatus.builder()
                .state("INITIALIZING")
                .authenticated(false)
                .primaryReady(false)
                .fullyReady(false)
                .generatedTokens(0)
                .requiredTokens(6) // Default
                .validTokens(Collections.emptyList())
                .inProgress(false)
                .cooldownActive(false)
                .remainingSeconds(0)
                .expiresAt(0)
                .build();
    }

    /**
     * Sync initial auth state from database after Spring context is ready.
     * This ensures WebSocket broadcasts correct state using Delta events.
     */
    @PostConstruct
    public void syncInitialStateFromDatabase() {
        try {
            List<String> validTokens = tokenStorageService.getValidApiNames();
            int tokenCount = tokenStorageService.getActiveTokenCount();
            boolean hasPrimary = validTokens.stream().anyMatch(name -> name.equalsIgnoreCase("PRIMARY"));

            logger.info("╔══════════════════════════════════════════════════════════╗");
            logger.info("║  SYNCING INITIAL AUTH STATE FROM DATABASE                ║");
            logger.info("╠══════════════════════════════════════════════════════════╣");
            logger.info("║  Active Tokens: {}                                       ║", tokenCount);
            logger.info("║  Valid APIs: {}                                          ║", validTokens);
            logger.info("║  Primary Ready: {}                                       ║", hasPrimary);
            logger.info("╚══════════════════════════════════════════════════════════╝");

            if (tokenCount > 0 && hasPrimary) {
                // Tokens exist in DB - mark as authenticated
                currentStatus.setState("PRIMARY_VALIDATED");
                currentStatus.setAuthenticated(true);
                currentStatus.setPrimaryReady(true);
                currentStatus.setFullyReady(tokenCount >= 6);
                currentStatus.setValidTokens(validTokens);
                currentStatus.setGeneratedTokens(tokenCount);
                currentStatus.setRequiredTokens(6);

                long expiresAt = calculateTokenExpiry();
                long remaining = expiresAt - timeProvider.now().getEpochSecond();
                currentStatus.setExpiresAt(expiresAt);
                currentStatus.setRemainingSeconds((int) Math.max(0, remaining));

                logger.info("✓ Auth state synced: AUTHENTICATED with {} tokens", tokenCount);

                // Broadcast DELTA events for initial state
                if (validTokens != null) {
                    for (String token : validTokens) {
                        wsHandler.broadcastEvent("TOKEN_READY", java.util.Map.of("api", token));
                    }
                }

                wsHandler.broadcastEvent("TOKEN_PROGRESS", java.util.Map.of(
                        "ready", validTokens.size(),
                        "total", 6));

            } else {
                currentStatus.setState("UNAUTHENTICATED");
                currentStatus.setAuthenticated(false);
                logger.info("✗ No active tokens found - state: UNAUTHENTICATED");
                // No event emitted for unauthenticated state - client relies on HTTP bootstrap
            }

        } catch (Exception e) {
            logger.error("Failed to sync initial auth state from database", e);
            currentStatus.setState("UNAUTHENTICATED");
            currentStatus.setAuthenticated(false);
        }

        startTokenExpiryScheduler();

        // Schedule Heartbeat
        scheduler.scheduleAtFixedRate(this::heartbeat, 10, 10, TimeUnit.SECONDS);
    }

    /**
     * Calculate token expiry (3:30 AM next day IST).
     */
    private long calculateTokenExpiry() {
        // Simple calculation: tokens expire at 3:30 AM IST next day
        // For now, return current time + 14 hours as approximation
        return timeProvider.now().getEpochSecond() + (14 * 3600);
    }

    public void updateLoginSuccess(List<String> validTokens, long expiresAtEpochSecond) {
        long now = timeProvider.now().getEpochSecond();
        int remaining = (int) Math.max(0, expiresAtEpochSecond - now);

        currentStatus.setState("AUTH_CONFIRMED");
        currentStatus.setAuthenticated(true);
        currentStatus.setFullyReady(true);
        currentStatus.setPrimaryReady(true);
        currentStatus.setValidTokens(validTokens);
        currentStatus.setGeneratedTokens(validTokens.size());
        currentStatus.setRequiredTokens(validTokens.size());
        currentStatus.setExpiresAt(expiresAtEpochSecond);
        currentStatus.setRemainingSeconds(remaining);

        logger.info("Auth Success updated. Tokens: {}, Expires in: {}s", validTokens.size(), remaining);

        // Emit TOKEN_READY for each token
        for (String token : validTokens) {
            wsHandler.broadcastEvent("TOKEN_READY", java.util.Map.of(
                    "api", token,
                    "expiresAt", expiresAtEpochSecond));
        }

        // Emit overall progress
        wsHandler.broadcastEvent("TOKEN_PROGRESS", java.util.Map.of(
                "ready", validTokens.size(),
                "total", currentStatus.getRequiredTokens()));
    }

    public void updateState(String state) {
        currentStatus.setState(state);
        // Only emit if critical state change
        if ("EXPIRED".equals(state) || "ERROR".equals(state)) {
            wsHandler.broadcastEvent("SESSION_INVALIDATED", java.util.Map.of(
                    "reason", state));
        }
    }

    /**
     * Notify frontend of token update (e.g., after retry).
     */
    public void notifyTokenUpdate() {
        logger.info("Token update notification triggered");
        // Broadcast all current tokens as ready (idempotent on frontend)
        List<String> validTokens = currentStatus.getValidTokens();
        if (validTokens != null) {
            for (String token : validTokens) {
                wsHandler.broadcastEvent("TOKEN_READY", java.util.Map.of("api", token));
            }
        }
    }

    private void startTokenExpiryScheduler() {
        if (isSchedulerRunning.getAndSet(true)) {
            return;
        }

        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (!currentStatus.isAuthenticated())
                    return;

                long now = timeProvider.now().getEpochSecond();
                long remaining = currentStatus.getExpiresAt() - now;
                currentStatus.setRemainingSeconds((int) Math.max(0, remaining));

                if (remaining <= 0) {
                    if (!"EXPIRED".equals(currentStatus.getState())) {
                        logger.warn("Tokens expired!");
                        currentStatus.setState("EXPIRED");
                        currentStatus.setAuthenticated(false);

                        wsHandler.broadcastEvent("SESSION_INVALIDATED", java.util.Map.of("reason", "EXPIRED"));
                    }
                }
                // No countdown broadcast needed - client handles countdown based on expiry time
            } catch (Exception e) {
                logger.error("Error in auth scheduler", e);
            }
        }, 0, 1, TimeUnit.SECONDS);

    }

    // Heartbeat to keep connection alive and sync uptime
    public void heartbeat() {
        wsHandler.broadcastEvent("HEARTBEAT", java.util.Map.of(
                "uptime", System.currentTimeMillis()));
    }
}
