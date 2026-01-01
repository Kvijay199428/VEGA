package com.vegatrader.upstox.auth.selenium.v2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Loads Upstox API configurations from .env file for V2 login automation.
 * Reads credentials from UPSTOX_MOBILE_NUMBER, UPSTOX_PIN, UPSTOX_TOTP.
 *
 * @since 2.1.0
 */
public class EnvConfigLoaderV2 {

    private static final Logger logger = LoggerFactory.getLogger(EnvConfigLoaderV2.class);

    // Credential keys
    private static final String KEY_MOBILE_NUMBER = "UPSTOX_MOBILE_NUMBER";
    private static final String KEY_PIN = "UPSTOX_PIN";
    private static final String KEY_TOTP = "UPSTOX_TOTP";
    private static final String KEY_USER_ID = "UPSTOX_USER_ID";
    private static final String KEY_REDIRECT_URI = "UPSTOX_REDIRECT_URI";

    // Client ID/Secret pattern: UPSTOX_CLIENT_ID_0, UPSTOX_CLIENT_SECRET_0, etc.
    private static final String KEY_CLIENT_ID_PATTERN = "UPSTOX_CLIENT_ID_%d";
    private static final String KEY_CLIENT_SECRET_PATTERN = "UPSTOX_CLIENT_SECRET_%d";

    private final Properties properties;
    private boolean loaded;

    public EnvConfigLoaderV2() {
        this.properties = new Properties();
        this.loaded = false;
        loadEnvFile();
    }

    /**
     * Load .env file from working directory.
     * Can be overridden with VEGA_ENV_FILE environment variable.
     */
    private void loadEnvFile() {
        // Try multiple locations
        String[] possiblePaths = {
                System.getenv("VEGA_ENV_FILE"),
                Paths.get(System.getProperty("user.dir"), ".env").toString(),
                Paths.get(System.getProperty("user.dir"), "backend", "java", "vega-trader", ".env").toString(),
                "d:\\projects\\VEGA TRADER\\backend\\java\\vega-trader\\.env"
        };

        for (String envPath : possiblePaths) {
            if (envPath == null)
                continue;

            try {
                Path path = Paths.get(envPath);
                if (path.toFile().exists()) {
                    try (FileInputStream fis = new FileInputStream(envPath)) {
                        properties.load(fis);
                        logger.info("✓ Loaded {} properties from: {}", properties.size(), envPath);
                        loaded = true;
                        return;
                    }
                }
            } catch (IOException e) {
                logger.debug("Could not load .env from: {}", envPath);
            }
        }

        logger.warn("Could not load .env file, falling back to environment variables");
    }

    /**
     * Get property value, falling back to environment variable.
     */
    private String getProperty(String key) {
        // Try .env file first
        String value = properties.getProperty(key);

        // Fallback to environment variable
        if (value == null || value.isEmpty()) {
            value = System.getenv(key);
        }

        return value;
    }

    /**
     * Get mobile number.
     */
    public String getMobileNumber() {
        return getProperty(KEY_MOBILE_NUMBER);
    }

    /**
     * Get PIN (6-digit password).
     */
    public String getPin() {
        return getProperty(KEY_PIN);
    }

    /**
     * Get TOTP secret (Base32 encoded).
     */
    public String getTotpSecret() {
        return getProperty(KEY_TOTP);
    }

    /**
     * Get user ID.
     */
    public String getUserId() {
        return getProperty(KEY_USER_ID);
    }

    /**
     * Get redirect URI (common for all APIs).
     */
    public String getRedirectUri() {
        String uri = getProperty(KEY_REDIRECT_URI);
        return uri != null ? uri : "http://localhost:28020/api/v1/auth/upstox/callback";
    }

    /**
     * Get client ID for specific API index (0-5).
     *
     * @param apiIndex API index (0=PRIMARY, 1-3=WEBSOCKET, 4-5=OPTIONCHAIN)
     * @return client ID
     */
    public String getClientId(int apiIndex) {
        // Shift index by +1 to match .env file (0 -> UPSTOX_CLIENT_ID_1)
        return getProperty(String.format(KEY_CLIENT_ID_PATTERN, apiIndex + 1));
    }

    /**
     * Get client secret for specific API index (0-5).
     */
    public String getClientSecret(int apiIndex) {
        return getProperty(String.format(KEY_CLIENT_SECRET_PATTERN, apiIndex + 1));
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
     * Build LoginCredentialsV2 from env configuration.
     */
    public LoginCredentialsV2 buildCredentials() {
        return new LoginCredentialsV2(getMobileNumber(), getPin(), getTotpSecret());
    }

    /**
     * Build LoginConfigV2 for specific API index.
     *
     * @param apiIndex 0-5
     * @param headless run browser in headless mode
     * @return complete login config
     */
    public LoginConfigV2 buildLoginConfig(int apiIndex, boolean headless) {
        return LoginConfigV2.builder()
                .apiName(getApiName(apiIndex))
                .clientId(getClientId(apiIndex))
                .clientSecret(getClientSecret(apiIndex))
                .redirectUri(getRedirectUri())
                .credentials(buildCredentials())
                .headless(headless)
                .browser("chrome")
                .primary(apiIndex == 0)
                .build();
    }

    /**
     * Check if configuration is loaded and complete.
     */
    public boolean isConfigured() {
        String mobile = getMobileNumber();
        String pin = getPin();
        String totp = getTotpSecret();
        String clientId = getClientId(0);
        String clientSecret = getClientSecret(0);

        if (mobile == null || mobile.isEmpty()) {
            logger.warn("Missing UPSTOX_MOBILE_NUMBER");
            return false;
        }
        if (pin == null || pin.isEmpty()) {
            logger.warn("Missing UPSTOX_PIN");
            return false;
        }
        if (totp == null || totp.isEmpty()) {
            logger.warn("Missing UPSTOX_TOTP");
            return false;
        }
        if (clientId == null || clientId.isEmpty()) {
            logger.warn("Missing UPSTOX_CLIENT_ID_0");
            return false;
        }
        if (clientSecret == null || clientSecret.isEmpty()) {
            logger.warn("Missing UPSTOX_CLIENT_SECRET_0");
            return false;
        }

        return true;
    }

    /**
     * Check if .env file was successfully loaded.
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Print configuration summary (without secrets).
     */
    public void printSummary() {
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("V2 Environment Configuration Summary");
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("Mobile:        {}", getMobileNumber());
        logger.info("PIN:           ******");
        logger.info("TOTP:          {}", getTotpSecret() != null ? "configured" : "missing");
        logger.info("Redirect URI:  {}", getRedirectUri());

        for (int i = 0; i < 6; i++) {
            String clientId = getClientId(i);
            if (clientId != null) {
                logger.info("API {}: {} - {}", i, getApiName(i),
                        clientId.substring(0, Math.min(8, clientId.length())) + "...");
            }
        }
        logger.info("═══════════════════════════════════════════════════════");
    }
}
