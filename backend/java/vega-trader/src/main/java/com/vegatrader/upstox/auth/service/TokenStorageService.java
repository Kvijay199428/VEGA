package com.vegatrader.upstox.auth.service;

import com.vegatrader.upstox.auth.entity.UpstoxTokenEntity;
import com.vegatrader.upstox.auth.repository.TokenRepository;
import com.vegatrader.upstox.auth.response.TokenResponse;
import com.vegatrader.upstox.auth.utils.TokenExpiryCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.vegatrader.util.time.LocaleConstants;

/**
 * Service for token storage and retrieval operations.
 * Handles database lock fallback to in-memory cache.
 *
 * @since 2.0.0
 */
@Service
public class TokenStorageService {

    private static final Logger logger = LoggerFactory.getLogger(TokenStorageService.class);

    private final TokenRepository tokenRepository;
    private final TokenCacheService tokenCacheService;
    private final com.vegatrader.util.time.TimeProvider timeProvider;

    public TokenStorageService(TokenRepository tokenRepository, TokenCacheService tokenCacheService,
            com.vegatrader.util.time.TimeProvider timeProvider) {
        this.tokenRepository = tokenRepository;
        this.tokenCacheService = tokenCacheService;
        this.timeProvider = timeProvider;
    }

    /**
     * Store new token in database.
     * Falls back to in-memory cache if database is locked.
     *
     * @param tokenResponse token response from API
     * @param apiName       API name category (PRIMARY, WEBSOCKET1, etc.)
     * @param clientId      client ID
     * @param clientSecret  client secret
     * @param redirectUri   redirect URI
     * @param isPrimary     whether this is the primary token
     * @return true if stored (in DB or cache)
     */
    public boolean storeToken(TokenResponse tokenResponse, String apiName,
            String clientId, String clientSecret,
            String redirectUri, boolean isPrimary) {

        UpstoxTokenEntity entity = new UpstoxTokenEntity();
        entity.setAccessToken(tokenResponse.getAccessToken());
        entity.setRefreshToken(tokenResponse.getRefreshToken());
        entity.setTokenType(tokenResponse.getTokenType());
        entity.setExpiresIn(tokenResponse.getExpiresIn());
        entity.setApiName(apiName);
        entity.setClientId(clientId);
        entity.setClientSecret(clientSecret);
        entity.setRedirectUri(redirectUri);
        entity.setIsPrimary(isPrimary);
        entity.setIsActive(1);
        entity.setCreatedAt(
                timeProvider.now().atZone(LocaleConstants.IST).toLocalDateTime());
        entity.setGeneratedAt(
                timeProvider.now().atZone(LocaleConstants.IST).toLocalDateTime().toString());
        entity.setValidityAt(TokenExpiryCalculator.calculateValidityAtString(
                timeProvider.now().atZone(LocaleConstants.IST).toLocalDateTime()));

        try {
            boolean saved = tokenRepository.save(entity);
            if (saved) {
                logger.info("✓ Stored token in DB for apiName: {}, isPrimary: {}", apiName, isPrimary);
                return true;
            } else {
                logger.warn("✗ Failed to store token in DB for apiName: {}", apiName);
                return false;
            }
        } catch (TokenRepository.DbLockException e) {
            // Database is locked - fall back to in-memory cache
            logger.warn("╔═══════════════════════════════════════════════════════╗");
            logger.warn("║  DB LOCKED - STORING TOKEN IN MEMORY CACHE  ║");
            logger.warn("╚═══════════════════════════════════════════════════════╝");
            tokenCacheService.cachePendingToken(entity);
            logger.info("✓ Token cached in memory for apiName: {} (pending DB persist)", apiName);
            return true; // Token is safe in cache
        }
    }

    /**
     * Update existing token with new access token.
     *
     * @param apiName       API name
     * @param tokenResponse new token response
     */
    public void updateToken(String apiName, TokenResponse tokenResponse) {
        Optional<UpstoxTokenEntity> existing = tokenRepository.findByApiName(apiName);

        if (existing.isPresent()) {
            UpstoxTokenEntity entity = existing.get();
            entity.setAccessToken(tokenResponse.getAccessToken());
            entity.setRefreshToken(tokenResponse.getRefreshToken());
            entity.setLastRefreshed(
                    timeProvider.now().atZone(LocaleConstants.IST).toLocalDateTime());
            entity.setValidityAt(TokenExpiryCalculator.calculateValidityAtString(
                    timeProvider.now().atZone(LocaleConstants.IST).toLocalDateTime()));
            entity.setUpdatedAt(System.currentTimeMillis());

            try {
                tokenRepository.save(entity);
                logger.info("✓ Updated token for apiName: {}", apiName);
            } catch (TokenRepository.DbLockException e) {
                tokenCacheService.cachePendingToken(entity);
                logger.warn("DB locked - update cached for apiName: {}", apiName);
            }
        } else {
            logger.warn("⚠ Token not found for update: {}", apiName);
        }
    }

    /**
     * Get token by API name.
     * Checks both database and cache.
     *
     * @param apiName API name
     * @return token entity or empty
     */
    public Optional<UpstoxTokenEntity> getToken(String apiName) {
        // First check DB
        Optional<UpstoxTokenEntity> dbToken = tokenRepository.findByApiName(apiName);
        if (dbToken.isPresent()) {
            return dbToken;
        }

        // Then check cache
        if (tokenCacheService.hasPendingToken(apiName)) {
            for (TokenCacheService.PendingToken pending : tokenCacheService.getPendingTokens()) {
                if (pending.getApiName().equals(apiName)) {
                    return Optional.of(pending.getEntity());
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Get all active tokens.
     *
     * @return list of active tokens
     */
    public List<UpstoxTokenEntity> getAllActiveTokens() {
        return tokenRepository.findAllActive();
    }

    /**
     * Deactivate token by API name.
     *
     * @param apiName API name
     */
    public void deactivateToken(String apiName) {
        tokenRepository.deactivateByApiName(apiName);
        logger.info("✓ Deactivated token: {}", apiName);
    }

    /**
     * Check if token exists and is active.
     * Checks both database and cache.
     *
     * @param apiName API name
     * @return true if token exists and active
     */
    public boolean hasActiveToken(String apiName) {
        Optional<UpstoxTokenEntity> token = tokenRepository.findByApiName(apiName);
        if (token.isPresent() && token.get().isActive()) {
            return true;
        }
        // Also check cache
        return tokenCacheService.hasPendingToken(apiName);
    }

    /**
     * Get total count of active tokens.
     * Includes both DB and cached tokens.
     *
     * @return count
     */
    public int getActiveTokenCount() {
        int dbCount = tokenRepository.countActive();
        int cacheCount = tokenCacheService.getPendingCount();
        return dbCount + cacheCount;
    }

    /**
     * Get cache status.
     */
    public TokenCacheService.CacheStatus getCacheStatus() {
        return tokenCacheService.getStatus();
    }

    /**
     * Get list of all valid API names.
     */
    public List<String> getValidApiNames() {
        return getAllActiveTokens().stream()
                .map(UpstoxTokenEntity::getApiName)
                .toList();
    }
}
