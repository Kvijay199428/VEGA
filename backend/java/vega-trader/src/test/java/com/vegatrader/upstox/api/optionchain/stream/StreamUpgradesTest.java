package com.vegatrader.upstox.api.optionchain.stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for streaming upgrades per websocket/b1.md and b2.md.
 */
class StreamUpgradesTest {

    // === TransportMode Tests ===

    @Test
    @DisplayName("TransportMode: WS_BINARY is WebSocket and binary")
    void transportModeBinary() {
        assertTrue(TransportMode.WS_BINARY.isWebSocket());
        assertTrue(TransportMode.WS_BINARY.isBinary());
    }

    @Test
    @DisplayName("TransportMode: WS_TEXT is WebSocket but not binary")
    void transportModeText() {
        assertTrue(TransportMode.WS_TEXT.isWebSocket());
        assertFalse(TransportMode.WS_TEXT.isBinary());
    }

    @Test
    @DisplayName("TransportMode: INTERNAL_BUS is not WebSocket")
    void transportModeInternal() {
        assertFalse(TransportMode.INTERNAL_BUS.isWebSocket());
        assertFalse(TransportMode.INTERNAL_BUS.isBinary());
    }

    // === StreamSettings Tests ===

    @Test
    @DisplayName("StreamSettings: defaults use WS_BINARY")
    void streamSettingsDefaults() {
        var settings = StreamSettings.defaults();

        assertEquals(TransportMode.WS_BINARY, settings.transportMode());
        assertEquals(3000, settings.heartbeatIntervalMs());
        assertTrue(settings.deltaOnly());
        assertTrue(settings.snapshotOnReconnect());
        assertTrue(settings.latencyTracking());
    }

    @Test
    @DisplayName("StreamSettings: debug uses WS_TEXT")
    void streamSettingsDebug() {
        var settings = StreamSettings.debug();

        assertEquals(TransportMode.WS_TEXT, settings.transportMode());
    }

    // === TransportFactory Tests ===

    @Test
    @DisplayName("TransportFactory: resolves binary transport")
    void transportFactoryBinary() {
        var factory = new TransportFactory();

        var transport = factory.resolve(TransportMode.WS_BINARY);

        assertNotNull(transport);
        assertEquals(TransportMode.WS_BINARY, transport.getMode());
    }

    @Test
    @DisplayName("TransportFactory: resolves text transport")
    void transportFactoryText() {
        var factory = new TransportFactory();

        var transport = factory.resolve(TransportMode.WS_TEXT);

        assertNotNull(transport);
        assertEquals(TransportMode.WS_TEXT, transport.getMode());
    }

    @Test
    @DisplayName("TransportFactory: internal bus returns null")
    void transportFactoryInternal() {
        var factory = new TransportFactory();

        var transport = factory.resolve(TransportMode.INTERNAL_BUS);

        assertNull(transport);
    }

    // === LatencyTracker Tests ===

    @Test
    @DisplayName("LatencyTracker: records latency")
    void latencyTrackerRecords() {
        var tracker = new LatencyTracker();

        var record = new LatencyTracker.LatencyRecord(
                "NSE_INDEX|Nifty 50", "2025-01-02", 24000, "CE",
                5, 2, 1, 1, 10, 19, java.time.Instant.now());

        tracker.record(record);

        var recent = tracker.getRecentRecords(10);
        assertEquals(1, recent.size());
        assertEquals(19, recent.get(0).endToEndMs());
    }

    @Test
    @DisplayName("LatencyTracker: builder tracks stages")
    void latencyTrackerBuilder() {
        var tracker = new LatencyTracker();

        var builder = tracker.builder("NSE_INDEX|Nifty 50", "2025-01-02", 24000, "CE");

        builder.brokerReceived();
        builder.adapterParsed();
        builder.feedApplied();
        builder.deltaComputed();
        builder.wsSent();
        builder.complete();

        var recent = tracker.getRecentRecords(10);
        assertEquals(1, recent.size());
        assertTrue(recent.get(0).endToEndMs() >= 0);
    }

