package com.vegatrader.upstox.api.rms;

import com.vegatrader.upstox.api.rms.entity.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RMS entities.
 * Tests entity logic, validators, and utility methods.
 * 
 * Section 3: Entity & Schema Tests
 */
class RmsEntityTest {

    // === 3.1 EquitySecurityType Tests ===

    @Test
    @DisplayName("EquitySecurityType: NORMAL allows all products")
    void normalSecurityTypeAllowsAll() {
        EquitySecurityType type = EquitySecurityType.NORMAL;

        assertTrue(type.isMisAllowed());
        assertTrue(type.isMtfAllowed());
        assertTrue(type.isCncAllowed());
    }

    @Test
    @DisplayName("EquitySecurityType: SME allows CNC only")
    void smeSecurityTypeCncOnly() {
        EquitySecurityType type = EquitySecurityType.SME;

        assertFalse(type.isMisAllowed());
        assertFalse(type.isMtfAllowed());
        assertTrue(type.isCncAllowed());
    }

    @Test
    @DisplayName("EquitySecurityType: IPO allows CNC only")
    void ipoSecurityTypeCncOnly() {
        EquitySecurityType type = EquitySecurityType.IPO;

        assertFalse(type.isMisAllowed());
        assertFalse(type.isMtfAllowed());
        assertTrue(type.isCncAllowed());
    }

    @Test
    @DisplayName("EquitySecurityType: PCA allows CNC only")
    void pcaSecurityTypeCncOnly() {
        EquitySecurityType type = EquitySecurityType.PCA;

        assertFalse(type.isMisAllowed());
        assertFalse(type.isMtfAllowed());
        assertTrue(type.isCncAllowed());
    }

    @Test
    @DisplayName("EquitySecurityType: fromCode handles null/invalid")
    void fromCodeHandlesInvalid() {
        assertEquals(EquitySecurityType.NORMAL, EquitySecurityType.fromCode(null));
        assertEquals(EquitySecurityType.NORMAL, EquitySecurityType.fromCode("INVALID"));
        assertEquals(EquitySecurityType.SME, EquitySecurityType.fromCode("sme"));
    }

    // === 3.2 ExchangeSeriesEntity Tests ===

    @Test
    @DisplayName("ExchangeSeriesEntity: EQ series allows intraday")
    void eqSeriesAllowsIntraday() {
        ExchangeSeriesEntity series = new ExchangeSeriesEntity();
        series.setExchange("NSE");
        series.setSeriesCode("EQ");
        series.setRollingSettlement(true);
        series.setTradeForTrade(false);
        series.setMisAllowed(true);

        assertTrue(series.isIntradayAllowed());
    }

    @Test
    @DisplayName("ExchangeSeriesEntity: BE series (T2T) blocks intraday")
    void beSeriesBlocksIntraday() {
        ExchangeSeriesEntity series = new ExchangeSeriesEntity();
        series.setExchange("NSE");
        series.setSeriesCode("BE");
        series.setRollingSettlement(true);
        series.setTradeForTrade(true);
        series.setMisAllowed(false);

        assertFalse(series.isIntradayAllowed());
    }

    // === RegulatoryWatchlistEntity Tests ===

    @Test
    @DisplayName("RegulatoryWatchlistEntity: isActive checks dates")
    void watchlistIsActiveChecksDate() {
        RegulatoryWatchlistEntity watchlist = new RegulatoryWatchlistEntity();
        watchlist.setEffectiveDate(LocalDate.now().minusDays(1));
        watchlist.setExpiryDate(LocalDate.now().plusDays(1));

        assertTrue(watchlist.isActive());
    }

    @Test
    @DisplayName("RegulatoryWatchlistEntity: expired entry is inactive")
    void watchlistExpiredIsInactive() {
        RegulatoryWatchlistEntity watchlist = new RegulatoryWatchlistEntity();
        watchlist.setEffectiveDate(LocalDate.now().minusDays(10));
        watchlist.setExpiryDate(LocalDate.now().minusDays(1));

        assertFalse(watchlist.isActive());
    }

