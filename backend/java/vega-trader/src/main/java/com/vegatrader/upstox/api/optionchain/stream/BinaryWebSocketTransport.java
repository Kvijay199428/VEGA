package com.vegatrader.upstox.api.optionchain.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;

/**
 * Binary WebSocket transport for production use.
 * Per websocket/b2.md section 2.
 * 
 * Frame structure: | seq (8B) | eventType (1B) | payloadLength (4B) | payload |
 * 
 * @since 4.8.0
 */
public class BinaryWebSocketTransport implements OptionChainTransport {

    private static final Logger logger = LoggerFactory.getLogger(BinaryWebSocketTransport.class);

    // Event type bytes
    private static final byte EVENT_SNAPSHOT = 0x01;
    private static final byte EVENT_DELTA = 0x02;
    private static final byte EVENT_HEARTBEAT = 0x03;
    private static final byte EVENT_STRIKE_DISABLED = 0x04;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void sendSnapshot(WebSocketSession session, OptionChainFeedStreamV3 data) {
        try {
            byte[] payload = objectMapper.writeValueAsBytes(data.getStrikes());
            ByteBuffer buffer = createFrame(data.getSequenceNumber(), EVENT_SNAPSHOT, payload);
            session.sendMessage(new BinaryMessage(buffer));

            logger.debug("Sent binary snapshot to {}: {} bytes", session.getId(), buffer.capacity());
        } catch (IOException e) {
            logger.error("Failed to send snapshot", e);
        }
    }

    @Override
    public void sendDelta(WebSocketSession session, List<WsMessage.Delta> deltas) {
        try {
            for (WsMessage.Delta delta : deltas) {
                byte[] payload = objectMapper.writeValueAsBytes(delta);
                ByteBuffer buffer = createFrame(delta.seq(), EVENT_DELTA, payload);
                session.sendMessage(new BinaryMessage(buffer));
            }

            logger.debug("Sent {} binary deltas to {}", deltas.size(), session.getId());
        } catch (IOException e) {
            logger.error("Failed to send delta", e);
        }
    }

    @Override
    public void sendPing(WebSocketSession session, long timestamp) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(13); // 8 + 1 + 4 + 0
            buffer.putLong(timestamp);
            buffer.put(EVENT_HEARTBEAT);
            buffer.putInt(0);
            buffer.flip();

            session.sendMessage(new BinaryMessage(buffer));
        } catch (IOException e) {
            logger.error("Failed to send ping", e);
        }
    }

    @Override
    public TransportMode getMode() {
        return TransportMode.WS_BINARY;
    }

    /**
     * Create a binary frame.
     */
    private ByteBuffer createFrame(long seq, byte eventType, byte[] payload) {
        ByteBuffer buffer = ByteBuffer.allocate(13 + payload.length);
        buffer.putLong(seq);
        buffer.put(eventType);
        buffer.putInt(payload.length);
        buffer.put(payload);
        buffer.flip();
        return buffer;
    }

    /**
     * Parse event type from binary frame.
     */
    public static byte parseEventType(ByteBuffer buffer) {
        buffer.position(8); // Skip seq
        return buffer.get();
    }

    /**
     * Parse sequence from binary frame.
     */
    public static long parseSequence(ByteBuffer buffer) {
        buffer.position(0);
        return buffer.getLong();
    }
}
