package com.vegatrader.upstox.api.instrument.controller;

import com.vegatrader.upstox.api.instrument.loader.InstrumentFileSource;
import com.vegatrader.upstox.api.instrument.loader.InstrumentLoaderService;
import com.vegatrader.upstox.api.instrument.search.InstrumentSearchService;
import com.vegatrader.upstox.api.instrument.search.InstrumentSearchService.InstrumentSearchResult;
import com.vegatrader.upstox.api.instrument.validation.InstrumentKeyPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller for instrument search, autocomplete, and resolution.
 * 
 * @since 4.0.0
 */
@RestController
@RequestMapping("/v1/instruments")
public class InstrumentController {

    private static final Logger logger = LoggerFactory.getLogger(InstrumentController.class);

    private final InstrumentSearchService searchService;
    private final InstrumentLoaderService loaderService;

    public InstrumentController(
            InstrumentSearchService searchService,
            InstrumentLoaderService loaderService) {
        this.searchService = searchService;
        this.loaderService = loaderService;
    }

    /**
     * Resolve symbol to instrument key.
     * 
     * GET /api/v1/instruments/resolve?symbol=RELIANCE&segment=NSE_EQ&type=EQ
     */
    @GetMapping("/resolve")
    public ResponseEntity<?> resolve(
            @RequestParam String symbol,
            @RequestParam String segment,
            @RequestParam(name = "type") String instrumentType) {

        logger.debug("Resolving instrument: {}/{}/{}", symbol, segment, instrumentType);

        return searchService.resolveInstrumentKey(symbol, segment, instrumentType)
                .map(key -> ResponseEntity.ok(Map.of(
                        "instrument_key", key,
                        "symbol", symbol,
                        "segment", segment,
                        "instrument_type", instrumentType)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Autocomplete search.
     * 
     * GET /api/v1/instruments/autocomplete?q=REL
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<List<InstrumentSearchResult>> autocomplete(@RequestParam String q) {
        logger.debug("Autocomplete query: {}", q);

        List<InstrumentSearchResult> results = searchService.autocomplete(q);
        return ResponseEntity.ok(results);
    }

    /**
     * Full search with filters.
     * 
     * GET /api/v1/instruments/search?symbol=RELIANCE&segment=NSE_EQ&type=EQ
     */
    @GetMapping("/search")
    public ResponseEntity<List<InstrumentSearchResult>> search(
            @RequestParam String symbol,
            @RequestParam String segment,
            @RequestParam(name = "type") String instrumentType) {

        logger.debug("Search: {}/{}/{}", symbol, segment, instrumentType);

        List<InstrumentSearchResult> results = searchService.search(symbol, segment, instrumentType);
        return ResponseEntity.ok(results);
    }

    /**
     * Get instrument by key with overlays.
     * 
     * GET /api/v1/instruments/{instrumentKey}
     */
    @GetMapping("/{instrumentKey}")
    public ResponseEntity<?> getByKey(@PathVariable String instrumentKey) {
        // Validate key format
        if (!InstrumentKeyPattern.isValidSingleKey(instrumentKey)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid instrument key format",
                    "pattern", "SEGMENT|IDENTIFIER"));
        }

        return searchService.findByKey(instrumentKey)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get expiry dates for underlying.
     * 
     * GET /api/v1/instruments/expiries?underlyingKey=NSE_INDEX|Nifty 50
     */
    @GetMapping("/expiries")
    public ResponseEntity<List<LocalDate>> getExpiries(@RequestParam String underlyingKey) {
        List<LocalDate> expiries = searchService.getExpiryDates(underlyingKey);
        return ResponseEntity.ok(expiries);
    }

    /**
     * Get options chain.
     * 
     * GET /api/v1/instruments/options-chain?underlyingKey=NSE_INDEX|Nifty
     * 50&expiry=2025-01-02
     */
    @GetMapping("/options-chain")
    public ResponseEntity<?> getOptionsChain(
            @RequestParam String underlyingKey,
            @RequestParam String expiry) {
        try {
            LocalDate expiryDate = LocalDate.parse(expiry);
            var chain = searchService.getOptionsChain(underlyingKey, expiryDate);
            return ResponseEntity.ok(chain);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Manually trigger instrument load.
     * 
     * POST /api/v1/instruments/load?source=nse
     */
    @PostMapping("/load")
    public ResponseEntity<?> loadInstruments(@RequestParam String source) {
        try {
            InstrumentFileSource fileSource = InstrumentFileSource.fromKey(source);
            int count;

            if (fileSource.isBod()) {
                count = loaderService.loadBodInstruments(fileSource);
            } else if (fileSource.isMis()) {
                count = loaderService.loadMisOverlay(fileSource);
            } else if (fileSource.isMtf()) {
                count = loaderService.loadMtfOverlay(fileSource);
            } else if (fileSource.isSuspension()) {
                count = loaderService.loadSuspensionOverlay(fileSource);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Unknown source category"));
            }

            return ResponseEntity.ok(Map.of(
                    "source", source,
                    "count", count,
                    "status", "success"));
        } catch (Exception e) {
            logger.error("Failed to load instruments: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", e.getMessage(),
                    "status", "failed"));
        }
    }

    /**
     * Trigger full daily refresh.
     * 
     * POST /api/v1/instruments/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> triggerRefresh() {
        try {
            loaderService.performDailyRefresh();
            return ResponseEntity.ok(Map.of("status", "success", "message", "Daily refresh completed"));
        } catch (Exception e) {
            logger.error("Daily refresh failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", e.getMessage(),
                    "status", "failed"));
        }
    }
}
