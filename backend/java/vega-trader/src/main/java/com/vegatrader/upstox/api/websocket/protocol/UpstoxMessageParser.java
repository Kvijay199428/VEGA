package com.vegatrader.upstox.api.websocket.protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vegatrader.upstox.api.response.websocket.MarketDataFeedV3Response;
import com.vegatrader.upstox.api.websocket.MarketUpdateV3;
import com.vegatrader.upstox.api.websocket.event.HeartbeatEvent;
import com.vegatrader.upstox.api.websocket.event.MarketUpdateEvent;
import com.vegatrader.upstox.api.websocket.event.UnknownEvent;
import com.vegatrader.upstox.api.websocket.event.UpstoxErrorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Defensive message parser for Upstox WebSocket messages.
 * 
 * <p>
 * Key features:
 * <ul>
 * <li>Type-based message routing</li>
 * <li>Unknown message type handling</li>
 * <li>Schema drift resilience</li>
 * <li>JSON parsing error recovery</li>
 * </ul>
 * 
 * <p>
 * This ensures:
 * <ul>
 * <li>Unknown events don't break the stream</li>
 * <li>Schema changes are logged and monitored</li>
 * <li>Forward compatibility with Upstox V4</li>
 * </ul>
 * 
 * @since 3.1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpstoxMessageParser {

    private static final Logger logger = LoggerFactory.getLogger(UpstoxMessageParser.class);

    private final ObjectMapper mapper;

    public UpstoxMessageParser() {
        this.mapper = new ObjectMapper();
        // Configure to ignore unknown properties
        mapper.configure(
                com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
    }

    /**
     * Parses a JSON message into a typed event.
     * 
     * <p>
     * Message type routing:
     * <ul>
     * <li>"live_feed" → MarketUpdateV3</li>
     * <li>"heartbeat" → HeartbeatEvent</li>
     * <li>"error" → UpstoxErrorEvent</li>
     * <li>unknown → UnknownEvent (logged, not dropped)</li>
     * </ul>
     * 
     * @param json the raw JSON message
     * @return the parsed event
     * @throws IOException if JSON parsing fails completely
     */
    public MarketUpdateEvent parse(String json) throws IOException {
        try {
            JsonNode root = mapper.readTree(json);
            String type = root.path("type").asText("");

            return switch (type) {
                case "live_feed", "initial_feed" -> parseMarketUpdate(json);
                case "heartbeat" -> parseHeartbeat(root);
                case "error" -> parseError(root);
                case "" -> {
                    logger.warn("Message missing 'type' field: {}",
                            json.substring(0, Math.min(100, json.length())));
                    yield new UnknownEvent("no_type", json);
                }
                default -> {
                    logger.info("Unknown message type '{}', capturing for monitoring", type);
                    yield new UnknownEvent(type, json);
                }
            };
        } catch (IOException e) {
            logger.error("Failed to parse JSON message: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Parses a market update message.
     * 
     * @param json the JSON string
     * @return MarketUpdateV3 event
     * @throws IOException if parsing fails
     */
    private MarketUpdateEvent parseMarketUpdate(String json) throws IOException {
        try {
            // Parse JSON into MarketDataFeedV3Response
            MarketDataFeedV3Response response = mapper.readValue(json, MarketDataFeedV3Response.class);

            // Wrap in MarketUpdateV3 and return
            return new MarketUpdateV3(response);

        } catch (IOException e) {
            logger.error("Failed to parse market update: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Parses a heartbeat message.
     * 
     * @param root the JSON root node
     * @return HeartbeatEvent
     */
    private HeartbeatEvent parseHeartbeat(JsonNode root) {
        long timestamp = root.path("timestamp").asLong(System.currentTimeMillis());
        return new HeartbeatEvent(timestamp);
    }

    /**
     * Parses an error message.
     * 
     * @param root the JSON root node
     * @return UpstoxErrorEvent
     */
    private UpstoxErrorEvent parseError(JsonNode root) {
        String code = root.path("code").asText("UNKNOWN_ERROR");
        String message = root.path("message").asText("No error message");
        return new UpstoxErrorEvent(code, message);
    }

    /**
     * Parses a Protobuf FeedResponse into a typed event.
     * 
     * <p>
     * This method converts the binary Protobuf message into our internal
     * DTO structure using MarketDataProtoMapper, then wraps it in a
     * MarketUpdateV3 event.
     * 
     * @param response the Protobuf FeedResponse
     * @return the parsed MarketUpdateEvent
     */
    public MarketUpdateEvent parse(
            com.upstox.marketdatafeederv3udapi.rpc.proto.MarketDataFeedV3.FeedResponse response) {
        try {
            // Use the mapper to convert Protobuf to DTO
            MarketDataFeedV3Response dto = MarketDataProtoMapper.mapResponse(response);

            // Wrap in MarketUpdateV3 and return
            return new MarketUpdateV3(dto);

        } catch (Exception e) {
            logger.error("Failed to parse Protobuf FeedResponse: {}", e.getMessage());
            return new UnknownEvent("protobuf_parse_error", e.getMessage());
        }
    }
}
