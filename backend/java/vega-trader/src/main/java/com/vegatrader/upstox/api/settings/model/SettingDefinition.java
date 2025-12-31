package com.vegatrader.upstox.api.settings.model;

/**
 * Settings definition - schema for a single setting.
 * Per IMPLEMENTATION_ROADMAP.md section 3.1.
 * 
 * @since 4.8.0
 */
public record SettingDefinition(
        String key,
        SettingScope scope,
        DataType dataType,
        String defaultValue,
        Double minValue,
        Double maxValue,
        String allowedValues, // JSON array
        boolean locked,
        boolean deprecated,
        String category,
        String description) {

    /**
     * Check if user can modify this setting.
     */
    public boolean isUserEditable() {
        return scope == SettingScope.USER && !locked;
    }

    /**
     * Check if admin can modify this setting.
     */
    public boolean isAdminEditable() {
        return (scope == SettingScope.ADMIN || scope == SettingScope.USER) && !locked;
    }

    /**
     * Validate a value against constraints.
     */
    public ValidationResult validate(String value) {
        if (locked) {
            return ValidationResult.error("Setting is system-locked");
        }

        if (deprecated) {
            return ValidationResult.warning("Setting is deprecated");
        }

        return switch (dataType) {
            case INTEGER -> validateInteger(value);
            case DECIMAL -> validateDecimal(value);
            case BOOLEAN -> validateBoolean(value);
            case STRING -> ValidationResult.ok();
            case ENUM -> validateEnum(value);
            case JSON_ARRAY -> validateJsonArray(value);
        };
    }

    private ValidationResult validateInteger(String value) {
        try {
            int v = Integer.parseInt(value);
            if (minValue != null && v < minValue) {
                return ValidationResult.error("Value below minimum: " + minValue);
            }
            if (maxValue != null && v > maxValue) {
                return ValidationResult.error("Value above maximum: " + maxValue);
            }
            return ValidationResult.ok();
        } catch (NumberFormatException e) {
            return ValidationResult.error("Invalid integer: " + value);
        }
    }

    private ValidationResult validateDecimal(String value) {
        try {
            double v = Double.parseDouble(value);
            if (minValue != null && v < minValue) {
                return ValidationResult.error("Value below minimum: " + minValue);
            }
            if (maxValue != null && v > maxValue) {
                return ValidationResult.error("Value above maximum: " + maxValue);
            }
            return ValidationResult.ok();
        } catch (NumberFormatException e) {
            return ValidationResult.error("Invalid decimal: " + value);
        }
    }

    private ValidationResult validateBoolean(String value) {
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return ValidationResult.ok();
        }
        return ValidationResult.error("Invalid boolean: " + value);
    }

    private ValidationResult validateEnum(String value) {
        if (allowedValues == null || allowedValues.isEmpty()) {
            return ValidationResult.ok();
        }
        if (allowedValues.contains("\"" + value + "\"")) {
            return ValidationResult.ok();
        }
        return ValidationResult.error("Value not in allowed list: " + allowedValues);
    }

    private ValidationResult validateJsonArray(String value) {
        if (value == null || (!value.startsWith("[") && !value.endsWith("]"))) {
            return ValidationResult.error("Invalid JSON array");
        }
        return ValidationResult.ok();
    }

    /**
     * Setting scope.
     */
    public enum SettingScope {
        SYSTEM, // Code/deployment only
        ADMIN, // Admin/Ops only
        USER // End user editable
    }

    /**
     * Data type.
     */
    public enum DataType {
        INTEGER, DECIMAL, BOOLEAN, STRING, ENUM, JSON_ARRAY
    }

    /**
     * Validation result.
     */
    public record ValidationResult(
            boolean valid,
            boolean warning,
            String message) {
        public static ValidationResult ok() {
            return new ValidationResult(true, false, null);
        }

        public static ValidationResult warning(String msg) {
            return new ValidationResult(true, true, msg);
        }

        public static ValidationResult error(String msg) {
            return new ValidationResult(false, false, msg);
        }
    }
}
