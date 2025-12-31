package com.vegatrader.upstox.api.settings;

import com.vegatrader.upstox.api.settings.model.UserPrioritySettings;
import com.vegatrader.upstox.api.settings.service.SettingsResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Final Settings per final-settings.md.
 */
class FinalSettingsTest {

    // === UserPrioritySettings Tests ===

    @Test
    @DisplayName("UserPrioritySettings: defaults creation")
    void defaultsCreation() {
        UserPrioritySettings defaults = UserPrioritySettings.defaults();

        assertNotNull(defaults);
        assertEquals(List.of("INDEX", "DERIVATIVES", "EQUITY"), defaults.instrumentLoadPriority());
        assertEquals(List.of(), defaults.preferredSectors());
        assertEquals(List.of("INSTRUMENT_ELIGIBILITY", "CLIENT_RISK", "QUANTITY_CAP"),
                defaults.validationPriority());
        assertEquals("INTRADAY", defaults.defaultProductType());
        assertEquals("NSE", defaults.defaultExchange());
        assertTrue(defaults.confirmBeforePlace());
    }

    @Test
    @DisplayName("UserPrioritySettings: validation - valid settings")
    void validationValidSettings() {
        UserPrioritySettings settings = new UserPrioritySettings(
                List.of("INDEX", "DERIVATIVES"),
                List.of("BANKING", "IT"),
                List.of("INSTRUMENT_ELIGIBILITY", "CLIENT_RISK"),
                List.of("UPSTOX"),
                "DELIVERY",
                "NSE",
                true);

        assertTrue(settings.isValid());
    }

    @Test
    @DisplayName("UserPrioritySettings: validation - invalid exchange")
    void validationInvalidExchange() {
        UserPrioritySettings settings = new UserPrioritySettings(
                List.of("INDEX"),
                List.of(),
                List.of("INSTRUMENT_ELIGIBILITY"),
                List.of("PRIMARY"),
                "INTRADAY",
                "INVALID_EXCHANGE", // Invalid
                true);

        assertFalse(settings.isValid());
    }

    @Test
    @DisplayName("UserPrioritySettings: validation - invalid product type")
    void validationInvalidProductType() {
        UserPrioritySettings settings = new UserPrioritySettings(
                List.of("INDEX"),
                List.of(),
                List.of("INSTRUMENT_ELIGIBILITY"),
                List.of("PRIMARY"),
                "INVALID_PRODUCT", // Invalid
                "NSE",
                true);

        assertFalse(settings.isValid());
    }

    @Test
    @DisplayName("UserPrioritySettings: merge with defaults")
    void mergeWithDefaults() {
        UserPrioritySettings partial = new UserPrioritySettings(
                List.of("SECTORAL", "INDEX"), // Custom
                null, // Use default
                null, // Use default
                List.of("ZERODHA"), // Custom
                null, // Use default
                "BSE", // Custom
                false);

        UserPrioritySettings merged = partial.mergeWithDefaults();

        assertEquals(List.of("SECTORAL", "INDEX"), merged.instrumentLoadPriority());
        assertEquals(List.of(), merged.preferredSectors()); // Default
        assertEquals(List.of("ZERODHA"), merged.brokerRoutingPriority());
        assertEquals("BSE", merged.defaultExchange());
    }

    // === SettingsResolver Tests ===

    @Test
    @DisplayName("SettingsResolver: get system defaults")
    void resolverSystemDefaults() {
        SettingsResolver resolver = new SettingsResolver();

        UserPrioritySettings defaults = resolver.getSystemDefaults();

        assertNotNull(defaults);
        assertEquals("NSE", defaults.defaultExchange());
    }

    @Test
    @DisplayName("SettingsResolver: resolve with no user settings")
    void resolverNoUserSettings() {
        SettingsResolver resolver = new SettingsResolver();

        UserPrioritySettings resolved = resolver.resolveSettings("user123", null);

        // Should return defaults
        assertEquals(UserPrioritySettings.defaults(), resolved);
    }

    @Test
    @DisplayName("SettingsResolver: save and retrieve user settings")
    void resolverSaveAndRetrieve() {
        SettingsResolver resolver = new SettingsResolver();

        UserPrioritySettings settings = new UserPrioritySettings(
                List.of("DERIVATIVES", "INDEX"),
                List.of("PHARMA"),
                List.of("CLIENT_RISK", "INSTRUMENT_ELIGIBILITY"),
                List.of("UPSTOX"),
                "CNC",
                "BSE",
                false);

        resolver.saveUserSettings("user456", settings);

        UserPrioritySettings retrieved = resolver.getUserSettings("user456");
        assertNotNull(retrieved);
        assertEquals("BSE", retrieved.defaultExchange());
        assertEquals("CNC", retrieved.defaultProductType());
    }

    @Test
    @DisplayName("SettingsResolver: invalid settings rejected")
    void resolverInvalidSettingsRejected() {
        SettingsResolver resolver = new SettingsResolver();

        UserPrioritySettings invalid = new UserPrioritySettings(
                List.of("INDEX"),
                List.of(),
                List.of("INVALID_CHECK"), // Invalid validation priority
                List.of("PRIMARY"),
                "INTRADAY",
                "NSE",
                true);

        assertThrows(IllegalArgumentException.class, () -> resolver.saveUserSettings("user789", invalid));
    }
}
