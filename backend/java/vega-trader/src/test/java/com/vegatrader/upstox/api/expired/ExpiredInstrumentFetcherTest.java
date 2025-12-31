package com.vegatrader.upstox.api.expired;

import com.vegatrader.upstox.api.expired.model.*;
import com.vegatrader.upstox.api.expired.service.ExpiredInstrumentFetcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ExpiredInstrumentFetcher integration layer and DTOs.
 * Per b1-b3.md: Integration tests for settings + expired data flow.
 */
class ExpiredInstrumentFetcherTest {

    // === ExpiredDataRequest Tests ===

    @Test
    @DisplayName("ExpiredDataRequest: factory methods")
    void dataRequestFactoryMethods() {
        ExpiredDataRequest forUnderlying = ExpiredDataRequest.forUnderlying("NSE_INDEX|Nifty 50");
        assertTrue(forUnderlying.isValid());
        assertEquals("NSE_INDEX|Nifty 50", forUnderlying.underlyingKey());
        assertEquals("both", forUnderlying.instrumentType());

        LocalDate expiry = LocalDate.of(2024, 4, 25);
        ExpiredDataRequest forOptions = ExpiredDataRequest.forOptions("NSE_INDEX|Nifty 50", expiry);
        assertEquals("options", forOptions.instrumentType());
        assertEquals(expiry, forOptions.expiry());

        ExpiredDataRequest forFutures = ExpiredDataRequest.forFutures("NSE_INDEX|Nifty 50", expiry);
        assertEquals("futures", forFutures.instrumentType());
    }

    @Test
    @DisplayName("ExpiredDataRequest: validation")
    void dataRequestValidation() {
        ExpiredDataRequest valid = ExpiredDataRequest.forUnderlying("NSE_INDEX|Nifty 50");
        assertTrue(valid.isValid());

        ExpiredDataRequest invalid = new ExpiredDataRequest(
                null, null, "both", "day",
                LocalDate.now(), LocalDate.now(), true);
        assertFalse(invalid.isValid());

        // From date after to date
        ExpiredDataRequest invalidRange = new ExpiredDataRequest(
                "NSE_INDEX|Nifty 50", null, "both", "day",
                LocalDate.now(), LocalDate.now().minusDays(1), true);
        assertFalse(invalidRange.isValid());
    }

    // === ExpiredDataResponse Tests ===

    @Test
    @DisplayName("ExpiredDataResponse: factory for candles")
    void dataResponseCandles() {
        List<Candle> candles = List.of(
                new Candle(ZonedDateTime.now(), 100, 110, 95, 105, 10000, 5000));

        ExpiredDataResponse response = ExpiredDataResponse.ofCandles(
                "NSE_INDEX|Nifty 50", LocalDate.of(2024, 4, 25), "day", candles);

        assertTrue(response.hasData());
        assertEquals(1, response.candleCount());
        assertEquals("day", response.interval());
    }

    @Test
    @DisplayName("ExpiredDataResponse: factory for options")
    void dataResponseOptions() {
        List<ExpiredOptionContract> options = List.of(
                new ExpiredOptionContract("NSE_FO|123", "NIFTY24APR24000CE", "CE",
                        24000, 50, "NSE_INDEX|Nifty 50", LocalDate.of(2024, 4, 25)));

        ExpiredDataResponse response = ExpiredDataResponse.ofOptions(
                "NSE_INDEX|Nifty 50", LocalDate.of(2024, 4, 25), options);

        assertTrue(response.hasData());
        assertEquals(1, response.options().size());
        assertEquals("options", response.instrumentType());
    }

    @Test
    @DisplayName("ExpiredDataResponse: factory for futures")
    void dataResponseFutures() {
        List<ExpiredFutureContract> futures = List.of(
                new ExpiredFutureContract("NSE_FO|456", "NIFTY24APRFUT",
                        50, "NSE_INDEX|Nifty 50", LocalDate.of(2024, 4, 25)));

        ExpiredDataResponse response = ExpiredDataResponse.ofFutures(
                "NSE_INDEX|Nifty 50", LocalDate.of(2024, 4, 25), futures);

        assertTrue(response.hasData());
        assertEquals(1, response.futures().size());
        assertEquals("futures", response.instrumentType());
    }

    @Test
    @DisplayName("ExpiredDataResponse: full response")
    void dataResponseFull() {
        List<Candle> candles = List.of(
                new Candle(ZonedDateTime.now(), 100, 110, 95, 105, 10000, 5000));
        List<ExpiredOptionContract> options = List.of(
                new ExpiredOptionContract("NSE_FO|123", "NIFTY24APR24000CE", "CE",
                        24000, 50, "NSE_INDEX|Nifty 50", LocalDate.of(2024, 4, 25)));
        List<ExpiredFutureContract> futures = List.of(
                new ExpiredFutureContract("NSE_FO|456", "NIFTY24APRFUT",
                        50, "NSE_INDEX|Nifty 50", LocalDate.of(2024, 4, 25)));

        ExpiredDataResponse response = ExpiredDataResponse.full(
                "NSE_INDEX|Nifty 50", LocalDate.of(2024, 4, 25), "5minute",
                candles, options, futures);

        assertTrue(response.hasData());
        assertEquals(1, response.candleCount());
        assertEquals(1, response.options().size());
        assertEquals(1, response.futures().size());
        assertEquals("both", response.instrumentType());
    }

    @Test
    @DisplayName("ExpiredDataResponse: no data check")
    void dataResponseNoData() {
        ExpiredDataResponse empty = ExpiredDataResponse.ofCandles(
                "NSE_INDEX|Nifty 50", LocalDate.of(2024, 4, 25), "day", List.of());

        assertFalse(empty.hasData());
        assertEquals(0, empty.candleCount());
    }

    // === ExpiredFetchSettings Tests ===

    @Test
    @DisplayName("ExpiredFetchSettings: record creation")
    void fetchSettingsRecord() {
        ExpiredInstrumentFetcher.ExpiredFetchSettings settings = new ExpiredInstrumentFetcher.ExpiredFetchSettings(
                "latest", "both", "day", 365, true, true, true);

        assertEquals("latest", settings.expiryFetch());
        assertEquals("both", settings.instrumentType());
        assertEquals("day", settings.interval());
        assertEquals(365, settings.maxHistoricalDays());
        assertTrue(settings.showWeeklyOptions());
        assertTrue(settings.cacheExpiries());
        assertTrue(settings.cacheContracts());
    }
}
