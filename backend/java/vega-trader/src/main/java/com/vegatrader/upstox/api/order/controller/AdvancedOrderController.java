package com.vegatrader.upstox.api.order.controller;

import com.vegatrader.upstox.api.order.model.*;
import com.vegatrader.upstox.api.order.service.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Advanced order management REST controller.
 * Per order-mgmt/main1/a1.md through a7.md.
 * 
 * Endpoints:
 * - POST /api/orders/multi/place - Place Multi Order V2
 * - PUT /api/orders/modify - Modify Order V3
 * - DELETE /api/orders/cancel - Cancel Order V3
 * - DELETE /api/orders/multi/cancel - Cancel Multi Order
 * - POST /api/orders/positions/exit - Exit All Positions
 * - GET /api/orders/{orderId}/history - Get Order History
 * 
 * @since 4.8.0
 */
@RestController
@RequestMapping("/api/orders")
public class AdvancedOrderController {

    private final MultiOrderService multiOrderService;
    private final OrderModifyService modifyService;
    private final OrderPersistenceOrchestrator orchestrator;

    public AdvancedOrderController(
            MultiOrderService multiOrderService,
            OrderModifyService modifyService,
            OrderPersistenceOrchestrator orchestrator) {
        this.multiOrderService = multiOrderService;
        this.modifyService = modifyService;
        this.orchestrator = orchestrator;
    }

    /**
     * POST /api/orders/multi/place - Place Multiple Orders (V2)
     * Per a1.md section 1 and a2.md.
     */
    @PostMapping("/multi/place")
    public ResponseEntity<MultiOrderResponse> placeMultiOrder(
            @RequestBody MultiOrderRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "demo") String userId) {

        var response = multiOrderService.placeMultiOrder(request, userId);

        HttpStatus status = switch (response.status()) {
            case "success" -> HttpStatus.OK;
            case "partial_success" -> HttpStatus.MULTI_STATUS; // 207
            default -> HttpStatus.BAD_REQUEST;
        };

        return ResponseEntity.status(status).body(response);
    }

    /**
     * PUT /api/orders/modify - Modify Order (V3)
     * Per a1.md section 2 and a3.md.
     */
    @PutMapping("/modify")
    public ResponseEntity<Map<String, Object>> modifyOrder(
            @RequestBody ModifyOrderRequest request) {

        var modifyRequest = new OrderModifyService.ModifyRequest(
                request.orderId(),
                request.correlationId(),
                request.quantity() != null ? request.quantity() : 0,
                request.validity(),
                request.price(),
                request.orderType(),
                request.disclosedQuantity() != null ? request.disclosedQuantity() : 0,
                request.triggerPrice());

        var result = modifyService.modifyOrder(modifyRequest);

        if ("success".equals(result.status())) {
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", Map.of(
                            "order_id", result.orderId(),
                            "updated_fields", result.updatedFields()),
                    "metadata", Map.of("latency", result.latencyMs())));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "error_code", result.errorCode(),
                    "message", result.message()));
        }
    }

    /**
     * DELETE /api/orders/cancel - Cancel Order (V3)
     * Per a1.md section 3.
     */
    @DeleteMapping("/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @RequestParam("order_id") String orderId) {

        var result = modifyService.cancelOrder(orderId);

        if ("success".equals(result.status())) {
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", Map.of("order_id", result.orderId()),
                    "metadata", Map.of("latency", result.latencyMs())));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "error_code", result.errorCode(),
                    "message", result.message()));
        }
    }

    /**
     * DELETE /api/orders/multi/cancel - Cancel Multi Order
     * Per a1.md section 4 and a5.md.
     */
    @DeleteMapping("/multi/cancel")
    public ResponseEntity<MultiOrderResponse> cancelMultiOrder(
            @RequestBody CancelMultiRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "demo") String userId) {

        MultiOrderResponse response;

        if (request.orderIds() != null && !request.orderIds().isEmpty()) {
            response = multiOrderService.cancelMultiOrder(request.orderIds(), userId);
        } else {
            response = multiOrderService.cancelByFilter(request.segment(), request.tag(), userId);
        }

        HttpStatus status = switch (response.status()) {
            case "success" -> HttpStatus.OK;
            case "partial_success" -> HttpStatus.MULTI_STATUS;
            default -> HttpStatus.BAD_REQUEST;
        };

        return ResponseEntity.status(status).body(response);
    }

    /**
     * POST /api/orders/positions/exit - Exit All Positions
     * Per a1.md section 5 and a6.md.
     */
    @PostMapping("/positions/exit")
    public ResponseEntity<MultiOrderResponse> exitAllPositions(
            @RequestParam(required = false) String segment,
            @RequestParam(required = false) String tag,
            @RequestHeader(value = "X-User-Id", defaultValue = "demo") String userId) {

        var response = multiOrderService.exitAllPositions(segment, tag, userId);

        HttpStatus status = switch (response.status()) {
            case "success" -> HttpStatus.OK;
            case "partial_success" -> HttpStatus.MULTI_STATUS;
            default -> HttpStatus.BAD_REQUEST;
        };

        return ResponseEntity.status(status).body(response);
    }

    /**
     * GET /api/orders/{orderId}/history - Get Order History
     * Per a7.md.
     */
    @GetMapping("/{orderId}/history")
    public ResponseEntity<Map<String, Object>> getOrderHistory(
            @PathVariable String orderId) {

        var orderOpt = orchestrator.getOrder(orderId);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var order = orderOpt.get();
        var auditLog = orchestrator.getAuditLog(orderId);

        // Build history from audit events
        List<Map<String, Object>> history = new ArrayList<>();
        for (var event : auditLog) {
            history.add(Map.of(
                    "status", event.eventType(),
                    "timestamp", event.createdAt().toString(),
                    "payload", event.payload()));
        }

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "data", Map.of(
                        "order_id", order.orderId(),
                        "current_status", order.status().name(),
                        "history", history)));
    }

    /**
     * GET /api/orders/by-correlation/{correlationId}
     * Lookup order by correlation ID.
     */
    @GetMapping("/by-correlation/{correlationId}")
    public ResponseEntity<Map<String, Object>> getByCorrelation(
            @PathVariable String correlationId) {

        var orderIdOpt = multiOrderService.getOrderIdByCorrelation(correlationId);
        if (orderIdOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String orderId = orderIdOpt.get();
        var orderOpt = orchestrator.getOrder(orderId);

        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
                "correlation_id", correlationId,
                "order_id", orderId,
                "order", orderOpt.get()));
    }

    /**
     * Request DTOs.
     */
    public record ModifyOrderRequest(
            String orderId,
            String correlationId,
            Integer quantity,
            String validity,
            BigDecimal price,
            String orderType,
            Integer disclosedQuantity,
            BigDecimal triggerPrice) {
    }

    public record CancelMultiRequest(
            List<String> orderIds,
            String segment,
            String tag) {
    }
}
