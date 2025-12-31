package com.vegatrader.upstox.api.optionchain.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Transport factory - runtime-selectable transport.
 * Per websocket/b2.md section 1.3.
 * 
 * Enables A/B testing without redeploying.
 * 
 * @since 4.8.0
 */
@Component
public class TransportFactory {

    private static final Logger logger = LoggerFactory.getLogger(TransportFactory.class);

    private final BinaryWebSocketTransport binaryTransport = new BinaryWebSocketTransport();
    private final TextWebSocketTransport textTransport = new TextWebSocketTransport();

    /**
     * Resolve transport based on settings.
     */
    public OptionChainTransport resolve(StreamSettings settings) {
        return resolve(settings.transportMode());
    }

    /**
     * Resolve transport by mode.
     */
    public OptionChainTransport resolve(TransportMode mode) {
        return switch (mode) {
            case WS_BINARY -> {
                logger.debug("Using binary WebSocket transport");
                yield binaryTransport;
            }
            case WS_TEXT -> {
                logger.debug("Using text WebSocket transport");
                yield textTransport;
            }
            case HTTP_LONG_POLL -> {
                logger.debug("Using HTTP long poll transport (fallback to text)");
                yield textTransport; // Fallback
            }
            case INTERNAL_BUS -> {
                logger.debug("Using internal bus (no network)");
                yield null; // Internal only
            }
        };
    }

    /**
     * Get default production transport.
     */
    public OptionChainTransport getDefault() {
        return binaryTransport;
    }

    /**
     * Get debug transport.
     */
    public OptionChainTransport getDebug() {
        return textTransport;
    }
}
