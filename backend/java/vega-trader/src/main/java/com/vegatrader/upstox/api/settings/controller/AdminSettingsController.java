package com.vegatrader.upstox.api.settings.controller;

import com.vegatrader.upstox.api.settings.model.*;
import com.vegatrader.upstox.api.settings.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Admin settings REST controller.
 * Per IMPLEMENTATION_ROADMAP.md section 3.1.
 * 
 * @since 4.8.0
 */
@RestController
@RequestMapping("/admin/settings")
public class AdminSettingsController {

    private final AdminSettingsService adminSettingsService;

    public AdminSettingsController(AdminSettingsService adminSettingsService) {
        this.adminSettingsService = adminSettingsService;
    }

    /**
     * GET /admin/settings - List all admin settings.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSettings(
            @RequestParam(required = false) String category) {

        List<AdminSetting> settings = adminSettingsService.getAllSettings();
        List<SettingDefinition> definitions = adminSettingsService.getAllDefinitions();

        if (category != null) {
            definitions = adminSettingsService.getDefinitionsByCategory(category);
            Set<String> keys = new HashSet<>();
            for (SettingDefinition d : definitions) {
                keys.add(d.key());
            }
            settings = settings.stream()
                    .filter(s -> keys.contains(s.key()))
                    .toList();
        }

        return ResponseEntity.ok(Map.of(
                "scope", "ADMIN_GLOBAL",
                "count", settings.size(),
                "settings", settings,
                "definitions", definitions));
    }

    /**
     * GET /admin/settings/{key} - Get single setting.
     */
    @GetMapping("/{key}")
    public ResponseEntity<?> getSetting(@PathVariable String key) {
        return adminSettingsService.getSetting(key)
                .map(s -> ResponseEntity.ok(Map.of(
                        "setting", s,
                        "value", s.value(),
                        "active", s.isActive())))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /admin/settings/update - Update setting.
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateSetting(
            @RequestBody SettingUpdateRequest request,
            @RequestHeader(value = "X-Admin-User", defaultValue = "admin") String adminUser) {

        // Validate required fields
        if (request.key() == null || request.value() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "key and value are required"));
        }

        if (request.reasonCode() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "reasonCode is required"));
        }

        var result = adminSettingsService.updateSetting(
                request.key(),
                request.value(),
                adminUser,
                request.reasonCode(),
                request.comment());

        if (result.success()) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "key", request.key(),
                    "oldValue", result.oldValue() != null ? result.oldValue() : "",
                    "newValue", result.newValue()));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", result.error()));
        }
    }

    /**
     * GET /admin/settings/history - Get audit log.
     */
    @GetMapping("/history")
    public ResponseEntity<List<AdminSettingsService.SettingsAuditEntry>> getHistory(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String key) {

        if (key != null) {
            return ResponseEntity.ok(adminSettingsService.getAuditLogForKey(key));
        }
        return ResponseEntity.ok(adminSettingsService.getAuditLog(limit));
    }

    /**
     * POST /admin/settings/kill-switch/enable - Enable kill switch.
     */
    @PostMapping("/kill-switch/enable")
    public ResponseEntity<Map<String, Object>> enableKillSwitch(
            @RequestBody KillSwitchRequest request,
            @RequestHeader(value = "X-Admin-User", defaultValue = "admin") String adminUser) {

        if (request.reason() == null || request.reason().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "reason is required"));
        }

        adminSettingsService.enableKillSwitch(request.reason(), adminUser);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "killSwitchEnabled", true,
                "reason", request.reason(),
                "enabledBy", adminUser));
    }

    /**
     * POST /admin/settings/kill-switch/disable - Disable kill switch.
     */
    @PostMapping("/kill-switch/disable")
    public ResponseEntity<Map<String, Object>> disableKillSwitch(
            @RequestBody KillSwitchRequest request,
            @RequestHeader(value = "X-Admin-User", defaultValue = "admin") String adminUser) {

        adminSettingsService.disableKillSwitch(adminUser, request.reason());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "killSwitchEnabled", false,
                "disabledBy", adminUser));
    }

    /**
     * GET /admin/settings/kill-switch/status - Get kill switch status.
     */
    @GetMapping("/kill-switch/status")
    public ResponseEntity<Map<String, Object>> getKillSwitchStatus() {
        boolean enabled = adminSettingsService.isKillSwitchEnabled();
        String reason = adminSettingsService.getSettingValue("rms.killSwitch.reason");

        return ResponseEntity.ok(Map.of(
                "enabled", enabled,
                "reason", reason != null ? reason : ""));
    }

    /**
     * Request DTOs.
     */
    public record SettingUpdateRequest(
            String key,
            String value,
            String effectiveFrom,
            String reasonCode,
            String comment) {
    }

    public record KillSwitchRequest(
            String reason) {
    }
}
