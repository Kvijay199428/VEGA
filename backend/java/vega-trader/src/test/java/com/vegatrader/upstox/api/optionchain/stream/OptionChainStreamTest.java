package com.vegatrader.upstox.api.optionchain.stream;

import com.vegatrader.analytics.valuation.OptionChainValuationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Option Chain WebSocket Streaming per websocket/a1.md.
 */
class OptionChainStreamTest {

    private OptionChainStreamManager manager;
    private OptionChainValuationService valuationService;

    @BeforeEach
    void setUp() {
        valuationService = new OptionChainValuationService();
        manager = new OptionChainStreamManager(valuationService);
    }

    // === OptionChainFeedStreamV3 Tests ===

    @Test
    @DisplayName("FeedStreamV3: create and get stream key")
    void feedStreamCreateAndGetKey() {
        var stream = new OptionChainFeedStreamV3("NSE_INDEX|Nifty 50", LocalDate.of(2025, 1, 2));

        assertEquals("NSE_INDEX|Nifty 50", stream.getUnderlyingKey());
        assertEquals(LocalDate.of(2025, 1, 2), stream.getExpiry());
        assertEquals("NSE_INDEX|Nifty 50|2025-01-02", stream.getStreamKey());
        assertEquals(0, stream.getSequenceNumber());
    }

    @Test
    @DisplayName("FeedStreamV3: update strike increments sequence")
    void feedStreamUpdateStrike() {
        var stream = new OptionChainFeedStreamV3("NSE_INDEX|Nifty 50", LocalDate.of(2025, 1, 2));

        var node = new OptionChainFeedStreamV3.StrikeNode(24000, null, null);
        long seq1 = stream.updateStrike(24000, node);
        long seq2 = stream.updateStrike(24100, node);

        assertEquals(1, seq1);
        assertEquals(2, seq2);
        assertEquals(2, stream.getStrikes().size());
    }

    @Test
    @DisplayName("FeedStreamV3: load snapshot replaces all")
    void feedStreamLoadSnapshot() {
        var stream = new OptionChainFeedStreamV3("NSE_INDEX|Nifty 50", LocalDate.of(2025, 1, 2));

        // Add initial
        stream.updateStrike(24000, new OptionChainFeedStreamV3.StrikeNode(24000, null, null));

        // Load snapshot with different strikes
        Map<Integer, OptionChainFeedStreamV3.StrikeNode> snapshot = Map.of(
                23500, new OptionChainFeedStreamV3.StrikeNode(23500, null, null),
                23600, new OptionChainFeedStreamV3.StrikeNode(23600, null, null));
        stream.loadSnapshot(snapshot);

        assertEquals(2, stream.getStrikes().size());
        assertFalse(stream.getStrikes().containsKey(24000));
        assertTrue(stream.getStrikes().containsKey(23500));
    }

    // === DeltaDetector Tests ===

    @Test
    @DisplayName("DeltaDetector: first update returns all fields")
    void deltaDetectorFirstUpdate() {
        var detector = new DeltaDetector();

        var marketData = new OptionChainFeedStreamV3.MarketData(
                200.5, 195.0, 100000, 5000000, 200.0, 100, 201.0, 100, 4800000);
        var greeks = new OptionChainFeedStreamV3.OptionGreeks(
                0.55, 0.02, -5.0, 0.15, 14.5, 0.52);
        var leg = new OptionChainFeedStreamV3.OptionLeg("NSE_FO|12345", marketData, greeks, null);

        var delta = detector.detectDelta(1, 24000, "CALL", "NSE_FO|12345", leg);

        assertTrue(delta.isPresent());
        var fields = delta.get().fields();
        assertTrue(fields.containsKey("market_data.ltp"));
        assertTrue(fields.containsKey("greeks.iv"));
    }

    @Test
    @DisplayName("DeltaDetector: no changes returns empty")
    void deltaDetectorNoChanges() {
        var detector = new DeltaDetector();

        var marketData = new OptionChainFeedStreamV3.MarketData(
                200.5, 195.0, 100000, 5000000, 200.0, 100, 201.0, 100, 4800000);
        var greeks = new OptionChainFeedStreamV3.OptionGreeks(
                0.55, 0.02, -5.0, 0.15, 14.5, 0.52);
        var leg = new OptionChainFeedStreamV3.OptionLeg("NSE_FO|12345", marketData, greeks, null);

        // First update
        detector.detectDelta(1, 24000, "CALL", "NSE_FO|12345", leg);

        // Same values - should be empty
        var delta = detector.detectDelta(2, 24000, "CALL", "NSE_FO|12345", leg);

        assertTrue(delta.isEmpty());
    }

