package com.vegatrader.upstox.api.optionchain;

import com.vegatrader.upstox.api.optionchain.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Option Chain module per a1-a4.md.
 * 
 * Note: Service tests that require DI are disabled.
 * Use @SpringBootTest for integration testing.
 */
class OptionChainTest {

    // === OptionChainStrike Tests ===

    @Test
    @DisplayName("OptionChainStrike: ITM/OTM/ATM detection for calls")
    void strikeITMOTMDetection() {
        var strike = createStrike(24000, 24150);

        assertTrue(strike.isCallITM()); // 24000 < 24150
        assertFalse(strike.isPutITM()); // 24000 NOT > 24150
    }

    @Test
    @DisplayName("OptionChainStrike: ATM detection")
    void strikeATMDetection() {
        var atmStrike = createStrike(24100, 24150); // Within 1%
        var otmStrike = createStrike(23000, 24150); // >1% away

        assertTrue(atmStrike.isATM());
        assertFalse(otmStrike.isATM());
    }

    // === OptionChainResponse Tests ===

    @Test
    @DisplayName("OptionChainResponse: success factory")
    void responseSuccessFactory() {
        var response = OptionChainResponse.success(
                "NSE_INDEX|Nifty 50",
                LocalDate.of(2025, 1, 30),
                24150.25,
                "API",
                List.of(createStrike(24000, 24150)));

        assertEquals("success", response.status());
        assertEquals(1, response.strikeCount());
        assertTrue(response.hasData());
    }

    @Test
    @DisplayName("OptionChainResponse: error factory")
    void responseErrorFactory() {
        var response = OptionChainResponse.error(
                "NSE_INDEX|Nifty 50",
                LocalDate.of(2025, 1, 30),
                "API timeout");

        assertTrue(response.status().contains("error"));
        assertFalse(response.hasData());
    }

    @Test
    @DisplayName("OptionChainResponse: ATM strike detection")
    void responseATMStrike() {
        var response = OptionChainResponse.success(
                "NSE_INDEX|Nifty 50",
                LocalDate.of(2025, 1, 30),
                24150,
                "API",
                List.of(
                        createStrike(24000, 24150),
                        createStrike(24100, 24150), // Closest to spot
                        createStrike(24200, 24150)));

        var atm = response.getATMStrike();
        assertNotNull(atm);
        assertEquals(24100, atm.strikePrice());
    }

    @Test
    @DisplayName("OptionChainResponse: total OI calculation")
    void responseTotalOI() {
        var response = OptionChainResponse.success(
                "NSE_INDEX|Nifty 50",
                LocalDate.of(2025, 1, 30),
                24150,
                "API",
                List.of(
                        createStrikeWithOI(24000, 5000000, 4000000),
                        createStrikeWithOI(24100, 3000000, 2500000)));

        assertEquals(8000000, response.getTotalCallOI());
        assertEquals(6500000, response.getTotalPutOI());
        assertEquals(0.8125, response.getOverallPCR(), 0.001);
    }

    // === OptionChainService Tests - Disabled (require DI) ===

    @Test
    @Disabled("Service now requires DI - use integration tests")
    @DisplayName("OptionChainService: cache hit returns cached data")
    void serviceCacheHit() {
        // OptionChainService now requires dependencies
    }

    @Test
    @Disabled("Service now requires DI - use integration tests")
    @DisplayName("OptionChainService: clear cache")
    void serviceClearCache() {
        // OptionChainService now requires dependencies
    }

    // === Helper Methods ===

    private OptionChainStrike createStrike(double strike, double spot) {
        return new OptionChainStrike(
                "NSE_INDEX|Nifty 50",
                LocalDate.of(2025, 1, 30),
                strike,
                spot,
                0.85,
                createOptionData(100, 0, 0),
                createOptionData(80, 0, 0),
                ZonedDateTime.now());
    }

    private OptionChainStrike createStrikeWithOI(double strike, long callOI, long putOI) {
        return new OptionChainStrike(
                "NSE_INDEX|Nifty 50",
                LocalDate.of(2025, 1, 30),
                strike,
                24150,
                0.85,
                createOptionData(100, callOI, 15.2),
                createOptionData(80, putOI, 14.8),
                ZonedDateTime.now());
    }

    private OptionChainStrike.OptionData createOptionData(double ltp, long oi, double iv) {
        return new OptionChainStrike.OptionData(
                "NSE_FO|12345",
                new OptionChainStrike.MarketData(ltp, 95, 100000, oi, 99, 100, 101, 100, oi - 1000),
                new OptionChainStrike.OptionGreeks(0.55, 0.02, -5.0, 0.15, iv, 0.65),
                null);
    }
}
