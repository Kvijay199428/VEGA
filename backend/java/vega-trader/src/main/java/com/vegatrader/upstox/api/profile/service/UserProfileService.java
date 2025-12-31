package com.vegatrader.upstox.api.profile.service;

import com.vegatrader.upstox.api.profile.model.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User profile service with caching.
 * Per profile/a1.md section 4.
 * 
 * Fetch Policy:
 * - Login: Mandatory fetch
 * - Token refresh: Mandatory fetch
 * - Order rejection (eligibility): Optional refetch
 * - UI refresh: Use cache
 * 
 * Cache TTL: 15 minutes (configurable)
 * 
 * @since 4.8.0
 */
@Service
public class UserProfileService {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileService.class);

    private static final int DEFAULT_CACHE_TTL_SECONDS = 900; // 15 minutes

    // In-memory cache
    private final Map<String, UserProfile> profileCache = new ConcurrentHashMap<>();

    /**
     * Get user profile (from cache or fetch).
     */
    public UserProfile getProfile(String userId) {
        UserProfile cached = profileCache.get(userId);

        if (cached != null && !cached.isStale(DEFAULT_CACHE_TTL_SECONDS)) {
            logger.debug("Profile cache hit for user {}", userId);
            return cached;
        }

        // Fetch from broker
        UserProfile profile = fetchFromBroker(userId);
        profileCache.put(userId, profile);

        logger.info("Profile fetched for user {}", userId);
        return profile;
    }

    /**
     * Force refresh profile (on login/token refresh).
     */
    public UserProfile refreshProfile(String userId) {
        profileCache.remove(userId);
        return getProfile(userId);
    }

    /**
     * Invalidate cache for user.
     */
    public void invalidateCache(String userId) {
        profileCache.remove(userId);
    }

    /**
     * Check if profile allows exchange.
     */
    public boolean isExchangeEnabled(String userId, UserProfile.Exchange exchange) {
        return getProfile(userId).hasExchange(exchange);
    }

    /**
     * Check if profile allows product type.
     */
    public boolean isProductEnabled(String userId, UserProfile.ProductType product) {
        return getProfile(userId).hasProduct(product);
    }

    /**
     * Check if profile allows order type.
     */
    public boolean isOrderTypeEnabled(String userId, UserProfile.OrderType orderType) {
        return getProfile(userId).hasOrderType(orderType);
    }

    /**
     * Check if user account is active.
     */
    public boolean isAccountActive(String userId) {
        return getProfile(userId).active();
    }

    /**
     * Check if user can sell (has POA or DDPI).
     */
    public boolean canSell(String userId) {
        return getProfile(userId).canSell();
    }

    /**
     * Fetch profile from broker (placeholder for actual API call).
     */
    private UserProfile fetchFromBroker(String userId) {
        // TODO: Implement actual Upstox API call via BrokerAdapter
        // For now, return mock profile

        logger.debug("Fetching profile from broker for {}", userId);

        return new UserProfile(
                userId,
                "UPSTOX",
                "Test User",
                "test@example.com",
                "XXXXX1234X",
                Set.of(UserProfile.Exchange.NSE, UserProfile.Exchange.BSE, UserProfile.Exchange.NFO),
                Set.of(UserProfile.ProductType.I, UserProfile.ProductType.D),
                Set.of(UserProfile.OrderType.MARKET, UserProfile.OrderType.LIMIT, UserProfile.OrderType.SL),
                true,
                false,
                true,
                Instant.now());
    }

    /**
     * Get cache stats.
     */
    public int getCacheSize() {
        return profileCache.size();
    }
}
