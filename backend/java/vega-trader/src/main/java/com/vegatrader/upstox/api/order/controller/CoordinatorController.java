package com.vegatrader.upstox.api.order.controller;

import com.vegatrader.upstox.api.order.model.*;
import com.vegatrader.upstox.api.order.service.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Coordinator REST Controller.
 * Per order-mgmt/main2/b3.md and b4.md.
 * 
 * Single entry point for all write operations with idempotency.
 * 
 * @since 4.8.0
 */
@RestController
@RequestMapping("/api/v2/coordinator")
public class CoordinatorController {

    private final CoordinatorService coordinatorService;

    public CoordinatorController(CoordinatorService coordinatorService) {
        this.coordinatorService = coordinatorService;
    }

    /**
     * POST /api/v2/coordinator/order/multi - Place multi order (idempotent)
     * Per b3.md section 6.1.
     */
    @PostMapping("/order/multi")
    public ResponseEntity<MultiOrderResponse> placeMultiOrder(
            @RequestBody MultiOrderRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "demo") String userId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        var response = coordinatorService.placeMultiOrder(request, userId, idempotencyKey);

        HttpStatus status = switch (response.status()) {
            case "success" -> HttpStatus.OK;
            case "partial_success" -> HttpStatus.MULTI_STATUS;
            default -> HttpStatus.BAD_REQUEST;
        };

        return ResponseEntity.status(status).body(response);
    }

    /**
     * PUT /api/v2/coordinator/order/modify - Modify order (idempotent)
     * Per b3.md section 6.2.
     */
    @PutMapping("/order/modify")
    public ResponseEntity<Map<String, Object>> modifyOrder(
            @RequestBody ModifyRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        var modifyRequest = new OrderModifyService.ModifyRequest(
                request.orderId(),
                request.correlationId(),
                request.quantity() != null ? request.quantity() : 0,
                request.validity(),
                request.price(),
                request.orderType(),
                request.disclosedQuantity() != null ? request.disclosedQuantity() : 0,
                request.triggerPrice());

        var result = coordinatorService.modifyOrder(modifyRequest, idempotencyKey);

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
     * DELETE /api/v2/coordinator/order/multi/cancel - Cancel multi order
     * (idempotent)
     * Per b3.md section 6.3.
     */
    @DeleteMapping("/order/multi/cancel")
    public ResponseEntity<MultiOrderResponse> cancelMultiOrder(
            @RequestBody CancelRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "demo") String userId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        var response = coordinatorService.cancelMultiOrder(request.orderIds(), userId, idempotencyKey);

        HttpStatus status = switch (response.status()) {
            case "success" -> HttpStatus.OK;
            case "partial_success" -> HttpStatus.MULTI_STATUS;
            default -> HttpStatus.BAD_REQUEST;
        };

        return ResponseEntity.status(status).body(response);
    }

    /**
     * GET /api/v2/coordinator/health - Health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "service", "coordinator",
                "timestamp", System.currentTimeMillis()));
    }

    // Request DTOs

    public record ModifyRequest(
            String orderId,
            String correlationId,
            Integer quantity,
            String validity,
            BigDecimal price,
            String orderType,
            Integer disclosedQuantity,
            BigDecimal triggerPrice) {
    }

    public record CancelRequest(
            List<String> orderIds) {
    }
}
