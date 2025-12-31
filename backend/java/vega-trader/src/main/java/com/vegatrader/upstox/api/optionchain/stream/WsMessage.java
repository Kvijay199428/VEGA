package com.vegatrader.upstox.api.optionchain.stream;

import java.time.Instant;
import java.util.Map;

/**
 * WebSocket message types for option chain streaming.
 * Per websocket/a1.md section 5.
 * 
 * @since 4.8.0
 */
public sealed interface WsMessage {

    /**
     * Snapshot message - sent once per subscription.
     * Frontend must not render deltas before snapshot.
     */
    record Snapshot(
            long seq,
            String underlyingKey,
            String expiry,
            Map<Integer, OptionChainFeedStreamV3.StrikeNode> payload,
            Instant timestamp) implements WsMessage {
        public String type() {
            return "SNAPSHOT";
        }
    }

    /**
     * Delta message - only changed fields transmitted.
     */
    record Delta(
            long seq,
            int strike,
            String leg, // "CALL" or "PUT"
            String instrumentKey,
            Map<String, Object> fields, // e.g., "market_data.ltp": 2449.9
            Instant timestamp) implements WsMessage {
        public String type() {
            return "DELTA";
        }
    }

    /**
     * Heartbeat ping - server to client.
     */
    record Ping(
            long ts) implements WsMessage {
        public String type() {
            return "PING";
        }
    }

    /**
     * Heartbeat pong - client to server.
     */
    record Pong(
            long ts) implements WsMessage {
        public String type() {
            return "PONG";
        }
    }

    /**
     * Error message.
     */
    record Error(
            String code,
            String message,
            Instant timestamp) implements WsMessage {
        public String type() {
            return "ERROR";
        }
    }

    /**
     * Subscription confirmation.
     */
    record Subscribed(
            String underlyingKey,
            String expiry,
            long initialSeq,
            Instant timestamp) implements WsMessage {
        public String type() {
            return "SUBSCRIBED";
        }
    }

    /**
     * Unsubscription confirmation.
     */
    record Unsubscribed(
            String underlyingKey,
            String expiry,
            Instant timestamp) implements WsMessage {
        public String type() {
            return "UNSUBSCRIBED";
        }
    }
}
