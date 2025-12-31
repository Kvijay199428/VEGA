package com.vegatrader.upstox.api.settings.model;

import java.time.Instant;

/**
 * Admin setting value with audit metadata.
 * Per IMPLEMENTATION_ROADMAP.md section 3.1.
 * 
 * @since 4.8.0
 */
public record AdminSetting(
        Long id,
        String key,
        String value,
        String tenantId,
        Instant effectiveFrom,
        Instant effectiveUntil,
        String updatedBy,
        Instant updatedAt,
        String reasonCode,
        String reasonComment) {

    /**
     * Check if setting is currently active.
     */
    public boolean isActive() {
        Instant now = Instant.now();
        boolean afterStart = effectiveFrom == null || !now.isBefore(effectiveFrom);
        boolean beforeEnd = effectiveUntil == null || now.isBefore(effectiveUntil);
        return afterStart && beforeEnd;
    }

    /**
     * Check if setting is scheduled (future effective date).
     */
    public boolean isScheduled() {
        return effectiveFrom != null && Instant.now().isBefore(effectiveFrom);
    }

    /**
     * Create new setting with updated value.
     */
    public AdminSetting withValue(String newValue, String updatedBy, String reasonCode, String comment) {
        return new AdminSetting(
                null,
                key,
                newValue,
                tenantId,
                Instant.now(),
                null,
                updatedBy,
                Instant.now(),
                reasonCode,
                comment);
    }

    /**
     * Builder for admin setting.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String key;
        private String value;
        private String tenantId = "GLOBAL";
        private Instant effectiveFrom;
        private String updatedBy;
        private String reasonCode;
        private String reasonComment;

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder effectiveFrom(Instant effectiveFrom) {
            this.effectiveFrom = effectiveFrom;
            return this;
        }

        public Builder updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public Builder reasonCode(String reasonCode) {
            this.reasonCode = reasonCode;
            return this;
        }

        public Builder reasonComment(String reasonComment) {
            this.reasonComment = reasonComment;
            return this;
        }

        public AdminSetting build() {
            return new AdminSetting(
                    null,
                    key,
                    value,
                    tenantId,
                    effectiveFrom != null ? effectiveFrom : Instant.now(),
                    null,
                    updatedBy,
                    Instant.now(),
                    reasonCode,
                    reasonComment);
        }
    }
}
