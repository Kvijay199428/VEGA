package com.vegatrader.upstox.api.settings.service;

import com.vegatrader.upstox.api.settings.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Admin settings service with validation and auditing.
 * Per IMPLEMENTATION_ROADMAP.md section 3.1.
 * 
 * @since 4.8.0
 */
@Service
public class AdminSettingsService {

    private static final Logger logger = LoggerFactory.getLogger(AdminSettingsService.class);

    // In-memory caches (production would use DB)
    private final Map<String, SettingDefinition> definitions = new ConcurrentHashMap<>();
    private final Map<String, AdminSetting> adminSettings = new ConcurrentHashMap<>();
    private final List<SettingsAuditEntry> auditLog = Collections.synchronizedList(new ArrayList<>());

    public AdminSettingsService() {
        initializeDefaults();
    }

    /**
     * Get all admin settings.
     */
    public List<AdminSetting> getAllSettings() {
        return new ArrayList<>(adminSettings.values());
    }

    /**
     * Get admin setting by key.
     */
    public Optional<AdminSetting> getSetting(String key) {
        return Optional.ofNullable(adminSettings.get(key));
    }

    /**
     * Get setting value (returns default if not set).
     */
    public String getSettingValue(String key) {
        AdminSetting setting = adminSettings.get(key);
        if (setting != null && setting.isActive()) {
            return setting.value();
        }

        SettingDefinition def = definitions.get(key);
        return def != null ? def.defaultValue() : null;
    }

