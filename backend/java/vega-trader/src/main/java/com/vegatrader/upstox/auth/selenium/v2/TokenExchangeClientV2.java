package com.vegatrader.upstox.auth.selenium.v2;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Client for exchanging authorization code for access token.
 * Calls Upstox token endpoint to exchange OAuth code.
 *
 * @since 2.1.0
 */
public class TokenExchangeClientV2 {

    private static final Logger logger = LoggerFactory.getLogger(TokenExchangeClientV2.class);

    private static final String TOKEN_URL = "https://api.upstox.com/v2/login/authorization/token";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final HttpClient httpClient;
    private final Gson gson;

    public TokenExchangeClientV2() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
        this.gson = new Gson();
    }

    /**
     * Exchange authorization code for access token.
     * 
     * @param code         authorization code from OAuth redirect
     * @param clientId     Upstox API client ID
     * @param clientSecret Upstox API client secret
     * @param redirectUri  callback redirect URI
     * @return TokenResponseV2 with access token details
     * @throws RuntimeException if exchange fails
     */
    public TokenResponseV2 exchangeCode(String code, String clientId,
            String clientSecret, String redirectUri) {
        logger.info("Exchanging authorization code for access token");
        logger.debug("Client ID: {}", clientId);

        try {
            // Build form data
            String formData = String.format(
                    "code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=authorization_code",
                    urlEncode(code),
                    urlEncode(clientId),
                    urlEncode(clientSecret),
                    urlEncode(redirectUri));

            // Create request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TOKEN_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .timeout(TIMEOUT)
                    .build();

            // Execute request
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            String body = response.body();

            logger.debug("Token exchange response: {} - {}", statusCode, body);

            if (statusCode != 200) {
                logger.error("Token exchange failed: {} - {}", statusCode, body);
                throw new RuntimeException("Token exchange failed with status " + statusCode + ": " + body);
            }

            // Parse response
            TokenResponseV2 tokenResponse = parseTokenResponse(body);
            logger.info("✓ Access token obtained (expires in: {} seconds)", tokenResponse.getExpiresIn());

            return tokenResponse;

        } catch (IOException | InterruptedException e) {
            logger.error("Token exchange request failed", e);
            throw new RuntimeException("Token exchange request failed: " + e.getMessage(), e);
        }
    }

    /**
     * Parse token response JSON.
     * 
     * @param json response body
     * @return TokenResponseV2
     */
    private TokenResponseV2 parseTokenResponse(String json) {
        try {
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

            TokenResponseV2 response = new TokenResponseV2();

            if (jsonObject.has("access_token")) {
                response.setAccessToken(jsonObject.get("access_token").getAsString());
            }
            if (jsonObject.has("refresh_token")) {
                response.setRefreshToken(jsonObject.get("refresh_token").getAsString());
            }
            if (jsonObject.has("token_type")) {
                response.setTokenType(jsonObject.get("token_type").getAsString());
            }
            if (jsonObject.has("expires_in")) {
                response.setExpiresIn(jsonObject.get("expires_in").getAsLong());
            }

            return response;

        } catch (Exception e) {
            logger.error("Failed to parse token response", e);
            throw new RuntimeException("Failed to parse token response: " + e.getMessage(), e);
        }
    }

    /**
     * URL encode a string.
     * 
     * @param value string to encode
     * @return URL encoded string
     */
    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * Token response DTO.
     */
    public static class TokenResponseV2 {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private long expiresIn;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        public long getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(long expiresIn) {
            this.expiresIn = expiresIn;
        }

        @Override
        public String toString() {
            return "TokenResponseV2{" +
                    "accessToken='***"
                    + (accessToken != null ? accessToken.substring(accessToken.length() - 10) : "null") + "'" +
                    ", tokenType='" + tokenType + '\'' +
                    ", expiresIn=" + expiresIn +
                    '}';
        }
    }
}
