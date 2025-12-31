package com.vegatrader.upstox.api.admin.controller;

import com.vegatrader.upstox.api.admin.model.*;
import com.vegatrader.upstox.api.admin.service.AdminActionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin API Controller per arch/a6.md section 2.
 * Provides admin-level controls for strikes, brokers, and contracts.
 * 
 * @since 4.6.0
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final AdminActionService adminService;

    public AdminController(AdminActionService adminService) {
        this.adminService = adminService;
    }

    /**
     * POST /admin/strikes/disable
     * Disable a strike for trading.
     */
    @PostMapping("/strikes/disable")
    public ResponseEntity<Map<String, Object>> disableStrike(
            @RequestBody StrikeDisableRequest request,
            @RequestHeader("X-Admin-User") String adminUser,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {

        logger.info("Strike disable request: {} by {}", request, adminUser);

        adminService.disableStrike(request, adminUser, ipAddress);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Strike disabled",
                "strike", request.strike(),
                "underlying", request.underlyingKey()));
    }

    /**
     * POST /admin/strikes/enable
     * Re-enable a previously disabled strike.
     */
    @PostMapping("/strikes/enable")
    public ResponseEntity<Map<String, Object>> enableStrike(
            @RequestBody StrikeEnableRequest request,
            @RequestHeader("X-Admin-User") String adminUser) {

        logger.info("Strike enable request: {} by {}", request, adminUser);

        adminService.enableStrike(request, adminUser);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Strike enabled"));
    }

    /**
     * POST /admin/brokers/priority
     * Update broker priority for a segment.
     */
    @PostMapping("/brokers/priority")
    public ResponseEntity<Map<String, Object>> updateBrokerPriority(
            @RequestBody BrokerPriorityRequest request,
            @RequestHeader("X-Admin-User") String adminUser) {

        logger.info("Broker priority update: {} by {}", request, adminUser);

        adminService.updateBrokerPriority(request, adminUser);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "priority", request.priority()));
    }

    /**
     * POST /admin/contracts/rollback
     * Rollback to a previous contract version.
     */
    @PostMapping("/contracts/rollback")
    public ResponseEntity<Map<String, Object>> rollbackContract(
            @RequestBody ContractRollbackRequest request,
            @RequestHeader("X-Admin-User") String adminUser) {

        logger.info("Contract rollback: {} by {}", request, adminUser);

        adminService.rollbackContract(request, adminUser);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "broker", request.broker(),
                "version", request.contractVersion()));
    }

    /**
     * GET /admin/audit
     * Get recent admin actions.
     */
    @GetMapping("/audit")
    public ResponseEntity<Object> getAuditLog(
            @RequestParam(value = "limit", defaultValue = "50") int limit) {

        return ResponseEntity.ok(adminService.getRecentActions(limit));
    }
}
