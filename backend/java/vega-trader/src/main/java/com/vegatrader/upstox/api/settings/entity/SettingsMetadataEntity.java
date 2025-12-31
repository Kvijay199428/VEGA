package com.vegatrader.upstox.api.settings.entity;

import jakarta.persistence.*;

/**
 * JPA Entity for settings_metadata table.
 * Defines setting schema with validation rules.
 * 
 * @since 4.3.0
 */
@Entity
@Table(name = "settings_metadata")
public class SettingsMetadataEntity {

    @Id
    @Column(name = "setting_key", length = 128)
    private String settingKey;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "description")
    private String description;

    @Column(name = "data_type", nullable = false, length = 16)
    private String dataType; // BOOLEAN / INTEGER / DECIMAL / STRING / ENUM / LIST

    @Column(name = "category", nullable = false, length = 32)
    private String category; // INSTRUMENT / ORDER / RISK / BROKER / LOGGING

    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "min_value")
    private String minValue;

    @Column(name = "max_value")
    private String maxValue;

    @Column(name = "allowed_values")
    private String allowedValues; // JSON array

    @Column(name = "min_role", nullable = false, length = 16)
    private String minRole = "TRADER";

    @Column(name = "editable", nullable = false)
    private Boolean editable = true;

    @Column(name = "scope", nullable = false, length = 16)
    private String scope = "GLOBAL";

    @Column(name = "display_order")
    private Integer displayOrder = 100;

    // --- Getters and Setters ---

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getMinValue() {
        return minValue;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    public String getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(String allowedValues) {
        this.allowedValues = allowedValues;
    }

    public String getMinRole() {
        return minRole;
    }

    public void setMinRole(String minRole) {
        this.minRole = minRole;
    }

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    // --- Utility Methods ---

    public boolean isBoolean() {
        return "BOOLEAN".equalsIgnoreCase(dataType);
    }

    public boolean isInteger() {
        return "INTEGER".equalsIgnoreCase(dataType);
    }

    public boolean isDecimal() {
        return "DECIMAL".equalsIgnoreCase(dataType);
    }

    public boolean isEnum() {
        return "ENUM".equalsIgnoreCase(dataType);
    }

    public boolean isList() {
        return "LIST".equalsIgnoreCase(dataType);
    }
}
