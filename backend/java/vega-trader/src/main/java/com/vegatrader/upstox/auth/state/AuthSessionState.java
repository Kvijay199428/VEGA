package com.vegatrader.upstox.auth.state;

import com.vegatrader.upstox.auth.entity.UpstoxTokenEntity;
import com.vegatrader.upstox.auth.utils.TokenExpiryCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Centralized state for Authentication Session.
 * Tracks configured vs valid tokens and readiness status.
 *
 * @since 1.0.0
 */
@Component
public class AuthSessionState {

    private static final Logger log = LoggerFactory.getLogger(AuthSessionState.class);

    private final Set<String> configuredApis = new HashSet<>();
    private final Set<String> validApis = new HashSet<>();

    private volatile boolean primaryReady = false;
    private volatile boolean fullyReady = false;

    /**
     * Hydrate state from database tokens and configured APIs.
     *
     * @param tokens     List of all active tokens from DB
     * @param apiConfigs List of all expected/configured APIs
     */
    public synchronized void hydrateFromDatabase(List<UpstoxTokenEntity> tokens, List<String> apiConfigs) {
        configuredApis.clear();
        configuredApis.addAll(apiConfigs);

        validApis.clear();
        primaryReady = false;

        for (UpstoxTokenEntity token : tokens) {
            // Check expiry
            if (token.isActive() && !TokenExpiryCalculator.isExpired(token.getValidityAt())) {
                validApis.add(token.getApiName());

                // Check if PRIMARY (case-insensitive check or use boolean flag)
                if (token.isPrimary() || "PRIMARY".equalsIgnoreCase(token.getApiName())) {
                    primaryReady = true;
                }
            }
        }

        evaluateReadiness();
        log.info("[AUTH-HYDRATE] Valid APIs: {} / {}", validApis.size(), configuredApis.size());
        log.info("[AUTH-HYDRATE] Missing: {}", getMissingApis());
    }

    /**
     * Register a newly generated valid token.
     *
     * @param apiName API Name (e.g. PRIMARY, WEBSOCKET_1)
     */
    public synchronized void registerValidToken(String apiName) {
        validApis.add(apiName);

        if ("PRIMARY".equalsIgnoreCase(apiName)) {
            primaryReady = true;
        }

        evaluateReadiness();
        log.debug("[AUTH-STATE] Registered: {}, FullyReady: {}", apiName, fullyReady);
    }

    /**
     * Evaluate if all configured tokens are present.
     */
    public synchronized void evaluateReadiness() {
        fullyReady = validApis.containsAll(configuredApis);
    }

    /**
     * Get set of missing APIs.
     */
    public Set<String> getMissingApis() {
        Set<String> missing = new HashSet<>(configuredApis);
        missing.removeAll(validApis);
        return missing;
    }

    public int getGeneratedCount() {
        return validApis.size();
    }

    public int getRequiredCount() {
        return configuredApis.size();
    }

    // Manual Getters (Replacing Lombok @Getter)
    public Set<String> getConfiguredApis() {
        return configuredApis;
    }

    public Set<String> getValidApis() {
        return validApis;
    }

    public boolean isPrimaryReady() {
        return primaryReady;
    }

    public boolean isFullyReady() {
        return fullyReady;
    }
}
