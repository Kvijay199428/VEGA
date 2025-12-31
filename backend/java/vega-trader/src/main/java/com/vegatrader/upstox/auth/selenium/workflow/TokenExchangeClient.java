package com.vegatrader.upstox.auth.selenium.workflow;

import com.google.gson.Gson;
import com.vegatrader.upstox.auth.config.AuthConstants;
import com.vegatrader.upstox.auth.request.TokenExchangeRequest;
import com.vegatrader.upstox.auth.response.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * HTTP client for exchanging authorization code for access token.
 *
 * @since 2.0.0
 */
public class TokenExchangeClient {

    private static final Logger logger = LoggerFactory.getLogger(TokenExchangeClient.class);

    private final HttpClient httpClient;
    private final Gson gson;

    public TokenExchangeClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    /**
     * Exchange authorization code for access token.
     *
     * @param authCode     authorization code
     * @param clientId     client ID (API key)
     * @param clientSecret client secret
     * @param redirectUri  redirect URI
     * @return TokenResponse with access token
     * @throws Exception if exchange fails
     */
    public TokenResponse exchangeCodeForToken(String authCode, String clientId,
            String clientSecret, String redirectUri) throws Exception {
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("Exchanging Authorization Code for Access Token");
        logger.info("═══════════════════════════════════════════════════════");

        // Build request
        TokenExchangeRequest request = TokenExchangeRequest.builder()
                .code(authCode)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(redirectUri)
                .build();

        request.validate();

        String tokenUrl = AuthConstants.API_BASE_URL + AuthConstants.TOKEN_ENDPOINT;
        logger.info("Token endpoint: {}", tokenUrl);

        // Create HTTP request
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(new URI(tokenUrl))
                .header("Content-Type", AuthConstants.CONTENT_TYPE_FORM)
                .header("Accept", AuthConstants.CONTENT_TYPE_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(request.toFormBody()))
                .timeout(Duration.ofSeconds(10))
                .build();

        // Send request
        logger.debug("Sending token exchange request...");
        HttpResponse<String> response = httpClient.send(httpRequest,
                HttpResponse.BodyHandlers.ofString());

        logger.info("Response status: {}", response.statusCode());

        // Check response
        if (response.statusCode() != 200) {
            logger.error("Token exchange failed: {}", response.body());
            throw new RuntimeException("Token exchange failed: " + response.statusCode() +
                    " - " + response.body());
        }

        // Parse response
        TokenResponse tokenResponse = gson.fromJson(response.body(), TokenResponse.class);

        if (tokenResponse.getAccessToken() == null || tokenResponse.getAccessToken().isEmpty()) {
            throw new RuntimeException("No access token in response");
        }

        logger.info("✓ Token exchange successful");
        logger.info("Access token length: {}", tokenResponse.getAccessToken().length());
        logger.info("Token type: {}", tokenResponse.getTokenType());
        logger.info("Expires in: {} seconds", tokenResponse.getExpiresIn());
        logger.info("═══════════════════════════════════════════════════════");

        return tokenResponse;
    }
}
