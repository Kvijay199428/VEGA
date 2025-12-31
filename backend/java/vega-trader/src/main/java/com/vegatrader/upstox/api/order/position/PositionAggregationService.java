package com.vegatrader.upstox.api.order.position;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vegatrader.upstox.auth.entity.UpstoxTokenEntity;
import com.vegatrader.upstox.auth.service.TokenStorageService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Position Aggregation Service.
 * Aggregates positions across brokers and provides real-time position data.
 * Per a1.md Section 8 - Position Awareness.
 * 
 * @since 5.0.0
 */
@Service
public class PositionAggregationService {

    private static final Logger logger = LoggerFactory.getLogger(PositionAggregationService.class);
    private static final String UPSTOX_POSITIONS_URL = "https://api.upstox.com/v2/portfolio/short-term-positions";

    // In-memory position cache (keyed by userId|instrumentToken)
    private final Map<String, Position> positionCache = new ConcurrentHashMap<>();

    private final TokenStorageService tokenStorageService;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public PositionAggregationService(TokenStorageService tokenStorageService,
            OkHttpClient httpClient,
            ObjectMapper objectMapper) {
        this.tokenStorageService = tokenStorageService;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Get all positions for a user.
     */
    public List<Position> getPositions(String userId) {
        return positionCache.values().stream()
                .filter(p -> p.userId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * Get position for a specific instrument.
     */
    public Optional<Position> getPosition(String userId, String instrumentToken) {
        String key = buildKey(userId, instrumentToken);
        return Optional.ofNullable(positionCache.get(key));
    }

    /**
     * Get net quantity for instrument.
     */
    public int getNetQuantity(String userId, String instrumentToken) {
        return getPosition(userId, instrumentToken)
                .map(Position::quantity)
                .orElse(0);
    }

    /**
     * Refresh positions from broker.
     */
    public void refreshPositions(String userId) {
        logger.info("Refreshing positions for user: {}", userId);
        try {
            String token = getActiveToken();
            Request request = new Request.Builder()
                    .url(UPSTOX_POSITIONS_URL)
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonNode root = objectMapper.readTree(response.body().string());
                    JsonNode data = root.path("data");

                    if (data.isArray()) {
                        for (JsonNode pos : data) {
                            Position position = parsePosition(userId, pos);
                            if (position != null) {
                                positionCache.put(buildKey(userId, position.instrumentToken()), position);
                            }
                        }
                    }
                    logger.info("Refreshed {} positions for {}", positionCache.size(), userId);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to refresh positions: {}", e.getMessage());
        }
    }

    /**
     * Update position from trade execution.
     */
    public void updateFromTrade(String userId, String instrumentToken, String symbol,
            String exchange, String segment, String product,
            String side, int quantity, BigDecimal price) {
        String key = buildKey(userId, instrumentToken);
        int signedQty = "BUY".equalsIgnoreCase(side) ? quantity : -quantity;

        Position existing = positionCache.get(key);
        if (existing == null) {
            // Create new position
            Position newPos = Position.fromFill(userId, instrumentToken, symbol,
                    exchange, segment, product, signedQty, price);
            positionCache.put(key, newPos);
            logger.info("Created new position: {} qty={}", instrumentToken, signedQty);
        } else {
            // Update existing position
            int newQty = existing.quantity() + signedQty;
            int newBuyQty = existing.buyQuantity() + (signedQty > 0 ? quantity : 0);
            int newSellQty = existing.sellQuantity() + (signedQty < 0 ? quantity : 0);

            BigDecimal newAvgBuy = signedQty > 0
                    ? calculateNewAverage(existing.averageBuyPrice(), existing.buyQuantity(), price, quantity)
                    : existing.averageBuyPrice();
            BigDecimal newAvgSell = signedQty < 0
                    ? calculateNewAverage(existing.averageSellPrice(), existing.sellQuantity(), price, quantity)
                    : existing.averageSellPrice();

            Position updated = new Position(
                    userId, instrumentToken, existing.symbol(), exchange, segment, product,
                    newQty, newBuyQty, newSellQty, newAvgBuy, newAvgSell,
                    price, existing.realizedPnl(), existing.unrealizedPnl(),
                    existing.dayChange(), existing.dayChangePercent(), Instant.now());
            positionCache.put(key, updated);
            logger.info("Updated position: {} qty={}", instrumentToken, newQty);
        }
    }

    /**
     * Get total exposure for user.
     */
    public BigDecimal getTotalExposure(String userId) {
        return getPositions(userId).stream()
                .map(Position::getNetValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get total P&L for user.
     */
    public BigDecimal getTotalPnl(String userId) {
        return getPositions(userId).stream()
                .map(Position::getTotalPnl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Clear cache for user.
     */
    public void clearCache(String userId) {
        positionCache.entrySet().removeIf(e -> e.getKey().startsWith(userId + "|"));
        logger.info("Cleared position cache for {}", userId);
    }

    // === Private Helpers ===

    private String buildKey(String userId, String instrumentToken) {
        return userId + "|" + instrumentToken;
    }

    private String getActiveToken() {
        Optional<UpstoxTokenEntity> tokenEntity = tokenStorageService.getToken("UPSTOX");
        if (tokenEntity.isPresent() && tokenEntity.get().isActive()) {
            return tokenEntity.get().getAccessToken();
        }
        throw new RuntimeException("No active Upstox token");
    }

    private Position parsePosition(String userId, JsonNode node) {
        try {
            return new Position(
                    userId,
                    node.path("instrument_token").asText(),
                    node.path("tradingsymbol").asText(),
                    node.path("exchange").asText(),
                    node.path("segment").asText("FO"),
                    node.path("product").asText(),
                    node.path("quantity").asInt(),
                    node.path("buy_quantity").asInt(),
                    node.path("sell_quantity").asInt(),
                    BigDecimal.valueOf(node.path("buy_price").asDouble()),
                    BigDecimal.valueOf(node.path("sell_price").asDouble()),
                    BigDecimal.valueOf(node.path("last_price").asDouble()),
                    BigDecimal.valueOf(node.path("realised").asDouble()),
                    BigDecimal.valueOf(node.path("unrealised").asDouble()),
                    BigDecimal.valueOf(node.path("day_change").asDouble()),
                    BigDecimal.valueOf(node.path("day_change_percentage").asDouble()),
                    Instant.now());
        } catch (Exception e) {
            logger.error("Failed to parse position: {}", e.getMessage());
            return null;
        }
    }

    private BigDecimal calculateNewAverage(BigDecimal oldAvg, int oldQty, BigDecimal newPrice, int newQty) {
        if (oldQty + newQty == 0)
            return BigDecimal.ZERO;
        BigDecimal totalValue = oldAvg.multiply(BigDecimal.valueOf(oldQty))
                .add(newPrice.multiply(BigDecimal.valueOf(newQty)));
        return totalValue.divide(BigDecimal.valueOf(oldQty + newQty), 4, java.math.RoundingMode.HALF_UP);
    }
}
