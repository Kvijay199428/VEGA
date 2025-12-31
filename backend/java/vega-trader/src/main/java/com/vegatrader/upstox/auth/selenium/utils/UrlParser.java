package com.vegatrader.upstox.auth.selenium.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for parsing URLs and extracting query parameters.
 *
 * @since 2.0.0
 */
public final class UrlParser {

    private static final Logger logger = LoggerFactory.getLogger(UrlParser.class);

    private UrlParser() {
        // Utility class
    }

    /**
     * Extract authorization code from callback URL.
     *
     * @param callbackUrl callback URL with code parameter
     * @return authorization code or null
     */
    public static String extractAuthCode(String callbackUrl) {
        if (callbackUrl == null || callbackUrl.isEmpty()) {
            logger.warn("Callback URL is null or empty");
            return null;
        }

        Map<String, String> params = parseQueryParameters(callbackUrl);
        String code = params.get("code");

        if (code != null) {
            logger.info("✓ Extracted authorization code (length={})", code.length());
        } else {
            logger.warn("⚠ Authorization code not found in URL");
        }

        return code;
    }

    /**
     * Extract state parameter from callback URL.
     *
     * @param callbackUrl callback URL
     * @return state value or null
     */
    public static String extractState(String callbackUrl) {
        Map<String, String> params = parseQueryParameters(callbackUrl);
        return params.get("state");
    }

    /**
     * Extract error from callback URL if present.
     *
     * @param callbackUrl callback URL
     * @return error string or null
     */
    public static String extractError(String callbackUrl) {
        Map<String, String> params = parseQueryParameters(callbackUrl);
        return params.get("error");
    }

    /**
     * Extract error description from callback URL.
     *
     * @param callbackUrl callback URL
     * @return error description or null
     */
    public static String extractErrorDescription(String callbackUrl) {
        Map<String, String> params = parseQueryParameters(callbackUrl);
        return params.get("error_description");
    }

    /**
     * Parse all query parameters from URL.
     *
     * @param url URL with query string
     * @return map of parameter name to value
     */
    public static Map<String, String> parseQueryParameters(String url) {
        Map<String, String> params = new HashMap<>();

        if (url == null || url.isEmpty()) {
            return params;
        }

        try {
            // Extract query string
            String query;
            if (url.contains("?")) {
                query = url.substring(url.indexOf("?") + 1);
            } else {
                return params; // No query parameters
            }

            // Remove fragment if present
            if (query.contains("#")) {
                query = query.substring(0, query.indexOf("#"));
            }

            // Parse parameters
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                    params.put(key, value);
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing URL parameters", e);
        }

        return params;
    }

    /**
     * Validate if URL contains authorization code.
     *
     * @param url URL to check
     * @return true if code parameter exists
     */
    public static boolean hasAuthCode(String url) {
        return url != null && (url.contains("code=") || parseQueryParameters(url).containsKey("code"));
    }

    /**
     * Validate if URL contains error parameter.
     *
     * @param url URL to check
     * @return true if error parameter exists
     */
    public static boolean hasError(String url) {
        return url != null && (url.contains("error=") || parseQueryParameters(url).containsKey("error"));
    }
}
