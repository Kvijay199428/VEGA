package com.vegatrader.upstox.api.settings;

import com.vegatrader.upstox.api.settings.model.*;
import com.vegatrader.upstox.api.settings.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Admin Settings Framework per IMPLEMENTATION_ROADMAP.md.
 */
class AdminSettingsTest {

    private AdminSettingsService service;

    @BeforeEach
    void setUp() {
        service = new AdminSettingsService();
    }

    // === SettingDefinition Tests ===

    @Test
    @DisplayName("SettingDefinition: validate integer in range")
    void settingDefinitionIntegerValid() {
        var def = new SettingDefinition(
                "test.maxQty", SettingDefinition.SettingScope.ADMIN,
                SettingDefinition.DataType.INTEGER, "100", 1.0, 500.0,
                null, false, false, "TEST", "Test setting");

        var result = def.validate("250");
        assertTrue(result.valid());
    }

    @Test
    @DisplayName("SettingDefinition: validate integer out of range")
    void settingDefinitionIntegerOutOfRange() {
        var def = new SettingDefinition(
                "test.maxQty", SettingDefinition.SettingScope.ADMIN,
                SettingDefinition.DataType.INTEGER, "100", 1.0, 500.0,
                null, false, false, "TEST", "Test setting");

        var result = def.validate("1000");
        assertFalse(result.valid());
        assertTrue(result.message().contains("maximum"));
    }

    @Test
    @DisplayName("SettingDefinition: validate boolean")
    void settingDefinitionBoolean() {
        var def = new SettingDefinition(
                "test.enabled", SettingDefinition.SettingScope.ADMIN,
                SettingDefinition.DataType.BOOLEAN, "false", null, null,
                null, false, false, "TEST", "Test setting");

        assertTrue(def.validate("true").valid());
        assertTrue(def.validate("false").valid());
        assertFalse(def.validate("yes").valid());
    }

    @Test
    @DisplayName("SettingDefinition: locked setting not editable")
    void settingDefinitionLocked() {
        var def = new SettingDefinition(
                "test.locked", SettingDefinition.SettingScope.ADMIN,
                SettingDefinition.DataType.STRING, "default", null, null,
                null, true, false, "TEST", "Locked setting");

        var result = def.validate("newValue");
        assertFalse(result.valid());
        assertTrue(result.message().contains("locked"));
    }

    @Test
    @DisplayName("SettingDefinition: scope checks")
    void settingDefinitionScopes() {
        var adminDef = new SettingDefinition(
                "test.admin", SettingDefinition.SettingScope.ADMIN,
                SettingDefinition.DataType.STRING, "default", null, null,
                null, false, false, "TEST", "Admin setting");

        var userDef = new SettingDefinition(
                "test.user", SettingDefinition.SettingScope.USER,
                SettingDefinition.DataType.STRING, "default", null, null,
                null, false, false, "TEST", "User setting");

        assertTrue(adminDef.isAdminEditable());
        assertFalse(adminDef.isUserEditable());
        assertTrue(userDef.isUserEditable());
        assertTrue(userDef.isAdminEditable());
    }

    // === AdminSetting Tests ===

    @Test
    @DisplayName("AdminSetting: build and check active")
    void adminSettingBuildActive() {
        var setting = AdminSetting.builder()
                .key("test.key")
                .value("test-value")
                .updatedBy("admin")
                .reasonCode("TEST")
                .build();

        assertEquals("test.key", setting.key());
        assertEquals("test-value", setting.value());
        assertTrue(setting.isActive());
        assertFalse(setting.isScheduled());
    }

    @Test
    @DisplayName("AdminSetting: scheduled setting")
    void adminSettingScheduled() {
        var setting = AdminSetting.builder()
                .key("test.key")
                .value("future-value")
                .effectiveFrom(Instant.now().plusSeconds(3600))
                .updatedBy("admin")
                .reasonCode("SCHEDULED")
                .build();

        assertTrue(setting.isScheduled());
        assertFalse(setting.isActive());
    }

    // === AdminSettingsService Tests ===

    @Test
    @DisplayName("AdminSettingsService: get setting value")
    void serviceGetSettingValue() {
        String value = service.getSettingValue("trading.maxOrderQty");
        assertEquals("1800", value);
    }

    @Test
    @DisplayName("AdminSettingsService: get as int")
    void serviceGetInt() {
        int value = service.getInt("trading.maxOrderQty", 0);
        assertEquals(1800, value);
    }

    @Test
    @DisplayName("AdminSettingsService: get as boolean")
    void serviceGetBoolean() {
        boolean value = service.getBoolean("rms.killSwitch.enabled", true);
        assertFalse(value);
    }

    @Test
    @DisplayName("AdminSettingsService: update setting")
    void serviceUpdateSetting() {
        var result = service.updateSetting(
                "trading.maxOrderQty", "2000", "test-admin", "CHANGE", "Test update");

        assertTrue(result.success());
        assertEquals("1800", result.oldValue());
        assertEquals("2000", result.newValue());

        // Verify new value
        assertEquals("2000", service.getSettingValue("trading.maxOrderQty"));
    }

    @Test
    @DisplayName("AdminSettingsService: update with invalid value")
    void serviceUpdateInvalidValue() {
        var result = service.updateSetting(
                "trading.maxOrderQty", "10000", "test-admin", "CHANGE", "Invalid");

        assertFalse(result.success());
        assertTrue(result.error().contains("maximum"));
    }

    @Test
    @DisplayName("AdminSettingsService: kill switch enable/disable")
    void serviceKillSwitch() {
        assertFalse(service.isKillSwitchEnabled());

        service.enableKillSwitch("MARKET_DISRUPTION", "risk-admin");
        assertTrue(service.isKillSwitchEnabled());
        assertEquals("MARKET_DISRUPTION", service.getSettingValue("rms.killSwitch.reason"));

        service.disableKillSwitch("risk-admin", "Market stabilized");
        assertFalse(service.isKillSwitchEnabled());
    }

    @Test
    @DisplayName("AdminSettingsService: audit log")
    void serviceAuditLog() {
        service.updateSetting("options.maxStrikesPerSide", "25", "admin", "TEST", "Test");

        var log = service.getAuditLog(10);
        assertFalse(log.isEmpty());

        var entry = log.get(log.size() - 1);
        assertEquals("options.maxStrikesPerSide", entry.key());
        assertEquals("20", entry.oldValue());
        assertEquals("25", entry.newValue());
    }

    @Test
    @DisplayName("AdminSettingsService: get definitions by category")
    void serviceDefinitionsByCategory() {
        var tradingDefs = service.getDefinitionsByCategory("TRADING");
        assertFalse(tradingDefs.isEmpty());

        for (var def : tradingDefs) {
            assertEquals("TRADING", def.category());
        }
    }
}
