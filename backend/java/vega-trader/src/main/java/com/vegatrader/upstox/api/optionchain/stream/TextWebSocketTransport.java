package com.vegatrader.upstox.api.optionchain.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Text WebSocket transport for debugging and compliance.
 * Per websocket/b2.md section 1.1.
 * 
 * @since 4.8.0
 */
public class TextWebSocketTransport implements OptionChainTransport {

    private static final Logger logger = LoggerFactory.getLogger(TextWebSocketTransport.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void sendSnapshot(WebSocketSession session, OptionChainFeedStreamV3 data) {
        try {
            WsMessage.Snapshot msg = new WsMessage.Snapshot(
                    data.getSequenceNumber(),
                    data.getUnderlyingKey(),
                    data.getExpiry().toString(),
                    data.getStrikes(),
                    Instant.now());

            String json = objectMapper.writeValueAsString(Map.of(
                    "type", "SNAPSHOT",
                    "seq", msg.seq(),
                    "underlyingKey", msg.underlyingKey(),
                    "expiry", msg.expiry(),
                    "payload", msg.payload(),
                    "timestamp", msg.timestamp().toString()));

            session.sendMessage(new TextMessage(json));
            logger.debug("Sent text snapshot to {}", session.getId());
        } catch (IOException e) {
            logger.error("Failed to send snapshot", e);
        }
    }

    @Override
    public void sendDelta(WebSocketSession session, List<WsMessage.Delta> deltas) {
        try {
            for (WsMessage.Delta delta : deltas) {
                String json = objectMapper.writeValueAsString(Map.of(
                        "type", "DELTA",
                        "seq", delta.seq(),
                        "strike", delta.strike(),
                        "leg", delta.leg(),
                        "instrumentKey", delta.instrumentKey(),
                        "fields", delta.fields(),
                        "timestamp", delta.timestamp().toString()));

                session.sendMessage(new TextMessage(json));
            }

            logger.debug("Sent {} text deltas to {}", deltas.size(), session.getId());
        } catch (IOException e) {
            logger.error("Failed to send delta", e);
        }
    }

    @Override
    public void sendPing(WebSocketSession session, long timestamp) {
        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "type", "PING",
                    "ts", timestamp));

            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            logger.error("Failed to send ping", e);
        }
    }

    @Override
    public TransportMode getMode() {
        return TransportMode.WS_TEXT;
    }
}
