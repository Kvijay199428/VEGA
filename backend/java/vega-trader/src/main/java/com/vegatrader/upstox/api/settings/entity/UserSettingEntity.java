package com.vegatrader.upstox.api.settings.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity for user_settings table.
 * 
 * @since 4.3.0
 */
@Entity
@Table(name = "user_settings")
@IdClass(UserSettingId.class)
public class UserSettingEntity {

    @Id
    @Column(name = "user_id", length = 64)
    private String userId;

    @Id
    @Column(name = "setting_key", length = 128)
    private String settingKey;

    @Column(name = "setting_value", nullable = false)
    private String settingValue;

    @Column(name = "scope", nullable = false, length = 16)
    private String scope = "GLOBAL";

    @Column(name = "editable", nullable = false)
    private Boolean editable = true;

    @Column(name = "role_min", nullable = false, length = 16)
    private String roleMin = "TRADER";

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    public UserSettingEntity() {
        this.lastUpdated = LocalDateTime.now();
    }

    public UserSettingEntity(String userId, String settingKey, String settingValue) {
        this();
        this.userId = userId;
        this.settingKey = settingKey;
        this.settingValue = settingValue;
    }

    // --- Getters and Setters ---

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public String getRoleMin() {
        return roleMin;
    }

    public void setRoleMin(String roleMin) {
        this.roleMin = roleMin;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // --- Utility Methods ---

    public boolean isBoolean() {
        return "true".equalsIgnoreCase(settingValue) || "false".equalsIgnoreCase(settingValue);
    }

    public boolean asBoolean() {
        return "true".equalsIgnoreCase(settingValue);
    }

    public int asInt(int defaultValue) {
        try {
            return Integer.parseInt(settingValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public double asDouble(double defaultValue) {
        try {
            return Double.parseDouble(settingValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}
