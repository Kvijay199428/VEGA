package com.vegatrader.upstox.api.settings.service;

import com.vegatrader.upstox.api.settings.entity.*;
import com.vegatrader.upstox.api.settings.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * User settings service with validation and auditing.
 * 
 * @since 4.3.0
 */
@Service
public class UserSettingsService {

    private static final Logger logger = LoggerFactory.getLogger(UserSettingsService.class);

    private final UserSettingRepository settingRepo;
    private final SettingsMetadataRepository metadataRepo;
    private final SettingsAuditLogRepository auditRepo;

    public UserSettingsService(
            UserSettingRepository settingRepo,
            SettingsMetadataRepository metadataRepo,
            SettingsAuditLogRepository auditRepo) {
        this.settingRepo = settingRepo;
        this.metadataRepo = metadataRepo;
        this.auditRepo = auditRepo;
    }

    // === Read Operations ===

    public Map<String, String> getAllSettings(String userId) {
        // Get user settings
        Map<String, String> userSettings = settingRepo.findByUserId(userId).stream()
                .collect(Collectors.toMap(
                        UserSettingEntity::getSettingKey,
                        UserSettingEntity::getSettingValue));

        // Merge with defaults
        Map<String, String> result = new HashMap<>();
        for (SettingsMetadataEntity meta : metadataRepo.findAll()) {
            String value = userSettings.getOrDefault(meta.getSettingKey(), meta.getDefaultValue());
            result.put(meta.getSettingKey(), value);
        }

        return result;
    }

    public String getSetting(String userId, String settingKey) {
        return settingRepo.findByUserIdAndSettingKey(userId, settingKey)
                .map(UserSettingEntity::getSettingValue)
                .orElseGet(() -> getDefaultValue(settingKey));
    }

    public boolean getBoolean(String userId, String settingKey, boolean defaultValue) {
        String value = getSetting(userId, settingKey);
        return value != null ? "true".equalsIgnoreCase(value) : defaultValue;
    }

    public int getInt(String userId, String settingKey, int defaultValue) {
        String value = getSetting(userId, settingKey);
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public double getDouble(String userId, String settingKey, double defaultValue) {
        String value = getSetting(userId, settingKey);
        try {
            return value != null ? Double.parseDouble(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // === Write Operations ===

    @Transactional
    public void updateSetting(String userId, String settingKey, String value, String changedBy, String interfaceType) {
        SettingsMetadataEntity meta = metadataRepo.findById(settingKey).orElse(null);
        if (meta == null) {
            throw new IllegalArgumentException("Unknown setting: " + settingKey);
        }

        if (!Boolean.TRUE.equals(meta.getEditable())) {
            throw new IllegalArgumentException("Setting is read-only: " + settingKey);
        }

        // Validate value
        validateValue(meta, value);

        // Get old value for audit
        String oldValue = getSetting(userId, settingKey);

        // Upsert setting
        UserSettingEntity entity = settingRepo.findByUserIdAndSettingKey(userId, settingKey)
                .orElseGet(() -> {
                    UserSettingEntity e = new UserSettingEntity(userId, settingKey, value);
                    e.setScope(meta.getScope());
                    e.setRoleMin(meta.getMinRole());
                    e.setEditable(meta.getEditable());
                    return e;
                });
        entity.setSettingValue(value);
        settingRepo.save(entity);

        // Audit log
        SettingsAuditLogEntity audit = new SettingsAuditLogEntity();
        audit.setUserId(userId);
        audit.setSettingKey(settingKey);
        audit.setOldValue(oldValue);
        audit.setNewValue(value);
        audit.setChangedBy(changedBy);
        audit.setInterfaceType(interfaceType);
        auditRepo.save(audit);

        logger.info("Setting updated: user={}, key={}, value={}", userId, settingKey, value);
    }

    @Transactional
    public void resetToDefault(String userId, String settingKey) {
        settingRepo.deleteByUserIdAndKey(userId, settingKey);
        logger.info("Setting reset to default: user={}, key={}", userId, settingKey);
    }

    // === Validation ===

    private void validateValue(SettingsMetadataEntity meta, String value) {
        switch (meta.getDataType().toUpperCase()) {
            case "BOOLEAN" -> {
                if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                    throw new IllegalArgumentException("Value must be true or false");
                }
            }
            case "INTEGER" -> {
                try {
                    int v = Integer.parseInt(value);
                    if (meta.getMinValue() != null && v < Integer.parseInt(meta.getMinValue())) {
                        throw new IllegalArgumentException("Value below minimum: " + meta.getMinValue());
                    }
                    if (meta.getMaxValue() != null && v > Integer.parseInt(meta.getMaxValue())) {
                        throw new IllegalArgumentException("Value above maximum: " + meta.getMaxValue());
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Value must be an integer");
                }
            }
            case "DECIMAL" -> {
                try {
                    double v = Double.parseDouble(value);
                    if (meta.getMinValue() != null && v < Double.parseDouble(meta.getMinValue())) {
                        throw new IllegalArgumentException("Value below minimum: " + meta.getMinValue());
                    }
                    if (meta.getMaxValue() != null && v > Double.parseDouble(meta.getMaxValue())) {
                        throw new IllegalArgumentException("Value above maximum: " + meta.getMaxValue());
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Value must be a decimal number");
                }
            }
            case "ENUM" -> {
                if (meta.getAllowedValues() != null && !meta.getAllowedValues().contains(value)) {
                    throw new IllegalArgumentException("Invalid value. Allowed: " + meta.getAllowedValues());
                }
            }
        }
    }

    private String getDefaultValue(String settingKey) {
        return metadataRepo.findById(settingKey)
                .map(SettingsMetadataEntity::getDefaultValue)
                .orElse(null);
    }

    // === Metadata Queries ===

    public List<SettingsMetadataEntity> getSettingsMetadata() {
        return metadataRepo.findByEditableTrueOrderByDisplayOrderAsc();
    }

    public List<SettingsMetadataEntity> getSettingsMetadataByCategory(String category) {
        return metadataRepo.findByCategoryOrderByDisplayOrderAsc(category);
    }
}
