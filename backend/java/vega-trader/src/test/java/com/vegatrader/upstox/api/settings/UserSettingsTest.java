package com.vegatrader.upstox.api.settings;

import com.vegatrader.upstox.api.settings.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User Settings module.
 * Tests entities, validation, and role enforcement.
 * 
 * Per testing-guide/a1.md sections 7, 9
 */
class UserSettingsTest {

    // === 7.1 Settings Boundary Tests ===

    @Test
    @DisplayName("UserSettingEntity: type conversion - boolean")
    void userSettingBooleanConversion() {
        UserSettingEntity setting = new UserSettingEntity("user1", "order.confirm.required", "true");

        assertTrue(setting.isBoolean());
        assertTrue(setting.asBoolean());

        setting.setSettingValue("false");
        assertFalse(setting.asBoolean());
    }

    @Test
    @DisplayName("UserSettingEntity: type conversion - integer")
    void userSettingIntegerConversion() {
        UserSettingEntity setting = new UserSettingEntity("user1", "order.max.qty.per.symbol", "1800");

        assertEquals(1800, setting.asInt(0));

        setting.setSettingValue("invalid");
        assertEquals(0, setting.asInt(0));
    }

    @Test
    @DisplayName("UserSettingEntity: type conversion - double")
    void userSettingDoubleConversion() {
        UserSettingEntity setting = new UserSettingEntity("user1", "order.price.deviation.pct", "1.5");

        assertEquals(1.5, setting.asDouble(0.0), 0.001);

        setting.setSettingValue("invalid");
        assertEquals(0.0, setting.asDouble(0.0), 0.001);
    }

    @Test
    @DisplayName("UserSettingId: equality check")
    void userSettingIdEquality() {
        UserSettingId id1 = new UserSettingId("user1", "order.confirm.required");
        UserSettingId id2 = new UserSettingId("user1", "order.confirm.required");
        UserSettingId id3 = new UserSettingId("user2", "order.confirm.required");

        assertEquals(id1, id2);
        assertNotEquals(id1, id3);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    // === 7.2 SettingsMetadata Tests ===

    @Test
    @DisplayName("SettingsMetadataEntity: data type checks")
    void settingsMetadataDataTypeChecks() {
        SettingsMetadataEntity boolMeta = new SettingsMetadataEntity();
        boolMeta.setDataType("BOOLEAN");

        SettingsMetadataEntity intMeta = new SettingsMetadataEntity();
        intMeta.setDataType("INTEGER");

        SettingsMetadataEntity enumMeta = new SettingsMetadataEntity();
        enumMeta.setDataType("ENUM");

        assertTrue(boolMeta.isBoolean());
        assertFalse(boolMeta.isInteger());

        assertTrue(intMeta.isInteger());
        assertFalse(intMeta.isBoolean());

        assertTrue(enumMeta.isEnum());
        assertFalse(enumMeta.isList());
    }

    @Test
    @DisplayName("SettingsMetadataEntity: decimal type")
    void settingsMetadataDecimalType() {
        SettingsMetadataEntity meta = new SettingsMetadataEntity();
        meta.setDataType("DECIMAL");
        meta.setMinValue("0.1");
        meta.setMaxValue("10");

        assertTrue(meta.isDecimal());
        assertEquals("0.1", meta.getMinValue());
        assertEquals("10", meta.getMaxValue());
    }

    @Test
    @DisplayName("SettingsMetadataEntity: list type")
    void settingsMetadataListType() {
        SettingsMetadataEntity meta = new SettingsMetadataEntity();
        meta.setDataType("LIST");
        meta.setAllowedValues("[\"NSE\",\"BSE\"]");

        assertTrue(meta.isList());
        assertFalse(meta.isEnum());
    }

    // === 9.1 Audit Log Tests ===

    @Test
    @DisplayName("SettingsAuditLogEntity: captures change")
    void auditLogCapturesChange() {
        SettingsAuditLogEntity audit = new SettingsAuditLogEntity();
        audit.setUserId("user1");
        audit.setSettingKey("order.confirm.required");
        audit.setOldValue("false");
        audit.setNewValue("true");
        audit.setChangedBy("user1");
        audit.setInterfaceType("UI");

        assertEquals("user1", audit.getUserId());
        assertEquals("order.confirm.required", audit.getSettingKey());
        assertEquals("false", audit.getOldValue());
        assertEquals("true", audit.getNewValue());
        assertEquals("UI", audit.getInterfaceType());
        assertNotNull(audit.getTimestamp());
    }

    @Test
    @DisplayName("SettingsAuditLogEntity: captures interface types")
    void auditLogCapturesInterfaceTypes() {
        SettingsAuditLogEntity cliAudit = new SettingsAuditLogEntity();
        cliAudit.setInterfaceType("CLI");

        SettingsAuditLogEntity apiAudit = new SettingsAuditLogEntity();
        apiAudit.setInterfaceType("API");

        assertEquals("CLI", cliAudit.getInterfaceType());
        assertEquals("API", apiAudit.getInterfaceType());
    }

    // === 7.2 Role Enforcement Tests (Entity level) ===

    @Test
    @DisplayName("UserSettingEntity: role min defaults")
    void userSettingRoleMinDefaults() {
        UserSettingEntity setting = new UserSettingEntity();

        assertEquals("TRADER", setting.getRoleMin());
        assertTrue(setting.getEditable());
        assertEquals("GLOBAL", setting.getScope());
    }

    @Test
    @DisplayName("SettingsMetadataEntity: admin-only settings")
    void settingsMetadataAdminOnly() {
        SettingsMetadataEntity meta = new SettingsMetadataEntity();
        meta.setSettingKey("broker.fallback.enabled");
        meta.setMinRole("ADMIN");
        meta.setEditable(true);

        assertEquals("ADMIN", meta.getMinRole());
    }

    @Test
    @DisplayName("SettingsMetadataEntity: read-only settings")
    void settingsMetadataReadOnly() {
        SettingsMetadataEntity meta = new SettingsMetadataEntity();
        meta.setSettingKey("system.version");
        meta.setEditable(false);

        assertFalse(meta.getEditable());
    }
}
