package com.vegatrader.upstox.api.expired.controller;

import com.vegatrader.upstox.api.expired.model.*;
import com.vegatrader.upstox.api.expired.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Expired Instruments Historical Data.
 * Implements endpoints per c1.md flow diagram.
 * 
 * @since 4.4.0
 */
@RestController
@RequestMapping("/api/v1/expired")
public class ExpiredInstrumentController {

    private static final Logger logger = LoggerFactory.getLogger(ExpiredInstrumentController.class);

    private final ExpiredInstrumentFetcher fetcher;
    private final ExpiredInstrumentService expiredService;
    private final HistoricalMarketDataService marketDataService;

    public ExpiredInstrumentController(
            ExpiredInstrumentFetcher fetcher,
            ExpiredInstrumentService expiredService,
            HistoricalMarketDataService marketDataService) {
        this.fetcher = fetcher;
        this.expiredService = expiredService;
        this.marketDataService = marketDataService;
    }

    /**
     * GET /api/v1/expired/expiries
     * Fetch available expiry dates for an underlying.
     */
    @GetMapping("/expiries")
    public ResponseEntity<Map<String, Object>> getExpiries(
            @RequestParam("underlying") String underlyingKey,
            @RequestParam(value = "userId", defaultValue = "default") String userId) {

        logger.info("GET /expired/expiries: underlying={}, userId={}", underlyingKey, userId);

        List<LocalDate> expiries = fetcher.fetchExpiries(userId, underlyingKey);

        return ResponseEntity.ok(Map.of(
                "underlyingKey", underlyingKey,
                "expiryCount", expiries.size(),
                "expiries", expiries));
    }

    /**
     * GET /api/v1/expired/options
     * Fetch expired option contracts for an expiry.
     */
    @GetMapping("/options")
    public ResponseEntity<ExpiredDataResponse> getExpiredOptions(
            @RequestParam("underlying") String underlyingKey,
            @RequestParam("expiry") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiry,
            @RequestParam(value = "userId", defaultValue = "default") String userId) {

        logger.info("GET /expired/options: underlying={}, expiry={}", underlyingKey, expiry);

        List<ExpiredOptionContract> options = fetcher.fetchOptions(userId, underlyingKey, expiry);

        return ResponseEntity.ok(ExpiredDataResponse.ofOptions(underlyingKey, expiry, options));
    }

    /**
     * GET /api/v1/expired/futures
     * Fetch expired future contracts for an expiry.
     */
    @GetMapping("/futures")
    public ResponseEntity<ExpiredDataResponse> getExpiredFutures(
            @RequestParam("underlying") String underlyingKey,
            @RequestParam("expiry") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiry,
            @RequestParam(value = "userId", defaultValue = "default") String userId) {

        logger.info("GET /expired/futures: underlying={}, expiry={}", underlyingKey, expiry);

        List<ExpiredFutureContract> futures = fetcher.fetchFutures(userId, underlyingKey, expiry);

        return ResponseEntity.ok(ExpiredDataResponse.ofFutures(underlyingKey, expiry, futures));
    }

    /**
     * GET /api/v1/expired/candles
     * Fetch historical candles for an expired instrument.
     */
    @GetMapping("/candles")
    public ResponseEntity<ExpiredDataResponse> getHistoricalCandles(
            @RequestParam("expiredKey") String expiredInstrumentKey,
            @RequestParam(value = "interval", defaultValue = "day") String interval,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(value = "userId", defaultValue = "default") String userId) {

        logger.info("GET /expired/candles: key={}, interval={}, from={}, to={}",
                expiredInstrumentKey, interval, fromDate, toDate);

        List<Candle> candles = fetcher.fetchCandles(userId, expiredInstrumentKey, fromDate, toDate);

        return ResponseEntity.ok(ExpiredDataResponse.ofCandles(
                expiredInstrumentKey, null, interval, candles));
    }

    /**
     * GET /api/v1/expired/data
     * Full historical data fetch with user settings.
     */
    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getExpiredData(
            @RequestParam("underlying") String underlyingKey,
            @RequestParam(value = "userId", defaultValue = "default") String userId) {

        logger.info("GET /expired/data: underlying={}, userId={}", underlyingKey, userId);

        List<Candle> candles = fetcher.fetchWithUserSettings(userId, underlyingKey);

        return ResponseEntity.ok(Map.of(
                "underlyingKey", underlyingKey,
                "candleCount", candles.size(),
                "candles", candles));
    }

    /**
     * GET /api/v1/expired/intervals
     * Get supported candle intervals.
     */
    @GetMapping("/intervals")
    public ResponseEntity<List<String>> getSupportedIntervals() {
        return ResponseEntity.ok(expiredService.getSupportedIntervals());
    }

    /**
     * GET /api/v1/expired/validate
     * Validate an underlying key format.
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateKey(
            @RequestParam("key") String key) {

        boolean valid = expiredService.isValidUnderlyingKey(key);

        return ResponseEntity.ok(Map.of(
                "key", key,
                "valid", valid));
    }
}
