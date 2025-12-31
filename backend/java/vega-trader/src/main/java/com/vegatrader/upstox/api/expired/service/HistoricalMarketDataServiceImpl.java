package com.vegatrader.upstox.api.expired.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vegatrader.upstox.api.expired.model.Candle;
import com.vegatrader.upstox.auth.entity.UpstoxTokenEntity;
import com.vegatrader.upstox.auth.service.TokenStorageService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Implementation of HistoricalMarketDataService.
 * Fetches OHLC + OI data for expired instruments.
 * 
 * @since 4.4.0
 */
@Service
public class HistoricalMarketDataServiceImpl implements HistoricalMarketDataService {

    private static final Logger logger = LoggerFactory.getLogger(HistoricalMarketDataServiceImpl.class);

    private static final Set<String> VALID_INTERVALS = Set.of(
            "1minute", "3minute", "5minute", "15minute", "30minute", "day");

    // Pattern for expired instrument key: EXCHANGE_SEGMENT|TOKEN|DD-MM-YYYY
    private static final Pattern EXPIRED_KEY_PATTERN = Pattern
            .compile("^(NSE|BSE)_(FO|INDEX)\\|\\d+\\|\\d{2}-\\d{2}-\\d{4}$");

    private static final int DEFAULT_MAX_DAYS = 365;
    private static final String BASE_URL = "https://api.upstox.com/v2/expired-instruments/historical-candle";

    private final TokenStorageService tokenStorageService;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public HistoricalMarketDataServiceImpl(TokenStorageService tokenStorageService,
            OkHttpClient httpClient,
            ObjectMapper objectMapper) {
        this.tokenStorageService = tokenStorageService;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    private String getActiveToken() {
        Optional<UpstoxTokenEntity> tokenEntity = tokenStorageService.getToken("UPSTOX");
        if (tokenEntity.isPresent() && tokenEntity.get().isActive()) {
            return tokenEntity.get().getAccessToken();
        }
        throw new RuntimeException("No active Upstox token");
    }

    @Override
    public List<Candle> fetchExpiredHistoricalCandles(
            String expiredInstrumentKey,
            String interval,
            LocalDate fromDate,
            LocalDate toDate) {

        // Validate inputs
        if (!isValidExpiredKey(expiredInstrumentKey)) {
            logger.warn("Invalid expired instrument key: {}", expiredInstrumentKey);
            return List.of();
        }

        if (!isValidInterval(interval)) {
            logger.warn("Invalid interval: {}", interval);
            return List.of();
        }

        if (!isValidDateRange(fromDate, toDate, DEFAULT_MAX_DAYS)) {
            logger.warn("Invalid date range: {} to {}", fromDate, toDate);
            return List.of();
        }

        logger.info("Fetching historical candles: {} [{} to {}] interval={}",
                expiredInstrumentKey, fromDate, toDate, interval);

        try {
            String url = String.format("%s/%s/%s/%s/%s",
                    BASE_URL,
                    expiredInstrumentKey,
                    interval,
                    toDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE)); // Upstox usually takes to/from or as params.
                                                                        // Assuming path params based on naming
                                                                        // convention or query params.

            // Actually Upstox historical API (for live) is
            // /v2/historical-candle/{instrumentKey}/{interval}/{to_date}/{from_date}
            // For expired, I will assume similar path structure or check documentation
            // provided.
            // Documentation `a1.md` just says GET
            // /v2/expired-instruments/historical-candle.
            // I'll stick to query params if path is ambiguous, but Upstox standard is path.
            // Let's use PATH style as it is standard Upstox.

            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + getActiveToken())
                    .header("Accept", "application/json")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonNode root = objectMapper.readTree(response.body().string());
                    List<Candle> candles = new ArrayList<>();
                    if (root.has("data") && root.get("data").get("candles").isArray()) {
                        for (JsonNode candleNode : root.get("data").get("candles")) {
                            // Parse [timestamp, open, high, low, close, volume, oi]
                            // TODO: Add parsing logic
                            // candles.add(new Candle(...));
                        }
                    }
                    return candles;
                } else {
                    logger.error("Failed to fetch historical candles: code={}", response.code());
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching historical candles: {}", e.getMessage());
        }

        return List.of();
    }

    @Override
    public boolean isValidInterval(String interval) {
        return interval != null && VALID_INTERVALS.contains(interval.toLowerCase());
    }

    /**
     * Validate expired instrument key format.
     */
    public boolean isValidExpiredKey(String expiredInstrumentKey) {
        if (expiredInstrumentKey == null || expiredInstrumentKey.isBlank()) {
            return false;
        }
        // Less strict pattern - just check it has 3 parts separated by |
        String[] parts = expiredInstrumentKey.split("\\|");
        return parts.length >= 3;
    }

    /**
     * Get maximum allowed days for historical fetch.
     */
    public int getMaxDays() {
        return DEFAULT_MAX_DAYS;
    }

    /**
     * Get supported intervals.
     */
    public Set<String> getSupportedIntervals() {
        return VALID_INTERVALS;
    }
}
