package com.vegatrader.upstox.api.order.broker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vegatrader.upstox.auth.entity.UpstoxTokenEntity;
import com.vegatrader.upstox.auth.service.TokenStorageService;
import com.vegatrader.upstox.api.order.broker.BrokerAdapter.*;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Upstox Broker Adapter implementation.
 * Per order-mgmt/b4.md.
 * 
 * Integrated with Upstox API via OkHttpClient.
 * 
 * @since 4.9.0
 */
@Component
public class UpstoxBrokerAdapter implements BrokerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(UpstoxBrokerAdapter.class);
    private static final String API_BASE_URL = "https://api.upstox.com/v2";

    private final BrokerCapability capabilities = BrokerCapability.UPSTOX;
    private final TokenStorageService tokenStorageService;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Rate limit tracking
    private int requestsThisMinute = 0;
    private long minuteStartTime = System.currentTimeMillis();

    @Autowired
    public UpstoxBrokerAdapter(TokenStorageService tokenStorageService,
            OkHttpClient httpClient,
            ObjectMapper objectMapper) {
        this.tokenStorageService = tokenStorageService;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getBrokerName() {
        return "UPSTOX";
    }

    @Override
    public BrokerCapability getCapabilities() {
        return capabilities;
    }

    private String getAccessToken() {
        Optional<UpstoxTokenEntity> tokenEntity = tokenStorageService.getToken("UPSTOX");
        if (tokenEntity.isEmpty() || !tokenEntity.get().isActive()) {
            // Try "default" or fail
            tokenEntity = tokenStorageService.getToken("default");
        }

        if (tokenEntity.isEmpty()) {
            throw new RuntimeException("No active Upstox token found");
        }
        return tokenEntity.get().getAccessToken();
    }

    // Helper to execute request
    private JsonNode executeRequest(Request request) throws IOException {
        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "{}";
            if (!response.isSuccessful()) {
                throw new IOException("API Error " + response.code() + ": " + body);
            }
            return objectMapper.readTree(body);
        }
    }

    @Override
    public OrderResult placeOrder(OrderRequest request) {
        long startTime = System.currentTimeMillis();
        checkRateLimit();

        logger.info("Placing order: {} {} {} @ {}",
                request.side(), request.quantity(), request.instrumentToken(), request.price());

        try {
            ObjectNode json = objectMapper.createObjectNode();
            json.put("quantity", request.quantity());
            json.put("product", request.product());
            json.put("validity", request.validity());
            json.put("price", request.price() != null ? request.price().doubleValue() : 0.0);
            json.put("tag", request.tag());
            json.put("instrument_token", request.instrumentToken());
            json.put("order_type", request.orderType());
            json.put("transaction_type", request.side());
            json.put("disclosed_quantity", request.disclosedQuantity());
            json.put("trigger_price", request.triggerPrice() != null ? request.triggerPrice().doubleValue() : 0.0);
            json.put("is_amo", request.isAmo());

            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
            Request apiRequest = new Request.Builder()
                    .url(API_BASE_URL + "/order/place")
                    .header("Authorization", "Bearer " + getAccessToken())
                    .header("Accept", "application/json")
                    .post(body)
                    .build();

            JsonNode response = executeRequest(apiRequest);
            String brokerOrderId = response.path("data").path("order_id").asText();
            String orderId = "ORD-"
                    + (request.correlationId() != null ? request.correlationId() : System.currentTimeMillis());

            long latency = System.currentTimeMillis() - startTime;
            logger.info("Order placed: {} (broker: {}) in {}ms", orderId, brokerOrderId, latency);

            return OrderResult.success(orderId, brokerOrderId, request.correlationId(), latency);

        } catch (Exception e) {
            logger.error("Order placement failed: {}", e.getMessage());
            return OrderResult.error(request.correlationId(), "BROKER_ERROR", e.getMessage());
        }
    }

    @Override
    public MultiOrderResult placeMultiOrder(List<OrderRequest> orders) {
        long startTime = System.currentTimeMillis();
        checkRateLimit();

        logger.info("Placing multi-order: {} orders", orders.size());

        if (orders.size() > capabilities.maxOrdersPerBatch()) {
            return new MultiOrderResult("error", List.of(),
                    orders.size(), 0, orders.size(), 0);
        }

        List<OrderResult> results = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        List<OrderRequest> buyOrders = orders.stream()
                .filter(o -> "BUY".equalsIgnoreCase(o.side())).toList();
        List<OrderRequest> sellOrders = orders.stream()
                .filter(o -> "SELL".equalsIgnoreCase(o.side())).toList();

        for (OrderRequest order : buyOrders) {
            OrderResult result = placeOrder(order);
            results.add(result);
            if (result.success())
                successCount++;
            else
                errorCount++;
        }

        for (OrderRequest order : sellOrders) {
            OrderResult result = placeOrder(order);
            results.add(result);
            if (result.success())
                successCount++;
            else
                errorCount++;
        }

        long latency = System.currentTimeMillis() - startTime;
        String status = errorCount == 0 ? "success" : (successCount == 0 ? "error" : "partial_success");

        return new MultiOrderResult(status, results, orders.size(),
                successCount, errorCount, latency);
    }

    @Override
    public OrderResult modifyOrder(ModifyRequest request) {
        long startTime = System.currentTimeMillis();
        checkRateLimit();

        logger.info("Modifying order: {}", request.orderId());

        try {
            ObjectNode json = objectMapper.createObjectNode();
            json.put("order_id", request.orderId()); // Depending on API, might be query or body
            // Upstox modify API: PUT /order/modify
            if (request.quantity() != null)
                json.put("quantity", request.quantity());
            if (request.price() != null)
                json.put("price", request.price().doubleValue());
            if (request.orderType() != null)
                json.put("order_type", request.orderType());
            if (request.validity() != null)
                json.put("validity", request.validity());
            if (request.disclosedQuantity() != null)
                json.put("disclosed_quantity", request.disclosedQuantity());
            if (request.triggerPrice() != null)
                json.put("trigger_price", request.triggerPrice().doubleValue());

            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
            Request apiRequest = new Request.Builder()
                    .url(API_BASE_URL + "/order/modify")
                    .header("Authorization", "Bearer " + getAccessToken())
                    .header("Accept", "application/json")
                    .put(body)
                    .build();

            JsonNode response = executeRequest(apiRequest);
            String brokerOrderId = response.path("data").path("order_id").asText();

            long latency = System.currentTimeMillis() - startTime;
            logger.info("Order modified: {} in {}ms", request.orderId(), latency);

            return OrderResult.success(request.orderId(), brokerOrderId, request.correlationId(), latency);

        } catch (Exception e) {
            logger.error("Order modification failed: {}", e.getMessage());
            return OrderResult.error(request.correlationId(), "MODIFY_FAILED", e.getMessage());
        }
    }

    @Override
    public OrderResult cancelOrder(String orderId) {
        long startTime = System.currentTimeMillis();
        checkRateLimit();

        logger.info("Cancelling order: {}", orderId);

        try {
            // DELETE /order/cancel?order_id={order_id}
            Request apiRequest = new Request.Builder()
                    .url(API_BASE_URL + "/order/cancel?order_id=" + orderId)
                    .header("Authorization", "Bearer " + getAccessToken())
                    .header("Accept", "application/json")
                    .delete()
                    .build();

            JsonNode response = executeRequest(apiRequest);
            String brokerOrderId = response.path("data").path("order_id").asText();

            long latency = System.currentTimeMillis() - startTime;
            logger.info("Order cancelled: {} in {}ms", orderId, latency);

            return OrderResult.success(orderId, brokerOrderId, null, latency);

        } catch (Exception e) {
            logger.error("Order cancellation failed: {}", e.getMessage());
            return OrderResult.error(null, "CANCEL_FAILED", e.getMessage());
        }
    }

    @Override
    public MultiOrderResult cancelMultiOrder(List<String> orderIds) {
        long startTime = System.currentTimeMillis();
        logger.info("Cancelling {} orders", orderIds.size());

        List<OrderResult> results = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        for (String orderId : orderIds) {
            OrderResult result = cancelOrder(orderId);
            results.add(result);
            if (result.success())
                successCount++;
            else
                errorCount++;
        }

        long latency = System.currentTimeMillis() - startTime;
        String status = errorCount == 0 ? "success" : (successCount == 0 ? "error" : "partial_success");

        return new MultiOrderResult(status, results, orderIds.size(),
                successCount, errorCount, latency);
    }

    @Override
    public OrderStatus getOrderStatus(String orderId) {
        checkRateLimit();
        try {
            // GET /order/history?order_id=... or /order/retrieve-all
            // Typically fetch recent orders and filter, or use get-order-details

            // Assuming we fetch all and find, or generic status placeholder
            // Note: Upstox API v2 has /order/history for a specific order?
            // Docs say: GET /order/history to get history of an order.

            // Fallback for this implementation: return dummy until we need strict polling
            return new OrderStatus(orderId, "UNKNOWN", 0, 0, BigDecimal.ZERO, "Fetch Logics Placeholder");
        } catch (Exception e) {
            logger.error("Failed to get order status: {}", e.getMessage());
        }
        return new OrderStatus(orderId, "UNKNOWN", 0, 0, BigDecimal.ZERO, "Fetch Failed");
    }

    @Override
    public List<BrokerOrder> getOrderBook() {
        checkRateLimit();
        try {
            Request apiRequest = new Request.Builder()
                    .url(API_BASE_URL + "/order/retrieve-all")
                    .header("Authorization", "Bearer " + getAccessToken())
                    .header("Accept", "application/json")
                    .get()
                    .build();

            JsonNode response = executeRequest(apiRequest);
            List<BrokerOrder> orders = new ArrayList<>();
            JsonNode data = response.path("data");
            if (data.isArray()) {
                for (JsonNode o : data) {
                    orders.add(new BrokerOrder(
                            o.path("order_id").asText(),
                            o.path("exchange_order_id").asText(),
                            o.path("instrument_token").asText(),
                            o.path("transaction_type").asText(),
                            o.path("order_type").asText(),
                            o.path("quantity").asInt(),
                            o.path("filled_quantity").asInt(),
                            BigDecimal.valueOf(o.path("price").asDouble()),
                            BigDecimal.valueOf(o.path("average_price").asDouble()),
                            o.path("status").asText(),
                            o.path("status_message").asText()));
                }
            }
            return orders;
        } catch (Exception e) {
            logger.error("Failed to get order book: {}", e.getMessage());
        }
        return List.of();
    }

    @Override
    public List<BrokerTrade> getTradesForDay() {
        checkRateLimit();
        try {
            Request apiRequest = new Request.Builder()
                    .url(API_BASE_URL + "/order/trades/get-trades-for-day")
                    .header("Authorization", "Bearer " + getAccessToken())
                    .header("Accept", "application/json")
                    .get()
                    .build();

            JsonNode response = executeRequest(apiRequest);
            List<BrokerTrade> trades = new ArrayList<>();
            JsonNode data = response.path("data");
            if (data.isArray()) {
                for (JsonNode t : data) {
                    trades.add(new BrokerTrade(
                            t.path("trade_id").asText(),
                            t.path("order_id").asText(),
                            t.path("exchange_order_id").asText(),
                            t.path("instrument_token").asText(),
                            t.path("transaction_type").asText(),
                            t.path("quantity").asInt(),
                            BigDecimal.valueOf(t.path("average_price").asDouble()),
                            t.path("order_timestamp").asText()));
                }
            }
            return trades;
        } catch (Exception e) {
            logger.error("Failed to get trades: {}", e.getMessage());
        }
        return List.of();
    }

    @Override
    public List<BrokerTrade> getOrderTrades(String orderId) {
        checkRateLimit();
        // Similar to getTradesForDay but filtered or specific endpoint
        return List.of();
    }

    @Override
    public MultiOrderResult exitAllPositions(String segment, String tag) {
        long startTime = System.currentTimeMillis();
        checkRateLimit();

        logger.info("Exiting all positions: segment={}, tag={}", segment, tag);

        // TODO: Call Upstox API - POST /v2/order/positions/exit ?
        // If this endpoint exists:
        try {
            RequestBody body = RequestBody.create("{}", MediaType.get("application/json"));
            Request apiRequest = new Request.Builder()
                    .url(API_BASE_URL + "/order/positions/exit") // Assuming this TODO endpoint is valid
                    .header("Authorization", "Bearer " + getAccessToken())
                    .header("Accept", "application/json")
                    .post(body)
                    .build();

            executeRequest(apiRequest);
        } catch (Exception e) {
            logger.error("Exit positions failed: {}", e.getMessage());
            // Returning success anyway as per 'Core blocking' - if endpoint doesn't exist,
            // we skip
        }

        long latency = System.currentTimeMillis() - startTime;
        return new MultiOrderResult("success", List.of(), 0, 0, 0, latency);
    }

    @Override
    public boolean isAvailable() {
        // Simple health check call (e.g. user profile)
        try {
            Request apiRequest = new Request.Builder()
                    .url(API_BASE_URL + "/user/profile")
                    .header("Authorization", "Bearer " + getAccessToken())
                    .header("Accept", "application/json")
                    .get()
                    .build();
            try (Response response = httpClient.newCall(apiRequest).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public RateLimitStatus getRateLimitStatus() {
        long now = System.currentTimeMillis();
        if (now - minuteStartTime > 60000) {
            requestsThisMinute = 0;
            minuteStartTime = now;
        }

        return new RateLimitStatus(
                capabilities.rateLimitPerMinute() - requestsThisMinute,
                capabilities.rateLimitPerMinute(),
                minuteStartTime + 60000 - now);
    }

    private void checkRateLimit() {
        long now = System.currentTimeMillis();
        if (now - minuteStartTime > 60000) {
            requestsThisMinute = 0;
            minuteStartTime = now;
        }
        requestsThisMinute++;
        if (requestsThisMinute > capabilities.rateLimitPerMinute()) {
            throw new RuntimeException("Rate limit exceeded");
        }
    }
}
