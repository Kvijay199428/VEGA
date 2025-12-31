package com.vegatrader.upstox.api.order.service;

import com.vegatrader.upstox.api.order.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Order modification and cancellation service.
 * Per order-mgmt/main1/a3.md (Modify Order V3) and a4.md (Cancel Order V3).
 * 
 * @since 4.8.0
 */
@Service
public class OrderModifyService {

    private static final Logger logger = LoggerFactory.getLogger(OrderModifyService.class);

    private final OrderPersistenceOrchestrator orchestrator;

    public OrderModifyService(OrderPersistenceOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    /**
     * Modify an existing order.
     * Per a3.md - Modify Order V3.
     */
    public ModifyResult modifyOrder(ModifyRequest request) {
        logger.info("Modify order: {}", request.orderId());

        long startTime = System.currentTimeMillis();

        // Get existing order
        var existingOpt = orchestrator.getOrder(request.orderId());
        if (existingOpt.isEmpty()) {
            return ModifyResult.error("ORDER_NOT_FOUND", "Order not found: " + request.orderId());
        }

        Order existing = existingOpt.get();

        // Check if modifiable
        if (existing.isComplete()) {
            return ModifyResult.error("MODIFY_NOT_ALLOWED",
                    "Order cannot be modified - status: " + existing.status());
        }

        // Build modified order
        Order modified = new Order(
                existing.id(),
                existing.orderId(),
                existing.brokerOrderId(),
                existing.userId(),
                existing.broker(),
                existing.exchange(),
                existing.symbol(),
                existing.instrumentKey(),
                existing.side(),
                request.orderType() != null ? Order.OrderType.valueOf(request.orderType().replace("-", "_"))
                        : existing.orderType(),
                existing.product(),
                request.quantity() > 0 ? request.quantity() : existing.quantity(),
                request.price() != null ? request.price() : existing.price(),
                request.triggerPrice() != null ? request.triggerPrice() : existing.triggerPrice(),
                existing.status(),
                existing.filledQuantity(),
                existing.averagePrice(),
                existing.placedAt(),
                existing.acknowledgedAt(),
                Instant.now(),
                existing.rmsSnapshotId());

        // Persist modification (would update in DB)
        orchestrator.persist(modified, null, null);

        // Calculate latency
        long latencyMs = System.currentTimeMillis() - startTime;

        // Determine updated fields
        List<String> updatedFields = new ArrayList<>();
        if (request.price() != null && !request.price().equals(existing.price())) {
            updatedFields.add("price");
        }
        if (request.quantity() > 0 && request.quantity() != existing.quantity()) {
            updatedFields.add("quantity");
        }
        if (request.orderType() != null) {
            updatedFields.add("order_type");
        }

        logger.info("Order modified: {} (latency={}ms)", request.orderId(), latencyMs);

        return ModifyResult.success(request.orderId(), request.correlationId(), updatedFields, latencyMs);
    }

    /**
     * Cancel a single order.
     * Per a4.md - Cancel Order V3.
     */
    public CancelResult cancelOrder(String orderId) {
        logger.info("Cancel order: {}", orderId);

        long startTime = System.currentTimeMillis();

        var existingOpt = orchestrator.getOrder(orderId);
        if (existingOpt.isEmpty()) {
            return CancelResult.error("ORDER_NOT_FOUND", "Order not found: " + orderId);
        }

        Order existing = existingOpt.get();

        if (existing.isComplete()) {
            return CancelResult.error("CANCEL_NOT_ALLOWED",
                    "Order cannot be cancelled - status: " + existing.status());
        }

        orchestrator.updateStatus(orderId, Order.OrderStatus.CANCELLED);

        long latencyMs = System.currentTimeMillis() - startTime;

        logger.info("Order cancelled: {} (latency={}ms)", orderId, latencyMs);

        return CancelResult.success(orderId, latencyMs);
    }

    /**
     * Modify request.
     */
    public record ModifyRequest(
            String orderId,
            String correlationId,
            int quantity,
            String validity,
            BigDecimal price,
            String orderType,
            int disclosedQuantity,
            BigDecimal triggerPrice) {
    }

    /**
     * Modify result.
     */
    public record ModifyResult(
            String status,
            String orderId,
            String correlationId,
            List<String> updatedFields,
            long latencyMs,
            String errorCode,
            String message) {
        public static ModifyResult success(String orderId, String correlationId,
                List<String> updatedFields, long latencyMs) {
            return new ModifyResult("success", orderId, correlationId, updatedFields, latencyMs, null, null);
        }

        public static ModifyResult error(String errorCode, String message) {
            return new ModifyResult("error", null, null, List.of(), 0, errorCode, message);
        }
    }

    /**
     * Cancel result.
     */
    public record CancelResult(
            String status,
            String orderId,
            long latencyMs,
            String errorCode,
            String message) {
        public static CancelResult success(String orderId, long latencyMs) {
            return new CancelResult("success", orderId, latencyMs, null, null);
        }

        public static CancelResult error(String errorCode, String message) {
            return new CancelResult("error", null, 0, errorCode, message);
        }
    }
}
