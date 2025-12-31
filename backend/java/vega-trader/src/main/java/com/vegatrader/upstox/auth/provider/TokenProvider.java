package com.vegatrader.upstox.auth.provider;

import com.vegatrader.upstox.auth.entity.UpstoxTokenEntity;
import com.vegatrader.upstox.auth.repository.TokenRepository;
import com.vegatrader.upstox.auth.utils.ApiNameResolver;
import com.vegatrader.upstox.auth.config.AuthConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main token provider with load balancing and caching.
 * Distributes tokens across PRIMARY, WEBSOCKET, and OPTIONCHAIN categories.
 *
 * @since 2.0.0
 */
public class TokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    private final TokenRepository tokenRepository;
    private final Map<String, UpstoxTokenEntity> tokenCache;
    private final AtomicInteger wsRoundRobin;
    private final AtomicInteger ocRoundRobin;

    public TokenProvider() {
        this.tokenRepository = new TokenRepository();
        this.tokenCache = new ConcurrentHashMap<>();
        this.wsRoundRobin = new AtomicInteger(0);
        this.ocRoundRobin = new AtomicInteger(0);

        initializeCache();
    }

    /**
     * Initialize token cache from database.
     */
    private void initializeCache() {
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("Initializing Token Cache from Database");
        logger.info("═══════════════════════════════════════════════════════");

        List<UpstoxTokenEntity> allTokens = tokenRepository.findAllActive();

        if (allTokens.isEmpty()) {
            logger.warn("⚠ No active tokens found in database!");
            return;
        }

        for (UpstoxTokenEntity token : allTokens) {
            tokenCache.put(token.getApiName(), token);
            logger.info("✓ Loaded token: {}", token.getApiName());
        }

        logger.info("═══════════════════════════════════════════════════════");
        logger.info("Token Cache Initialized: {} tokens", tokenCache.size());
        logger.info("═══════════════════════════════════════════════════════");
    }

    /**
     * Get token for endpoint with automatic load balancing.
     *
     * @param endpoint the API endpoint
     * @return token entity or null if not available
     */
    public UpstoxTokenEntity getTokenForEndpoint(String endpoint) {
        String category = ApiNameResolver.getCategory(
                ApiNameResolver.resolveApiName(endpoint));

        if (AuthConstants.TOKEN_CATEGORY_WEBSOCKET_PREFIX.equals(category)) {
            return getWebSocketToken();
        }

        if (AuthConstants.TOKEN_CATEGORY_OPTIONCHAIN_PREFIX.equals(category)) {
            return getOptionChainToken();
        }

        return getPrimaryToken();
    }

    /**
     * Get WebSocket token with round-robin load balancing.
     */
    private UpstoxTokenEntity getWebSocketToken() {
        // Try tokens in round-robin order: WEBSOCKET1 → WEBSOCKET2 → WEBSOCKET3
        for (int i = 0; i < AuthConstants.WEBSOCKET_TOKEN_COUNT; i++) {
            int index = (wsRoundRobin.getAndIncrement() % AuthConstants.WEBSOCKET_TOKEN_COUNT) + 1;
            String apiName = AuthConstants.TOKEN_CATEGORY_WEBSOCKET_PREFIX + index;

            UpstoxTokenEntity token = tokenCache.get(apiName);
            if (token != null && token.isActive()) {
                logger.debug("WebSocket → Using {}", apiName);
                return token;
            }
        }

        logger.warn("⚠ No WebSocket tokens available, using PRIMARY fallback");
        return getPrimaryToken();
    }

    /**
     * Get Option Chain token with round-robin load balancing.
     */
    private UpstoxTokenEntity getOptionChainToken() {
        // Try tokens in round-robin order: OPTIONCHAIN1 → OPTIONCHAIN2
        for (int i = 0; i < AuthConstants.OPTIONCHAIN_TOKEN_COUNT; i++) {
            int index = (ocRoundRobin.getAndIncrement() % AuthConstants.OPTIONCHAIN_TOKEN_COUNT) + 1;
            String apiName = AuthConstants.TOKEN_CATEGORY_OPTIONCHAIN_PREFIX + index;

            UpstoxTokenEntity token = tokenCache.get(apiName);
            if (token != null && token.isActive()) {
                logger.debug("OptionChain → Using {}", apiName);
                return token;
            }
        }

        logger.warn("⚠ No OptionChain tokens available, using PRIMARY fallback");
        return getPrimaryToken();
    }

    /**
     * Get PRIMARY token.
     */
    private UpstoxTokenEntity getPrimaryToken() {
        UpstoxTokenEntity token = tokenCache.get(AuthConstants.TOKEN_CATEGORY_PRIMARY);

        if (token == null || !token.isActive()) {
            logger.error("✗ PRIMARY token unavailable!");
            return null;
        }

        logger.debug("Standard → Using PRIMARY");
        return token;
    }

    /**
     * Get token by API name directly.
     *
     * @param apiName the API name (PRIMARY, WEBSOCKET1, etc.)
     * @return token entity or null
     */
    public UpstoxTokenEntity getToken(String apiName) {
        UpstoxTokenEntity cached = tokenCache.get(apiName);

        if (cached != null && cached.isActive()) {
            return cached;
        }

        // Refresh from database
        return tokenRepository.findByApiName(apiName).orElse(null);
    }

    /**
     * Refresh token cache from database.
     */
    public synchronized void refreshCache() {
        logger.info("🔄 Refreshing token cache from database...");
        tokenCache.clear();
        wsRoundRobin.set(0);
        ocRoundRobin.set(0);
        initializeCache();
    }

    /**
     * Get cache statistics.
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();

        stats.put("total_cached_tokens", tokenCache.size());
        stats.put("primary_available", tokenCache.containsKey(AuthConstants.TOKEN_CATEGORY_PRIMARY));

        long wsCount = tokenCache.keySet().stream()
                .filter(k -> k.startsWith(AuthConstants.TOKEN_CATEGORY_WEBSOCKET_PREFIX))
                .count();
        stats.put("websocket_count", wsCount + "/" + AuthConstants.WEBSOCKET_TOKEN_COUNT);

        long ocCount = tokenCache.keySet().stream()
                .filter(k -> k.startsWith(AuthConstants.TOKEN_CATEGORY_OPTIONCHAIN_PREFIX))
                .count();
        stats.put("optionchain_count", ocCount + "/" + AuthConstants.OPTIONCHAIN_TOKEN_COUNT);

        stats.put("ws_next_index", (wsRoundRobin.get() % AuthConstants.WEBSOCKET_TOKEN_COUNT) + 1);
        stats.put("oc_next_index", (ocRoundRobin.get() % AuthConstants.OPTIONCHAIN_TOKEN_COUNT) + 1);

        return stats;
    }
}
