package com.vegatrader.upstox.api.logics;

import com.vegatrader.upstox.api.rms.enums.RmsRejectCode;
import com.vegatrader.upstox.api.expiry.entity.*;
import com.vegatrader.upstox.api.strike.entity.*;
import com.vegatrader.upstox.api.bse.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Logics module per a1.md sections 1-4.
 */
class LogicsTest {

    // === RmsRejectCode Tests (Section 4.3) ===

    @Test
    @DisplayName("RmsRejectCode: all codes exist")
    void allRmsCodesExist() {
        // 24 codes defined in enum (expiry:3, strike:3, price:3, qty:3, bse:3,
        // client:3, sector:2, general:3)
        assertTrue(RmsRejectCode.values().length >= 20);
    }

    @Test
    @DisplayName("RmsRejectCode: categories are correct")
    void rmsCodeCategories() {
        assertEquals("EXPIRY", RmsRejectCode.RMS_EXPIRY_INVALID.getCategory());
        assertEquals("STRIKE", RmsRejectCode.RMS_STRIKE_DISABLED.getCategory());
        assertEquals("PRICE", RmsRejectCode.RMS_PRICE_BAND.getCategory());
        assertEquals("QUANTITY", RmsRejectCode.RMS_QTY_CAP.getCategory());
        assertEquals("BSE", RmsRejectCode.RMS_BSE_T2T.getCategory());
        assertEquals("CLIENT", RmsRejectCode.RMS_CLIENT_LIMIT.getCategory());
    }

    @Test
    @DisplayName("RmsRejectCode: retry allowed")
    void rmsCodeRetryAllowed() {
        assertTrue(RmsRejectCode.RMS_PRICE_BAND.isRetryAllowed());
        assertTrue(RmsRejectCode.RMS_SYSTEM_ERROR.isRetryAllowed());
        assertFalse(RmsRejectCode.RMS_EXPIRY_INVALID.isRetryAllowed());
        assertFalse(RmsRejectCode.RMS_BSE_T2T.isRetryAllowed());
    }

    @Test
    @DisplayName("RmsRejectCode: BSE specific check")
    void rmsCodeBseSpecific() {
        assertTrue(RmsRejectCode.RMS_BSE_T2T.isBseSpecific());
        assertTrue(RmsRejectCode.RMS_BSE_CNC_ONLY.isBseSpecific());
        assertFalse(RmsRejectCode.RMS_PRICE_BAND.isBseSpecific());
    }

    // === ExchangeExpiryRuleEntity Tests (Section 1) ===

    @Test
    @DisplayName("ExchangeExpiryRuleEntity: cycle type checks")
    void expiryRuleCycleType() {
        ExchangeExpiryRuleEntity weekly = new ExchangeExpiryRuleEntity();
        weekly.setCycleType("WEEKLY");

        ExchangeExpiryRuleEntity monthly = new ExchangeExpiryRuleEntity();
        monthly.setCycleType("MONTHLY");

        assertTrue(weekly.isWeekly());
        assertFalse(weekly.isMonthly());

        assertTrue(monthly.isMonthly());
        assertFalse(monthly.isWeekly());
    }

    @Test
    @DisplayName("ExchangeExpiryRuleEntity: fallback strategy")
    void expiryRuleFallback() {
        ExchangeExpiryRuleEntity rule = new ExchangeExpiryRuleEntity();
        rule.setFallbackStrategy("PREVIOUS_TRADING_DAY");

        assertTrue(rule.usePreviousTradingDay());

        rule.setFallbackStrategy("NEXT_TRADING_DAY");
        assertFalse(rule.usePreviousTradingDay());
    }

    @Test
    @DisplayName("ExchangeExpiryRuleId: equality check")
    void expiryRuleIdEquality() {
        ExchangeExpiryRuleId id1 = new ExchangeExpiryRuleId("NSE", "OPTIDX", "WEEKLY");
        ExchangeExpiryRuleId id2 = new ExchangeExpiryRuleId("NSE", "OPTIDX", "WEEKLY");
        ExchangeExpiryRuleId id3 = new ExchangeExpiryRuleId("BSE", "OPTIDX", "WEEKLY");

        assertEquals(id1, id2);
        assertNotEquals(id1, id3);
    }

    // === StrikeStatusEntity Tests (Section 2) ===

    @Test
    @DisplayName("StrikeStatusEntity: option type checks")
    void strikeStatusOptionType() {
        StrikeStatusEntity ce = new StrikeStatusEntity();
        ce.setOptionType("CE");

        StrikeStatusEntity pe = new StrikeStatusEntity();
        pe.setOptionType("PE");

        assertTrue(ce.isCall());
        assertFalse(ce.isPut());

        assertTrue(pe.isPut());
        assertFalse(pe.isCall());
    }

    @Test
    @DisplayName("StrikeStatusId: equality check")
    void strikeStatusIdEquality() {
        StrikeStatusId id1 = new StrikeStatusId("NSE", "NIFTY", 24000.0, "CE");
        StrikeStatusId id2 = new StrikeStatusId("NSE", "NIFTY", 24000.0, "CE");
        StrikeStatusId id3 = new StrikeStatusId("NSE", "NIFTY", 24000.0, "PE");

        assertEquals(id1, id2);
        assertNotEquals(id1, id3);
    }

    // === BseGroupRuleEntity Tests (Section 3) ===

    @Test
    @DisplayName("BseGroupRuleEntity: T2T check")
    void bseGroupT2T() {
        BseGroupRuleEntity t2t = new BseGroupRuleEntity();
        t2t.setTradeForTrade(true);
        t2t.setCncOnly(true);

        BseGroupRuleEntity normal = new BseGroupRuleEntity();
        normal.setTradeForTrade(false);
        normal.setCncOnly(false);

        assertTrue(t2t.isTradeForTrade());
        assertTrue(t2t.isCncOnly());

        assertFalse(normal.isTradeForTrade());
        assertFalse(normal.isCncOnly());
    }

    @Test
    @DisplayName("BseGroupRuleEntity: blocked check")
    void bseGroupBlocked() {
        BseGroupRuleEntity active = new BseGroupRuleEntity();
        active.setActive(true);

        BseGroupRuleEntity blocked = new BseGroupRuleEntity();
        blocked.setActive(false);

        assertFalse(active.isBlocked());
        assertTrue(blocked.isBlocked());
    }

    // === StrikeSchemeEntity Tests ===

    @Test
    @DisplayName("StrikeSchemeId: equality check")
    void strikeSchemeIdEquality() {
        StrikeSchemeId id1 = new StrikeSchemeId("NSE", "NIFTY");
        StrikeSchemeId id2 = new StrikeSchemeId("NSE", "NIFTY");
        StrikeSchemeId id3 = new StrikeSchemeId("NSE", "BANKNIFTY");

        assertEquals(id1, id2);
        assertNotEquals(id1, id3);
    }
}
