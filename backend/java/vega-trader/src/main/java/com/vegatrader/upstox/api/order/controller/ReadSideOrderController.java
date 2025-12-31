package com.vegatrader.upstox.api.order.controller;

import com.vegatrader.upstox.api.order.model.*;
import com.vegatrader.upstox.api.order.service.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Read-Side Order & Trade REST Controller.
 * Per order-mgmt/main2/b1.md and b2.md.
 * 
 * Endpoints:
 * - GET /api/v2/order/history - Order history
 * - GET /api/v2/order/retrieve-all - Order book
 * - GET /api/v2/order/trades/get-trades-for-day - Today's trades
 * - GET /api/v2/order/trades - Order trades
 * - GET /api/v2/charges/historical-trades - Trade history
 * 
 * @since 4.8.0
 */
@RestController
@RequestMapping("/api/v2")
public class ReadSideOrderController {

        private final CoordinatorService coordinatorService;

        public ReadSideOrderController(CoordinatorService coordinatorService) {
                this.coordinatorService = coordinatorService;
        }

        /**
         * GET /api/v2/order/history - Get order history
         * Per b1.md line 3-198.
         */
        @GetMapping("/order/history")
        public ResponseEntity<Map<String, Object>> getOrderHistory(
                        @RequestParam(value = "order_id", required = false) String orderId,
                        @RequestParam(value = "tag", required = false) String tag) {

                if (orderId == null && tag == null) {
                        return ResponseEntity.badRequest().body(Map.of(
                                        "status", "error",
                                        "message", "Either order_id or tag must be provided"));
                }

                var response = coordinatorService.getOrderHistory(orderId, tag);

                return ResponseEntity.ok(Map.of(
                                "status", response.status(),
                                "data", response.events()));
        }

        /**
         * GET /api/v2/order/retrieve-all - Get order book
         * Per b1.md line 291-440.
         */
        @GetMapping("/order/retrieve-all")
        public ResponseEntity<Map<String, Object>> getOrderBook(
                        @RequestHeader(value = "X-User-Id", defaultValue = "demo") String userId) {

                var response = coordinatorService.getOrderBook(userId);

                return ResponseEntity.ok(Map.of(
                                "status", response.status(),
                                "data", response.data(),
                                "metadata", Map.of(
                                                "last_update", response.lastUpdate().toString(),
                                                "age_ms", response.ageMs(),
                                                "source", response.source())));
        }

        /**
         * GET /api/v2/order/trades/get-trades-for-day - Get all trades for day
         * Per b1.md line 486-585.
         */
        @GetMapping("/order/trades/get-trades-for-day")
        public ResponseEntity<Map<String, Object>> getTradesForDay(
                        @RequestHeader(value = "X-User-Id", defaultValue = "demo") String userId) {

                var response = coordinatorService.getTradesForDay(userId);

                return ResponseEntity.ok(Map.of(
                                "status", response.status(),
                                "data", response.data()));
        }

        /**
         * GET /api/v2/order/trades - Get trades for specific order
         * Per b1.md line 589-700.
         */
        @GetMapping("/order/trades")
        public ResponseEntity<Map<String, Object>> getOrderTrades(
                        @RequestParam("order_id") String orderId) {

                var trades = coordinatorService.getTradesForOrder(orderId);

                return ResponseEntity.ok(Map.of(
                                "status", "success",
                                "data", trades));
        }

        /**
         * GET /api/v2/charges/historical-trades - Get trade history
         * Per b1.md line 701+.
         * Supports segment filter and pagination.
         */
        @GetMapping("/charges/historical-trades")
        public ResponseEntity<Map<String, Object>> getTradeHistory(
                        @RequestParam(value = "segment", required = false) String segment,
                        @RequestParam("start_date") String startDate,
                        @RequestParam("end_date") String endDate,
                        @RequestParam("page_number") int pageNumber,
                        @RequestParam("page_size") int pageSize,
                        @RequestHeader(value = "X-User-Id", defaultValue = "demo") String userId) {

                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);

                // Validate date range (max 3 financial years per API docs)
                if (start.isBefore(LocalDate.now().minusYears(3))) {
                        return ResponseEntity.badRequest().body(Map.of(
                                        "status", "error",
                                        "message", "start_date must be within last 3 financial years"));
                }

                var response = coordinatorService.getTradeHistory(
                                userId, segment, start, end, pageNumber, pageSize);

                return ResponseEntity.ok(Map.of(
                                "status", response.status(),
                                "data", response.data(),
                                "errors", (Object) null,
                                "meta_data", Map.of(
                                                "page", Map.of(
                                                                "page_number", response.metadata().pageNumber(),
                                                                "page_size", response.metadata().pageSize(),
                                                                "total_records", response.metadata().totalRecords(),
                                                                "total_pages", response.metadata().totalPages()))));
        }
}
