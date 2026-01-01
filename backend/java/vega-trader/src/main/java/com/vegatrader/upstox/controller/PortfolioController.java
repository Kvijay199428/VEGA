package com.vegatrader.upstox.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Portfolio Controller - Provides KPI endpoints for Dashboard.
 */
@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioController.class);

    /**
     * GET /api/portfolio/kpis
     * Returns key performance indicators for dashboard tiles.
     */
    @GetMapping("/kpis")
    public ResponseEntity<Map<String, Object>> getKpis() {
        Map<String, Object> kpis = new HashMap<>();

        // Net P&L (Mock data - would come from portfolio service)
        kpis.put("netPnl", 12450.75);
        kpis.put("netPnlPercent", 2.35);

        // Exposure
        kpis.put("grossExposure", 525000.00);
        kpis.put("netExposure", 125000.00);

        // Margin
        kpis.put("marginUsed", 185000.00);
        kpis.put("marginAvailable", 315000.00);
        kpis.put("marginUtilization", 37.0);

        // Positions
        kpis.put("openPositions", 8);
        kpis.put("dayTrades", 12);

        return ResponseEntity.ok(kpis);
    }
}
