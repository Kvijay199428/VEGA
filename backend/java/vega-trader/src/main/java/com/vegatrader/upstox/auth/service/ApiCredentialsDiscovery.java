package com.vegatrader.upstox.auth.service;

import com.vegatrader.upstox.auth.config.AuthConstants;
import com.vegatrader.upstox.auth.selenium.config.LoginCredentials;
import com.vegatrader.upstox.auth.selenium.v2.EnvConfigLoaderV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Discovers all Upstox API credentials from .env file.
 * Supports dynamic discovery of UPSTOX_CLIENT_ID_N and UPSTOX_CLIENT_SECRET_N.
 * Enforces 1-based indexing for .env keys (ID_1..ID_6) mapped to 0-based API
 * list.
 *
 * @since 2.0.0
 */
@Service
public class ApiCredentialsDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(ApiCredentialsDiscovery.class);
    // Use canonical constant from AuthConstants
    private static final int MAX_API_COUNT = AuthConstants.TOTAL_UPSTOX_APIS;

    private final List<UpstoxAppCredentials> discoveredApps = new ArrayList<>();
    private final EnvConfigLoaderV2 envConfigLoader;

    /**
     * Constructor - initializes EnvConfigLoaderV2.
     */
    public ApiCredentialsDiscovery() {
        this.envConfigLoader = new EnvConfigLoaderV2();
    }

    /**
     * Credentials for a single Upstox App.
     */
    public static class UpstoxAppCredentials {
        private final int index;
        private final String clientId;
        private final String clientSecret;
        private final String purpose;

        public UpstoxAppCredentials(int index, String clientId, String clientSecret) {
            this.index = index;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.purpose = resolvePurpose(index);
        }

        private static String resolvePurpose(int index) {
            // Use canonical API order if possible, with index check
            if (index >= 0 && index < AuthConstants.API_ORDER.size()) {
                return AuthConstants.API_ORDER.get(index);
            }
            return "API_" + index; // Fallback should ideally never happen given strictly bounded loop
        }

        public int getIndex() {
            return index;
        }

        public String getClientId() {
            return clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public String getPurpose() {
            return purpose;
        }

        @Override
        public String toString() {
            return String.format("UpstoxApp[%d:%s:%s...]", index, purpose,
                    (clientId != null && clientId.length() > 8) ? clientId.substring(0, 8) : "null");
        }
    }

    @PostConstruct
    public void discoverApis() {
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("Discovering Upstox API Credentials from .env file");
        logger.info("═══════════════════════════════════════════════════════");

        if (!envConfigLoader.isLoaded()) {
            logger.warn("⚠ .env file not loaded - checking system environment variables...");
        }

        for (int i = 0; i < MAX_API_COUNT; i++) {
            // Try EnvConfigLoaderV2 first (reads from .env file)
            // Note: envConfigLoader.getClientId(i) intentionally maps 'i' to 'i+1'
            // internally used for property keys.
            String clientId = envConfigLoader.getClientId(i);
            String clientSecret = envConfigLoader.getClientSecret(i);

            // Fallback to strict environment variables if not in .env
            // FIX: Use 1-based indexing for fallback too (apiIndex + 1)
            int envIndex = i + 1;
            if (clientId == null || clientId.isEmpty()) {
                clientId = System.getenv("UPSTOX_CLIENT_ID_" + envIndex);
            }
            if (clientSecret == null || clientSecret.isEmpty()) {
                clientSecret = System.getenv("UPSTOX_CLIENT_SECRET_" + envIndex);
            }

            if (clientId != null && !clientId.isEmpty() && clientSecret != null && !clientSecret.isEmpty()) {
                UpstoxAppCredentials app = new UpstoxAppCredentials(i, clientId, clientSecret);
                discoveredApps.add(app);
                logger.info("✓ Discovered API {}: {} ({})", i, app.getPurpose(),
                        (clientId.length() > 8) ? clientId.substring(0, 8) + "..." : clientId);
            } else {
                logger.warn("⚠ Missing credentials for API Index {} (Expects UPSTOX_CLIENT_ID_{})", i, envIndex);
            }
        }

        if (discoveredApps.isEmpty()) {
            logger.warn("⚠ No Upstox API credentials found in .env or environment!");
            logger.warn("  Expected .env format: UPSTOX_CLIENT_ID_1=xxx, UPSTOX_CLIENT_SECRET_1=xxx");
        } else {
            logger.info("═══════════════════════════════════════════════════════");
            logger.info("✓ Total APIs discovered: {}", discoveredApps.size());
            logger.info("═══════════════════════════════════════════════════════");
        }
    }

    /**
     * Get all discovered API credentials.
     */
    public List<UpstoxAppCredentials> getAllApps() {
        return Collections.unmodifiableList(discoveredApps);
    }

    /**
     * Get total number of discovered APIs.
     */
    public int getAppCount() {
        return discoveredApps.size();
    }

    /**
     * Get credentials by index.
     */
    public UpstoxAppCredentials getApp(int index) {
        return discoveredApps.stream()
                .filter(app -> app.getIndex() == index)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get PRIMARY app (index 0).
     */
    public UpstoxAppCredentials getPrimaryApp() {
        return getApp(0);
    }

    /**
     * Get common login credentials (User/Pass/TOTP) from .env
     */
    public LoginCredentials getCommonCredentials() {
        return new LoginCredentials(
                envConfigLoader.getMobileNumber(),
                envConfigLoader.getPin(),
                envConfigLoader.getTotpSecret());
    }
}
