package com.vegatrader.upstox.api.optionchain.controller;

import com.vegatrader.upstox.api.optionchain.model.*;
import com.vegatrader.upstox.api.optionchain.service.OptionChainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Option Chain REST Controller.
 * Per optionchain/implementations/a1.md.
 * 
 * @since 4.7.0
 */
@RestController
@RequestMapping("/api/v1/option-chain")
public class OptionChainController {

    private static final Logger logger = LoggerFactory.getLogger(OptionChainController.class);

    private final OptionChainService service;

    public OptionChainController(OptionChainService service) {
        this.service = service;
    }

    /**
     * GET /api/v1/option-chain
     * Fetch option chain for a symbol and expiry.
     */
    @GetMapping
    public ResponseEntity<OptionChainResponse> getOptionChain(
            @RequestParam("symbol") String instrumentKey,
            @RequestParam("expiry") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiry) {

        logger.info("GET /option-chain: symbol={}, expiry={}", instrumentKey, expiry);

        OptionChainResponse response = service.getOptionChain(instrumentKey, expiry);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/option-chain/expiries
     * Get available expiry dates for a symbol.
     */
    @GetMapping("/expiries")
    public ResponseEntity<Map<String, Object>> getExpiries(
            @RequestParam("symbol") String instrumentKey) {

        logger.info("GET /option-chain/expiries: symbol={}", instrumentKey);

        List<LocalDate> expiries = service.getExpiries(instrumentKey);

        return ResponseEntity.ok(Map.of(
                "symbol", instrumentKey,
                "expiryCount", expiries.size(),
                "expiries", expiries));
    }

    /**
     * POST /api/v1/option-chain/cache/clear
     * Clear cache for a specific symbol/expiry (Admin only).
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, Object>> clearCache(
            @RequestParam("symbol") String instrumentKey,
            @RequestParam("expiry") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiry,
            @RequestHeader("X-Admin-User") String adminUser) {

        logger.info("POST /option-chain/cache/clear: {} {} by {}",
                instrumentKey, expiry, adminUser);

        service.clearCache(instrumentKey, expiry);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Cache cleared"));
    }

    /**
     * POST /api/v1/option-chain/prewarm
     * Prewarm cache for a symbol/expiry (Admin only).
     */
    @PostMapping("/prewarm")
    public ResponseEntity<Map<String, Object>> prewarmCache(
            @RequestParam("symbol") String instrumentKey,
            @RequestParam("expiry") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiry,
            @RequestHeader("X-Admin-User") String adminUser) {

        logger.info("POST /option-chain/prewarm: {} {} by {}",
                instrumentKey, expiry, adminUser);

        service.prewarmCache(instrumentKey, expiry);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Cache prewarmed"));
    }
}