    @Test
    @DisplayName("LatencyTracker: aggregates metrics")
    void latencyTrackerMetrics() {
        var tracker = new LatencyTracker();

        tracker.record(new LatencyTracker.LatencyRecord(
                "NSE_INDEX|Nifty 50", "2025-01-02", 24000, "CE",
                5, 2, 1, 1, 10, 20, java.time.Instant.now()));
        tracker.record(new LatencyTracker.LatencyRecord(
                "NSE_INDEX|Nifty 50", "2025-01-02", 24100, "CE",
                4, 2, 1, 1, 8, 16, java.time.Instant.now()));

        var metrics = tracker.getMetrics("NSE_INDEX|Nifty 50", "2025-01-02");

        assertNotNull(metrics);
        assertEquals(2, metrics.count());
        assertEquals(16, metrics.minMs());
        assertEquals(20, metrics.maxMs());
    }

    // === FeedMulticastDispatcher Tests ===

    @Test
    @DisplayName("FeedMulticastDispatcher: subscribe and publish")
    void multicastDispatcherPublish() {
        var dispatcher = new FeedMulticastDispatcher();
        var received = new java.util.ArrayList<WsMessage.Delta>();

        dispatcher.subscribe("TEST|2025-01-02", new FeedMulticastDispatcher.DeltaSubscriber() {
            @Override
            public void onDelta(List<WsMessage.Delta> deltas) {
                received.addAll(deltas);
            }

            @Override
            public void onSnapshot(OptionChainFeedStreamV3 snapshot) {
            }
        });

        var delta = new WsMessage.Delta(1, 24000, "CALL", "NSE_FO|12345",
                Map.of("market_data.ltp", 200.0), java.time.Instant.now());

        dispatcher.publish("TEST|2025-01-02", List.of(delta));

        assertEquals(1, received.size());
        assertEquals(24000, received.get(0).strike());
    }

    @Test
    @DisplayName("FeedMulticastDispatcher: multiple subscribers")
    void multicastDispatcherMultipleSubscribers() {
        var dispatcher = new FeedMulticastDispatcher();
        var count = new int[] { 0 };

        dispatcher.subscribe("TEST|2025-01-02", new FeedMulticastDispatcher.DeltaSubscriber() {
            @Override
            public void onDelta(List<WsMessage.Delta> deltas) {
                count[0]++;
            }

            @Override
            public void onSnapshot(OptionChainFeedStreamV3 snapshot) {
            }
        });

        dispatcher.subscribe("TEST|2025-01-02", new FeedMulticastDispatcher.DeltaSubscriber() {
            @Override
            public void onDelta(List<WsMessage.Delta> deltas) {
                count[0]++;
            }

            @Override
            public void onSnapshot(OptionChainFeedStreamV3 snapshot) {
            }
        });

        assertEquals(2, dispatcher.getSubscriberCount("TEST|2025-01-02"));

        dispatcher.publish("TEST|2025-01-02", List.of(
                new WsMessage.Delta(1, 24000, "CALL", "NSE_FO|12345",
                        Map.of("ltp", 200.0), java.time.Instant.now())));

        assertEquals(2, count[0]); // Both subscribers received
    }

    @Test
    @DisplayName("FeedMulticastDispatcher: unsubscribe")
    void multicastDispatcherUnsubscribe() {
        var dispatcher = new FeedMulticastDispatcher();

        var subscriber = new FeedMulticastDispatcher.DeltaSubscriber() {
            @Override
            public void onDelta(List<WsMessage.Delta> deltas) {
            }

            @Override
            public void onSnapshot(OptionChainFeedStreamV3 snapshot) {
            }
        };

        dispatcher.subscribe("TEST|2025-01-02", subscriber);
        assertEquals(1, dispatcher.getSubscriberCount("TEST|2025-01-02"));

        dispatcher.unsubscribe("TEST|2025-01-02", subscriber);
        assertEquals(0, dispatcher.getSubscriberCount("TEST|2025-01-02"));
    }
}
