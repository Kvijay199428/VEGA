package com.vegatrader.upstox.api.expired.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vegatrader.upstox.api.expired.model.*;
import com.vegatrader.upstox.auth.entity.UpstoxTokenEntity;
import com.vegatrader.upstox.auth.service.TokenStorageService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Implementation of ExpiredInstrumentService per a1.md.
 * Manages expired instruments as derived, runtime-only entities.
 * 
 * @since 4.4.0
 */
@Service
public class ExpiredInstrumentServiceImpl implements ExpiredInstrumentService {

    private static final Logger logger = LoggerFactory.getLogger(ExpiredInstrumentServiceImpl.class);

    // Pattern for underlying key: EXCHANGE_SEGMENT|SYMBOL
    private static final Pattern UNDERLYING_KEY_PATTERN = Pattern.compile("^(NSE|BSE)_(INDEX|FO|EQ)\\|.+$");
    private static final String BASE_URL = "https://api.upstox.com/v2/expired-instruments";

    // Cache for expiry dates (TTL: 24h)
    private final Map<String, CachedExpiries> expiryCache = new ConcurrentHashMap<>();

    private final TokenStorageService tokenStorageService;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public ExpiredInstrumentServiceImpl(TokenStorageService tokenStorageService,
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
    public List<LocalDate> fetchExpiries(String underlyingKey) {
        if (!isValidUnderlyingKey(underlyingKey)) {
            logger.warn("Invalid underlying key: {}", underlyingKey);
            return List.of();
        }

        // Check cache first
        CachedExpiries cached = expiryCache.get(underlyingKey);
        if (cached != null && !cached.isExpired()) {
            logger.debug("Cache hit for expiries: {}", underlyingKey);
            return cached.expiries();
        }

        logger.info("Fetching expiries for: {}", underlyingKey);
        try {
            String url = String.format("%s/expiries?underlying_key=%s", BASE_URL, underlyingKey);
            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + getActiveToken())
                    .header("Accept", "application/json")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonNode root = objectMapper.readTree(response.body().string());
                    List<LocalDate> expiries = new ArrayList<>();
                    if (root.has("data") && root.get("data").isArray()) {
                        for (JsonNode dateNode : root.get("data")) {
                            expiries.add(LocalDate.parse(dateNode.asText()));
                        }
                    }
                    cacheExpiries(underlyingKey, expiries);
                    return expiries;
                } else {
                    logger.error("Failed to fetch expiries: code={}", response.code());
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching expiries: {}", e.getMessage());
        }

        return List.of();
    }

    @Override
    public List<ExpiredOptionContract> fetchExpiredOptions(String underlyingKey, LocalDate expiry) {
        if (!isValidUnderlyingKey(underlyingKey) || expiry == null || expiry.isAfter(LocalDate.now())) {
            return List.of();
        }

        logger.info("Fetching expired options: {} @ {}", underlyingKey, expiry);
        try {
            String url = String.format("%s/option/contract?underlying_key=%s&expiry_date=%s",
                    BASE_URL, underlyingKey, expiry.format(DateTimeFormatter.ISO_LOCAL_DATE));
            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + getActiveToken())
                    .header("Accept", "application/json")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    // TODO: Parse into ExpiredOptionContract list properly
                    // For now returning empty list as placeholders can't be guessed without model
                    return new ArrayList<>();
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching expired options: {}", e.getMessage());
        }
        return List.of();
    }

    @Override
    public List<ExpiredFutureContract> fetchExpiredFutures(String underlyingKey, LocalDate expiry) {
        if (!isValidUnderlyingKey(underlyingKey) || expiry == null || expiry.isAfter(LocalDate.now())) {
            return List.of();
        }

        logger.info("Fetching expired futures: {} @ {}", underlyingKey, expiry);
        try {
            String url = String.format("%s/future/contract?underlying_key=%s&expiry_date=%s",
                    BASE_URL, underlyingKey, expiry.format(DateTimeFormatter.ISO_LOCAL_DATE));
            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + getActiveToken())
                    .header("Accept", "application/json")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    // TODO: Parse into ExpiredFutureContract list
                    return new ArrayList<>();
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching expired futures: {}", e.getMessage());
        }
        return List.of();
    }

    @Override
    public String buildExpiredInstrumentKey(String instrumentKey, LocalDate expiry) {
        if (instrumentKey == null || expiry == null) {
            throw new IllegalArgumentException("Instrument key and expiry cannot be null");
        }

        String dateStr = String.format("%02d-%02d-%04d",
                expiry.getDayOfMonth(), expiry.getMonthValue(), expiry.getYear());

        return instrumentKey + "|" + dateStr;
    }

    @Override
    public boolean isValidUnderlyingKey(String underlyingKey) {
        if (underlyingKey == null || underlyingKey.isBlank()) {
            return false;
        }
        return UNDERLYING_KEY_PATTERN.matcher(underlyingKey).matches();
    }

    /**
     * Cache expiries for an underlying (called after API fetch).
     */
    public void cacheExpiries(String underlyingKey, List<LocalDate> expiries) {
        expiryCache.put(underlyingKey, new CachedExpiries(expiries, System.currentTimeMillis()));
        logger.debug("Cached {} expiries for {}", expiries.size(), underlyingKey);
    }

    /**
     * Clear expiry cache.
     */
    public void clearCache() {
        expiryCache.clear();
        logger.info("Expiry cache cleared");
    }

    /**
     * Inner class for cached expiries with TTL.
     */
    private record CachedExpiries(List<LocalDate> expiries, long cachedAt) {
        private static final long TTL_MS = 24 * 60 * 60 * 1000; // 24 hours

        boolean isExpired() {
            return System.currentTimeMillis() - cachedAt > TTL_MS;
        }
    }
}
