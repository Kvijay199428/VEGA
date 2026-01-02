package com.vegatrader.upstox.auth.service;

import com.vegatrader.upstox.auth.dto.AuthStatus;
import com.vegatrader.upstox.auth.websocket.AuthStatusWebSocketHandler;
import com.vegatrader.util.time.TimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // Maintain current status
    private AuthStatus currentStatus;

    // To avoid duplicate scheduling
    private final AtomicBoolean isSchedulerRunning = new AtomicBoolean(false);

    private static final int TOKEN_DURATION_SEC = 86400; // 24 hours (example) - set by actual logic

    public AuthService(AuthStatusWebSocketHandler wsHandler, TimeProvider timeProvider) {
        this.wsHandler = wsHandler;
        this.timeProvider = timeProvider;

        // Initial state
        this.currentStatus = AuthStatus.builder()
                .state("UNAUTHENTICATED")
                .authenticated(false)
                .primaryReady(false)
                .fullyReady(false)
                .generatedTokens(0)
                .requiredTokens(0)
                .validTokens(Collections.emptyList())
                .inProgress(false)
                .cooldownActive(false)
                .remainingSeconds(0)
                .expiresAt(0)
                .build();

        startTokenExpiryScheduler();

        // Schedule Heartbeat
        scheduler.scheduleAtFixedRate(this::heartbeat, 0, 10, TimeUnit.SECONDS);
    }

    public void updateLoginSuccess(List<String> validTokens, long expiresAtEpochSecond) {
        long now = timeProvider.now().getEpochSecond();
        int remaining = (int) Math.max(0, expiresAtEpochSecond - now);

        currentStatus.setState("AUTH_CONFIRMED");
        currentStatus.setAuthenticated(true);
        currentStatus.setFullyReady(true); // Simplified for prototype
        currentStatus.setPrimaryReady(true);
        currentStatus.setValidTokens(validTokens);
        currentStatus.setGeneratedTokens(validTokens.size());
        currentStatus.setRequiredTokens(validTokens.size());
        currentStatus.setExpiresAt(expiresAtEpochSecond);
        currentStatus.setRemainingSeconds(remaining);

        logger.info("Auth Success updated. Tokens: {}, Expires in: {}s", validTokens.size(), remaining);
        wsHandler.broadcastStatus(currentStatus);
    }

    public void updateState(String state) {
        currentStatus.setState(state);
        wsHandler.broadcastStatus(currentStatus);
    }

    private void startTokenExpiryScheduler() {
        if (isSchedulerRunning.getAndSet(true)) {
            return;
        }

        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (!currentStatus.isAuthenticated()) {
                    // Still broadcast "UNAUTHENTICATED" or similar occasionally
                    // but mainly we care about countdown when authenticated
                    return;
                }

                long now = timeProvider.now().getEpochSecond();
                long remaining = currentStatus.getExpiresAt() - now;
                currentStatus.setRemainingSeconds((int) Math.max(0, remaining));

                if (remaining <= 0) {
                    // Token expired
                    if (!"EXPIRED".equals(currentStatus.getState())) {
                        logger.warn("Tokens expired!");
                        currentStatus.setState("EXPIRED");
                        currentStatus.setAuthenticated(false);
                        currentStatus.setPrimaryReady(false);
                        currentStatus.setFullyReady(false);
                        wsHandler.broadcastStatus(currentStatus);
                        // Trigger auto-refresh logic here if integrated
                    }
                } else {
                    // Broadcast countdown
                    wsHandler.broadcastStatus(currentStatus);
                }
            } catch (Exception e) {
                logger.error("Error in auth scheduler", e);
            }
        }, 0, 1, TimeUnit.SECONDS);

    }

    // HARD FIX #3: Heartbeat (Prevents Silent Disconnects)
    // Runs every 10 seconds to keep the connection alive
    public void heartbeat() {
        if (currentStatus != null) {
            wsHandler.broadcastStatus(currentStatus);
        }
    }
}