    /**
     * Get setting as integer.
     */
    public int getInt(String key, int defaultValue) {
        try {
            String value = getSettingValue(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Get setting as boolean.
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getSettingValue(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    /**
     * Update admin setting.
     */
    public SettingsUpdateResult updateSetting(
            String key,
            String newValue,
            String updatedBy,
            String reasonCode,
            String comment) {

        // Get definition
        SettingDefinition def = definitions.get(key);
        if (def == null) {
            return SettingsUpdateResult.error("Unknown setting key: " + key);
        }

        // Check permission
        if (!def.isAdminEditable()) {
            return SettingsUpdateResult.error("Setting is not admin-editable: " + key);
        }

        // Validate value
        var validation = def.validate(newValue);
        if (!validation.valid()) {
            return SettingsUpdateResult.error(validation.message());
        }

        // Get old value
        String oldValue = getSettingValue(key);

        // Create new setting
        AdminSetting newSetting = AdminSetting.builder()
                .key(key)
                .value(newValue)
                .updatedBy(updatedBy)
                .reasonCode(reasonCode)
                .reasonComment(comment)
                .build();

        // Update cache
        adminSettings.put(key, newSetting);

        // Audit log
        auditLog.add(new SettingsAuditEntry(
                key,
                oldValue,
                newValue,
                updatedBy,
                reasonCode,
                comment,
                Instant.now()));

        logger.info("Admin setting updated: {} = {} (by {})", key, newValue, updatedBy);

        return SettingsUpdateResult.success(oldValue, newValue);
    }

    /**
     * Get audit log.
     */
    public List<SettingsAuditEntry> getAuditLog(int limit) {
        int start = Math.max(0, auditLog.size() - limit);
        return new ArrayList<>(auditLog.subList(start, auditLog.size()));
    }

    /**
     * Get audit log for specific key.
     */
    public List<SettingsAuditEntry> getAuditLogForKey(String key) {
        return auditLog.stream()
                .filter(e -> e.key().equals(key))
                .toList();
    }

    /**
     * Check if kill switch is enabled.
     */
    public boolean isKillSwitchEnabled() {
        return getBoolean("rms.killSwitch.enabled", false);
    }

    /**
     * Enable kill switch (emergency).
     */
    public void enableKillSwitch(String reason, String updatedBy) {
        updateSetting("rms.killSwitch.enabled", "true", updatedBy, "EMERGENCY", reason);
        updateSetting("rms.killSwitch.reason", reason, updatedBy, "EMERGENCY", "Kill switch enabled");
        logger.warn("KILL SWITCH ENABLED by {} - Reason: {}", updatedBy, reason);
    }

    /**
     * Disable kill switch.
     */
    public void disableKillSwitch(String updatedBy, String reason) {
        updateSetting("rms.killSwitch.enabled", "false", updatedBy, "RECOVERY", reason);
        updateSetting("rms.killSwitch.reason", "", updatedBy, "RECOVERY", "Kill switch disabled");
        logger.info("Kill switch disabled by {} - Reason: {}", updatedBy, reason);
    }

    /**
     * Get all definitions.
     */
    public List<SettingDefinition> getAllDefinitions() {
        return new ArrayList<>(definitions.values());
    }

    /**
     * Get definitions by category.
     */
    public List<SettingDefinition> getDefinitionsByCategory(String category) {
        return definitions.values().stream()
                .filter(d -> category.equals(d.category()))
                .toList();
    }

    /**
     * Initialize default definitions.
     */
    private void initializeDefaults() {
        // Trading controls
        addDefinition("trading.maxOrderQty", SettingDefinition.SettingScope.ADMIN,
                SettingDefinition.DataType.INTEGER, "1800", 1.0, 5000.0, "TRADING", "Maximum order quantity");
        addDefinition("trading.maxNotionalValue", SettingDefinition.SettingScope.ADMIN,
                SettingDefinition.DataType.DECIMAL, "10000000", 100000.0, 100000000.0, "TRADING", "Max notional value");

        // Option chain
        addDefinition("options.maxStrikesPerSide", SettingDefinition.SettingScope.ADMIN,
                SettingDefinition.DataType.INTEGER, "20", 5.0, 50.0, "OPTION_CHAIN", "Max strikes per side");

        // RMS
        addDefinition("rms.killSwitch.enabled", SettingDefinition.SettingScope.ADMIN,
                SettingDefinition.DataType.BOOLEAN, "false", null, null, "RMS", "Global kill switch");
        addDefinition("rms.killSwitch.reason", SettingDefinition.SettingScope.ADMIN,
                SettingDefinition.DataType.STRING, "", null, null, "RMS", "Kill switch reason");
        addDefinition("rms.marginBufferPct", SettingDefinition.SettingScope.ADMIN,
                SettingDefinition.DataType.DECIMAL, "5", 0.0, 50.0, "RMS", "Margin buffer percentage");

        // WebSocket
        addDefinition("ws.deltaOnly", SettingDefinition.SettingScope.ADMIN,
                SettingDefinition.DataType.BOOLEAN, "true", null, null, "WEBSOCKET", "Delta-only updates");
        addDefinition("ws.binaryEncoding", SettingDefinition.SettingScope.ADMIN,
                SettingDefinition.DataType.BOOLEAN, "true", null, null, "WEBSOCKET", "Binary encoding");

        // Initialize admin settings with defaults
        for (SettingDefinition def : definitions.values()) {
            if (def.scope() == SettingDefinition.SettingScope.ADMIN) {
                adminSettings.put(def.key(), AdminSetting.builder()
                        .key(def.key())
                        .value(def.defaultValue())
                        .updatedBy("SYSTEM")
                        .reasonCode("INITIAL_SETUP")
                        .build());
            }
        }
    }

    private void addDefinition(String key, SettingDefinition.SettingScope scope,
            SettingDefinition.DataType dataType, String defaultValue,
            Double min, Double max, String category, String description) {
        definitions.put(key, new SettingDefinition(
                key, scope, dataType, defaultValue, min, max, null, false, false, category, description));
    }

    /**
     * Update result.
     */
    public record SettingsUpdateResult(
            boolean success,
            String oldValue,
            String newValue,
            String error) {
        public static SettingsUpdateResult success(String oldValue, String newValue) {
            return new SettingsUpdateResult(true, oldValue, newValue, null);
        }

        public static SettingsUpdateResult error(String error) {
            return new SettingsUpdateResult(false, null, null, error);
        }
    }

    /**
     * Audit entry.
     */
    public record SettingsAuditEntry(
            String key,
            String oldValue,
            String newValue,
            String updatedBy,
            String reasonCode,
            String comment,
            Instant timestamp) {
    }
}
