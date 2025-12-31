package com.vegatrader.upstox.auth.selenium.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Loads Upstox API configurations from .env file.
 * Reads from backend/.env for all 6 API configurations.
 *
 * @since 2.0.0
 */
public class EnvConfigLoader {

    private static final Logger logger = LoggerFactory.getLogger(EnvConfigLoader.class);

    private final Properties properties;

    public EnvConfigLoader() {
        this.properties = new Properties();
        loadEnvFile();
    }

    /**
     * Load .env file from working directory.
     * Can be overridden with VEGA_ENV_FILE environment variable.
     */
    private void loadEnvFile() {
        // Allow override via environment variable
        String envPath = System.getenv().getOrDefault(
                "VEGA_ENV_FILE",
                Paths.get(System.getProperty("user.dir"), ".env").toString());

        logger.info("Loading .env from: {}", envPath);

        try (FileInputStream fis = new FileInputStream(envPath)) {
            properties.load(fis);
            logger.info("✓ Loaded {} properties from .env", properties.size());
        } catch (IOException e) {
            logger.error("Failed to load .env file from: {}", envPath, e);
            logger.warn("Attempting to load from environment variables instead");
        }
    }

    /**
     * Get property value, falling back to environment variable.
     */
    private String getProperty(String key) {
        // Try .env file first
        String value = properties.getProperty(key);

        // Fallback to environment variable
        if (value == null) {
            value = System.getenv(key);
        }

        return value;
    }

    /**
     * Get client ID for specific API index (0-5).
     *
     * @param apiIndex API index (0=PRIMARY, 1-3=WEBSOCKET, 4-5=OPTIONCHAIN)
     * @return client ID
     */
    public String getClientId(int apiIndex) {
        return getProperty(String.format("UPSTOX_CLIENT_ID_%d", apiIndex));
    }

    /**
     * Get client secret for specific API index (0-5).
     */
    public String getClientSecret(int apiIndex) {
        return getProperty(String.format("UPSTOX_CLIENT_SECRET_%d", apiIndex));
    }

    /**
     * Get redirect URI (common for all APIs).
     */
    public String getRedirectUri() {
        return getProperty("UPSTOX_REDIRECT_URI");
    }

    /**
     * Get mobile number.
     */
    public String getMobileNumber() {
        return getProperty("UPSTOX_MOBILE_NUMBER");
    }

    /**
     * Get PIN (password).
     */
    public String getPin() {
        return getProperty("UPSTOX_PIN");
    }

    /**
     * Get TOTP secret.
     */
    public String getTotpSecret() {
        return getProperty("UPSTOX_TOTP");
    }

    /**
     * Get user ID.
     */
    public String getUserId() {
        return getProperty("UPSTOX_USER_ID");
    }

    /**
     * Get API name for index.
     *
     * @param apiIndex 0-5
     * @return API name (PRIMARY, WEBSOCKET1-3, OPTIONCHAIN1-2)
     */
    public String getApiName(int apiIndex) {
        switch (apiIndex) {
            case 0:
                return "PRIMARY";
            case 1:
                return "WEBSOCKET1";
            case 2:
                return "WEBSOCKET2";
            case 3:
                return "WEBSOCKET3";
            case 4:
                return "OPTIONCHAIN1";
            case 5:
                return "OPTIONCHAIN2";
            default:
                throw new IllegalArgumentException("Invalid API index: " + apiIndex);
        }
    }

    /**
     * Check if all required properties are loaded.
     *
     * @return true if all configs are present
     */
    public boolean isConfigured() {
        for (int i = 0; i < 6; i++) {
            if (getClientId(i) == null || getClientSecret(i) == null) {
                logger.warn("Missing configuration for API {}", i);
                return false;
            }
        }

        if (getMobileNumber() == null || getPin() == null) {
            logger.warn("Missing credentials (mobile/PIN)");
            return false;
        }

        return true;
    }

    /**
     * Print configuration summary (without secrets).
     */
    public void printSummary() {
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("Environment Configuration Summary");
        logger.info("═══════════════════════════════════════════════════════");

        for (int i = 0; i < 6; i++) {
            String clientId = getClientId(i);
            if (clientId != null) {
                logger.info("API {}: {} - Client ID: {}...",
                        i, getApiName(i), clientId.substring(0, 8));
            }
        }

        logger.info("Mobile: {}", getMobileNumber());
        logger.info("User ID: {}", getUserId());
        logger.info("Redirect URI: {}", getRedirectUri());
        logger.info("TOTP Configured: {}", getTotpSecret() != null);
        logger.info("═══════════════════════════════════════════════════════");
    }
}
