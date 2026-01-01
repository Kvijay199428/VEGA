package com.vegatrader.upstox.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * System Controller - Provides system health and summary endpoints.
 * Used by Dashboard and Account pages.
 */
@RestController
@RequestMapping("/api/system")
public class SystemController {

    private static final Logger logger = LoggerFactory.getLogger(SystemController.class);

    @Autowired
    private com.vegatrader.upstox.auth.state.AuthSessionState authSessionState;

    @Autowired
    private com.vegatrader.upstox.auth.state.OperatorControlState operatorControlState;

    /**
     * GET /api/system/summary
     * Returns application summary for dashboard header.
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSystemSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("appName", "VEGA TRADER");
        summary.put("version", "1.0.0");
        summary.put("account", "UPSTOX_PRO");
        summary.put("liveStatus", authSessionState.isPrimaryReady() ? "LIVE" : "DEGRADED");
        summary.put("serverTime", LocalDateTime.now(ZoneId.of("Asia/Kolkata")).toString());
        summary.put("automationEnabled", operatorControlState.isAutomationEnabled());
        return ResponseEntity.ok(summary);
    }

    /**
     * GET /api/system/health
     * Returns health status of all system components.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();

        // Database Health
        Map<String, Object> db = new HashMap<>();
        db.put("status", "OK");
        db.put("message", "SQLite operational");
        health.put("database", db);

        // Cache Health
        Map<String, Object> cache = new HashMap<>();
        cache.put("status", "OK");
        cache.put("message", "In-memory cache active");
        health.put("cache", cache);

        // API Rate Limit
        Map<String, Object> api = new HashMap<>();
        api.put("status", operatorControlState.isAutomationEnabled() ? "SAFE" : "LIMITED");
        api.put("message", operatorControlState.isAutomationEnabled() ? "Rate limits normal" : "Automation paused");
        health.put("api", api);

        // WebSocket Connections
        Map<String, Object> ws = new HashMap<>();
        ws.put("status", authSessionState.isPrimaryReady() ? "CONNECTED" : "DISCONNECTED");
        ws.put("connections", authSessionState.getGeneratedCount());
        health.put("websocket", ws);

        // Auth Status
        Map<String, Object> auth = new HashMap<>();
        auth.put("primaryReady", authSessionState.isPrimaryReady());
        auth.put("fullyReady", authSessionState.isFullyReady());
        auth.put("tokensGenerated", authSessionState.getGeneratedCount());
        auth.put("tokensRequired", authSessionState.getRequiredCount());
        health.put("auth", auth);

        return ResponseEntity.ok(health);
    }
}
