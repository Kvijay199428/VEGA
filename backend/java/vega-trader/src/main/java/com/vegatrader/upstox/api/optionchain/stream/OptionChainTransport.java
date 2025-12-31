package com.vegatrader.upstox.api.optionchain.stream;

import org.springframework.web.socket.WebSocketSession;
import java.util.List;

/**
 * Transport interface for option chain streaming.
 * Per websocket/b2.md section 1.3.
 * 
 * Implementations handle the actual message serialization and sending.
 * 
 * @since 4.8.0
 */
public interface OptionChainTransport {

    /**
     * Send full snapshot to a session.
     */
    void sendSnapshot(WebSocketSession session, OptionChainFeedStreamV3 data);

    /**
     * Send delta updates to a session.
     */
    void sendDelta(WebSocketSession session, List<WsMessage.Delta> deltas);

    /**
     * Send heartbeat ping.
     */
    void sendPing(WebSocketSession session, long timestamp);

    /**
     * Get transport mode.
     */
    TransportMode getMode();
}
