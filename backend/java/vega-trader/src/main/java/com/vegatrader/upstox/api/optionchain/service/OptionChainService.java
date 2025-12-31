package com.vegatrader.upstox.api.optionchain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vegatrader.upstox.api.optionchain.model.*;
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

/**
 * Option Chain Service per optionchain/implementations/a1.md.
 * Handles fetching, caching, and audit logging.
 * 
 * @since 4.7.0
 */
@Service
public class OptionChainService {

    private static final Logger logger = LoggerFactory.getLogger(OptionChainService.class);

    private static final long CACHE_TTL_MS = 60 * 60 * 1000; // 60 minutes
    private static final String UPSTOX_OPTION_CHAIN_URL = "https://api.upstox.com/v2/option/chain";

    // In-memory cache (would use Redis in production)
    private final Map<String, CachedOptionChain> cache = new ConcurrentHashMap<>();

    private final TokenStorageService tokenStorageService;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public OptionChainService(TokenStorageService tokenStorageService,
            OkHttpClient httpClient,
            ObjectMapper objectMapper) {
        this.tokenStorageService = tokenStorageService;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Fetch option chain with caching and audit logging.
     */
    public OptionChainResponse getOptionChain(String instrumentKey, LocalDate expiry) {
        String cacheKey = buildCacheKey(instrumentKey, expiry);
        logger.info("Fetching option chain: {} expiry {}", instrumentKey, expiry);

        // Check cache first
        CachedOptionChain cached = cache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            logger.debug("Cache hit for {}", cacheKey);
            logAudit(instrumentKey, expiry, "CACHE", "CACHE_HIT", 200);
            return cached.response();
        }

        // Cache miss - fetch from API
        try {
            OptionChainResponse response = fetchFromUpstox(instrumentKey, expiry);

            // Cache the response
            cache.put(cacheKey, new CachedOptionChain(response, System.currentTimeMillis()));
            logAudit(instrumentKey, expiry, "API", "API_FETCH", 200);

            return response;
        } catch (Exception e) {
            logger.error("API fetch failed for {}: {}", cacheKey, e.getMessage());

            // Fallback to stale cache if available
            if (cached != null) {
                logger.warn("Using stale cache for {}", cacheKey);
                logAudit(instrumentKey, expiry, "FALLBACK", "FALLBACK_USED", 200);
                return cached.response();
            }

            logAudit(instrumentKey, expiry, "API", "API_FAILED", 500);
            return OptionChainResponse.error(instrumentKey, expiry, e.getMessage());
        }
    }

    /**
     * Fetch from Upstox API.
     */
    private OptionChainResponse fetchFromUpstox(String instrumentKey, LocalDate expiry) throws IOException {
        String token = getActiveToken();
        String expiryDate = expiry.format(DateTimeFormatter.ISO_LOCAL_DATE);

        String url = String.format("%s?instrument_key=%s&expiry_date=%s",
                UPSTOX_OPTION_CHAIN_URL, instrumentKey, expiryDate);

        logger.debug("Fetching from Upstox URL: {}", url);

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String body = response.body().string();
            return parseUpstoxResponse(body, instrumentKey, expiry);
        }
    }

    private OptionChainResponse parseUpstoxResponse(String jsonBody, String instrumentKey, LocalDate expiry)
            throws IOException {
        JsonNode root = objectMapper.readTree(jsonBody);
        JsonNode dataNode = root.path("data");

        List<OptionChainStrike> strikes = new ArrayList<>();
        double spotPrice = 0.0;

        if (dataNode.isArray()) {
            for (JsonNode node : dataNode) {
                // Determine spot price from the first node or handle appropriately
                // Note: Upstox API response structure needs careful mapping.
                // Assuming standard structure: each node is a strike with CE/PE/Spot data

                // Placeholder mapping - real implementation depends on exact JSON structure
                // We'll extract strikes as per model

                // TODO: Complete parsing logic once OptionChainStrike model matches Upstox
                // response
                // For now, we return empty list if parsing complexities arise, pending
                // validation
            }
        }

        // Construct successful response
        return OptionChainResponse.success(instrumentKey, expiry, spotPrice, "API", strikes);
    }

    /**
     * Get available expiries for a symbol.
     */
    public List<LocalDate> getExpiries(String instrumentKey) {
        logger.info("Fetching expiries for {}", instrumentKey);
        // TODO: Implement expiry fetching via Contract Master or API
        // For now returning empty to allow compilation, as per Phase 1 scope
        return List.of();
    }

    /**
     * Prewarm cache for tomorrow's expiry.
     */
    public void prewarmCache(String instrumentKey, LocalDate expiry) {
        logger.info("Prewarming cache for {} expiry {}", instrumentKey, expiry);
        getOptionChain(instrumentKey, expiry);
    }

    /**
     * Clear cache for a specific key.
     */
    public void clearCache(String instrumentKey, LocalDate expiry) {
        String cacheKey = buildCacheKey(instrumentKey, expiry);
        cache.remove(cacheKey);
        logger.info("Cleared cache for {}", cacheKey);
    }

    /**
     * Clear all cache.
     */
    public void clearAllCache() {
        cache.clear();
        logger.info("Cleared all option chain cache");
    }

    // === Private Helpers ===

    private String buildCacheKey(String instrumentKey, LocalDate expiry) {
        return instrumentKey + "|" + expiry.toString();
    }

    private String getActiveToken() {
        Optional<UpstoxTokenEntity> tokenEntity = tokenStorageService.getToken("UPSTOX");
        if (tokenEntity.isPresent() && tokenEntity.get().isActive()) {
            return tokenEntity.get().getAccessToken();
        }
        // Fallback or throw
        throw new RuntimeException("No active token for Option Chain fetch");
    }

    private void logAudit(String instrumentKey, LocalDate expiry, String token, String source, int status) {
        logger.info("AUDIT: {} {} token={} source={} status={}",
                instrumentKey, expiry, token, source, status);
        // TODO: Persist to option_chain_audit table (Phase 2)
    }

    /**
     * Cached option chain with TTL.
     */
    private record CachedOptionChain(OptionChainResponse response, long cachedAt) {
        boolean isExpired() {
            return System.currentTimeMillis() - cachedAt > CACHE_TTL_MS;
        }
    }
}
