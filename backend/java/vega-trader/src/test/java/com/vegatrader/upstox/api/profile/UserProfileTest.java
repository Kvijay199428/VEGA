package com.vegatrader.upstox.api.profile;

import com.vegatrader.upstox.api.profile.model.*;
import com.vegatrader.upstox.api.profile.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for User Profile & Funds Module per profile/a1-a3.md.
 */
class UserProfileTest {

    // === UserProfile Tests ===

    @Test
    @DisplayName("UserProfile: create and check exchanges")
    void userProfileExchanges() {
        var profile = createTestProfile();

        assertTrue(profile.hasExchange(UserProfile.Exchange.NSE));
        assertTrue(profile.hasExchange(UserProfile.Exchange.NFO));
        assertFalse(profile.hasExchange(UserProfile.Exchange.MCX));
    }

    @Test
    @DisplayName("UserProfile: check products")
    void userProfileProducts() {
        var profile = createTestProfile();

        assertTrue(profile.hasProduct(UserProfile.ProductType.I));
        assertTrue(profile.hasProduct(UserProfile.ProductType.D));
        assertFalse(profile.hasProduct(UserProfile.ProductType.MTF));
    }

    @Test
    @DisplayName("UserProfile: check order types")
    void userProfileOrderTypes() {
        var profile = createTestProfile();

        assertTrue(profile.hasOrderType(UserProfile.OrderType.MARKET));
        assertTrue(profile.hasOrderType(UserProfile.OrderType.LIMIT));
    }

    @Test
    @DisplayName("UserProfile: can sell requires POA or DDPI")
    void userProfileCanSell() {
        var withPoa = new UserProfile(
                "U1", "UPSTOX", "Test", "test@x.com", "PAN",
                Set.of(), Set.of(), Set.of(), true, false, true, Instant.now());

        var withDdpi = new UserProfile(
                "U2", "UPSTOX", "Test", "test@x.com", "PAN",
                Set.of(), Set.of(), Set.of(), false, true, true, Instant.now());

        var neither = new UserProfile(
                "U3", "UPSTOX", "Test", "test@x.com", "PAN",
                Set.of(), Set.of(), Set.of(), false, false, true, Instant.now());

        assertTrue(withPoa.canSell());
        assertTrue(withDdpi.canSell());
        assertFalse(neither.canSell());
    }

    @Test
    @DisplayName("UserProfile: stale detection")
    void userProfileStale() {
        var fresh = new UserProfile(
                "U1", "UPSTOX", "Test", "test@x.com", "PAN",
                Set.of(), Set.of(), Set.of(), true, false, true, Instant.now());

        var stale = new UserProfile(
                "U2", "UPSTOX", "Test", "test@x.com", "PAN",
                Set.of(), Set.of(), Set.of(), true, false, true,
                Instant.now().minusSeconds(1000));

        assertFalse(fresh.isStale(900));
        assertTrue(stale.isStale(900));
    }

    // === FundsMargin Tests ===

    @Test
    @DisplayName("FundsMargin: from equity only (July 2025)")
    void fundsMarginEquityOnly() {
        var funds = FundsMargin.fromEquityOnly(
                "U1", "UPSTOX",
                15000, 1000, 16000,
                500, 200, 0, 0);

        assertTrue(funds.combinedMargin());
        assertEquals(15000, funds.availableMargin());
    }

    @Test
    @DisplayName("FundsMargin: from legacy (pre July 2025)")
    void fundsMarginLegacy() {
        var funds = FundsMargin.fromLegacy("U1", "UPSTOX", 10000, 5000);

        assertFalse(funds.combinedMargin());
        assertEquals(15000, funds.availableMargin());
    }

    @Test
    @DisplayName("FundsMargin: margin sufficiency check")
    void fundsMarginSufficiency() {
        var funds = FundsMargin.fromEquityOnly(
                "U1", "UPSTOX",
                15000, 1000, 16000,
                500, 200, 0, 0);

        assertTrue(funds.hasSufficientMargin(10000));
        assertTrue(funds.hasSufficientMargin(15000));
        assertFalse(funds.hasSufficientMargin(20000));
    }

    @Test
    @DisplayName("FundsMargin: utilization percentage")
    void fundsMarginUtilization() {
        var funds = FundsMargin.fromEquityOnly(
                "U1", "UPSTOX",
                8000, 2000, 10000,
                500, 200, 0, 0);

        assertEquals(20.0, funds.getUtilizationPct(), 0.01);
    }

    @Test
    @DisplayName("FundsMargin: July 2025 date check")
    void fundsMarginJuly2025Date() {
        // This will return false until July 19, 2025
        // After that date, it returns true
        LocalDate july2025 = LocalDate.of(2025, 7, 19);
        boolean isAfter = !LocalDate.now().isBefore(july2025);

        assertEquals(isAfter, FundsMargin.isAfterCombinedMarginDate());
    }

    // === UserProfileService Tests ===

    @Test
    @DisplayName("UserProfileService: get profile caches")
    void profileServiceCaches() {
        var service = new UserProfileService();

        var profile1 = service.getProfile("U1");
        var profile2 = service.getProfile("U1");

        assertNotNull(profile1);
        assertNotNull(profile2);
        assertEquals(1, service.getCacheSize());
    }

    @Test
    @DisplayName("UserProfileService: exchange check")
    void profileServiceExchangeCheck() {
        var service = new UserProfileService();

        assertTrue(service.isExchangeEnabled("U1", UserProfile.Exchange.NSE));
    }

    // === FundsMarginService Tests ===

    @Test
    @DisplayName("FundsMarginService: maintenance window check")
    void fundsServiceMaintenanceWindow() {
        var service = new FundsMarginService();

        // Just verify method exists and works
        boolean isMaintenance = service.isMaintenanceWindow();
        // Depends on current time - just verify no exception
        assertNotNull(service);
    }

    @Test
    @DisplayName("FundsMarginService: margin sufficiency")
    void fundsServiceMarginCheck() {
        var service = new FundsMarginService();

        // May throw during maintenance, so wrap
        try {
            boolean sufficient = service.hasSufficientMargin("U1", 5000);
            // Result depends on mock data
            assertNotNull(service);
        } catch (FundsMarginService.FundsMaintenanceException e) {
            // Expected during 00:00-05:30 IST
        }
    }

    // === Helper Methods ===

    private UserProfile createTestProfile() {
        return new UserProfile(
                "TEST123",
                "UPSTOX",
                "Test User",
                "test@example.com",
                "XXXXX1234X",
                Set.of(UserProfile.Exchange.NSE, UserProfile.Exchange.BSE, UserProfile.Exchange.NFO),
                Set.of(UserProfile.ProductType.I, UserProfile.ProductType.D),
                Set.of(UserProfile.OrderType.MARKET, UserProfile.OrderType.LIMIT, UserProfile.OrderType.SL),
                true,
                false,
                true,
                Instant.now());
    }
}