    @Test
    @DisplayName("DeltaDetector: only changed fields returned")
    void deltaDetectorChangedFields() {
        var detector = new DeltaDetector();

        var marketData1 = new OptionChainFeedStreamV3.MarketData(
                200.5, 195.0, 100000, 5000000, 200.0, 100, 201.0, 100, 4800000);
        var greeks1 = new OptionChainFeedStreamV3.OptionGreeks(
                0.55, 0.02, -5.0, 0.15, 14.5, 0.52);
        var leg1 = new OptionChainFeedStreamV3.OptionLeg("NSE_FO|12345", marketData1, greeks1, null);

        // First update
        detector.detectDelta(1, 24000, "CALL", "NSE_FO|12345", leg1);

        // Only LTP and volume changed
        var marketData2 = new OptionChainFeedStreamV3.MarketData(
                201.5, 195.0, 100500, 5000000, 200.0, 100, 201.0, 100, 4800000);
        var leg2 = new OptionChainFeedStreamV3.OptionLeg("NSE_FO|12345", marketData2, greeks1, null);

        var delta = detector.detectDelta(2, 24000, "CALL", "NSE_FO|12345", leg2);

        assertTrue(delta.isPresent());
        var fields = delta.get().fields();
        assertEquals(2, fields.size());
        assertTrue(fields.containsKey("market_data.ltp"));
        assertTrue(fields.containsKey("market_data.volume"));
    }

    // === StreamManager Tests ===

    @Test
    @DisplayName("StreamManager: get or create stream")
    void streamManagerGetOrCreate() {
        // manager is initialized in setUp

        var stream1 = manager.getOrCreateStream("NSE_INDEX|Nifty 50", "2025-01-02");
        var stream2 = manager.getOrCreateStream("NSE_INDEX|Nifty 50", "2025-01-02");

        assertSame(stream1, stream2);
        assertEquals(1, manager.getActiveStreamCount());
    }

    @Test
    @DisplayName("StreamManager: different expiries are separate")
    void streamManagerDifferentExpiries() {
        var stream1 = manager.getOrCreateStream("NSE_INDEX|Nifty 50", "2025-01-02");
        var stream2 = manager.getOrCreateStream("NSE_INDEX|Nifty 50", "2025-01-09");

        assertNotSame(stream1, stream2);
        assertEquals(2, manager.getActiveStreamCount());
    }

    // === OptionChainFeeder Tests ===

    @Test
    @DisplayName("Feeder: transforms to UI rows")
    void feederTransformsToRows() {
        var stream = new OptionChainFeedStreamV3("NSE_INDEX|Nifty 50", LocalDate.of(2025, 1, 2));

        var callMd = new OptionChainFeedStreamV3.MarketData(200, 195, 100000, 5000000, 199, 100, 201, 100, 4800000);
        var putMd = new OptionChainFeedStreamV3.MarketData(50, 55, 80000, 4000000, 49, 100, 51, 100, 3900000);
        var callGreeks = new OptionChainFeedStreamV3.OptionGreeks(0.55, 0.02, -5, 0.15, 14.5, 0.52);
        var putGreeks = new OptionChainFeedStreamV3.OptionGreeks(-0.45, 0.02, -4, 0.12, 15.0, 0.48);

        var callLeg = new OptionChainFeedStreamV3.OptionLeg("CE", callMd, callGreeks, null);
        var putLeg = new OptionChainFeedStreamV3.OptionLeg("PE", putMd, putGreeks, null);

        stream.updateStrike(24000, new OptionChainFeedStreamV3.StrikeNode(24000, callLeg, putLeg));

        var feeder = new OptionChainFeeder(stream, 24050);
        var rows = feeder.toFeederRows();

        assertEquals(1, rows.size());
        assertEquals(24000, rows.get(0).strike());
        assertEquals(200, rows.get(0).call().ltp());
        assertEquals(50, rows.get(0).put().ltp());
    }

    @Test
    @DisplayName("Feeder: calculates moneyness")
    void feederCalculatesMoneyness() {
        var stream = new OptionChainFeedStreamV3("NSE_INDEX|Nifty 50", LocalDate.of(2025, 1, 2));

        // ATM strike
        stream.updateStrike(24050, new OptionChainFeedStreamV3.StrikeNode(24050,
                createLeg(200), createLeg(50)));

        // ITM call
        stream.updateStrike(23500, new OptionChainFeedStreamV3.StrikeNode(23500,
                createLeg(550), createLeg(5)));

        var feeder = new OptionChainFeeder(stream, 24050);
        var rows = feeder.toFeederRows();

        assertEquals(2, rows.size());
    }

    private OptionChainFeedStreamV3.OptionLeg createLeg(double ltp) {
        return new OptionChainFeedStreamV3.OptionLeg(
                "NSE_FO|12345",
                new OptionChainFeedStreamV3.MarketData(ltp, ltp - 5, 100000, 5000000, ltp - 0.5, 100, ltp + 0.5, 100,
                        4800000),
                new OptionChainFeedStreamV3.OptionGreeks(0.5, 0.02, -5, 0.15, 14.5, 0.5),
                null);
    }
}