    @Test
    @DisplayName("RegulatoryWatchlistEntity: PCA type identified")
    void watchlistPcaTypeIdentified() {
        RegulatoryWatchlistEntity watchlist = new RegulatoryWatchlistEntity();
        watchlist.setWatchType("PCA");

        assertTrue(watchlist.isPca());
        assertFalse(watchlist.isSurveillance());
    }

    // === IpoCalendarEntity Tests ===

    @Test
    @DisplayName("IpoCalendarEntity: isListingDay checks today")
    void ipoListingDayToday() {
        IpoCalendarEntity ipo = new IpoCalendarEntity();
        ipo.setListingDate(LocalDate.now());

        assertTrue(ipo.isListingDay());
    }

    @Test
    @DisplayName("IpoCalendarEntity: past listing not listing day")
    void ipoPastListingNotListingDay() {
        IpoCalendarEntity ipo = new IpoCalendarEntity();
        ipo.setListingDate(LocalDate.now().minusDays(5));

        assertFalse(ipo.isListingDay());
        assertTrue(ipo.isPastListing());
    }

    // === PriceBandEntity Tests ===

    @Test
    @DisplayName("PriceBandEntity: price within band")
    void priceWithinBand() {
        PriceBandEntity band = new PriceBandEntity();
        band.setLowerPrice(90.0);
        band.setUpperPrice(110.0);

        assertTrue(band.isWithinBand(100.0));
        assertFalse(band.isOutsideBand(100.0));
    }

    @Test
    @DisplayName("PriceBandEntity: price outside band")
    void priceOutsideBand() {
        PriceBandEntity band = new PriceBandEntity();
        band.setLowerPrice(90.0);
        band.setUpperPrice(110.0);

        assertTrue(band.isOutsideBand(85.0));
        assertTrue(band.isOutsideBand(115.0));
    }

    // === QuantityCapEntity Tests ===

    @Test
    @DisplayName("QuantityCapEntity: exceeds qty limit")
    void quantityCapExceedsQty() {
        QuantityCapEntity cap = new QuantityCapEntity();
        cap.setMaxQty(100);
        cap.setMaxValue(null);
        cap.setEffectiveDate(LocalDate.now().minusDays(1));

        assertTrue(cap.exceedsLimit(150, 100.0));
        assertFalse(cap.exceedsLimit(50, 100.0));
    }

    @Test
    @DisplayName("QuantityCapEntity: exceeds value limit")
    void quantityCapExceedsValue() {
        QuantityCapEntity cap = new QuantityCapEntity();
        cap.setMaxQty(1000);
        cap.setMaxValue(50000.0);
        cap.setEffectiveDate(LocalDate.now().minusDays(1));

        assertTrue(cap.exceedsLimit(100, 600.0)); // 60000 > 50000
        assertFalse(cap.exceedsLimit(100, 400.0)); // 40000 < 50000
    }

    // === FoContractLifecycleEntity Tests ===

    @Test
    @DisplayName("FoContractLifecycleEntity: expired contract")
    void foContractExpired() {
        FoContractLifecycleEntity contract = new FoContractLifecycleEntity();
        contract.setExpiryDate(LocalDate.now().minusDays(1));

        assertTrue(contract.isExpired());
    }

    @Test
    @DisplayName("FoContractLifecycleEntity: days to expiry")
    void foContractDaysToExpiry() {
        FoContractLifecycleEntity contract = new FoContractLifecycleEntity();
        contract.setExpiryDate(LocalDate.now().plusDays(10));

        assertEquals(10, contract.daysToExpiry());
    }

    @Test
    @DisplayName("FoContractLifecycleEntity: type detection")
    void foContractTypeDetection() {
        FoContractLifecycleEntity future = new FoContractLifecycleEntity();
        future.setInstrumentType("FUT");

        FoContractLifecycleEntity call = new FoContractLifecycleEntity();
        call.setInstrumentType("CE");

        assertTrue(future.isFuture());
        assertFalse(future.isOption());
        assertTrue(call.isOption());
        assertFalse(call.isFuture());
    }
}
