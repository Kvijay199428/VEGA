package com.vegatrader.upstox.auth.selenium.v2;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Upstox v3 Token Request Client.
 * 
 * This is the FALLBACK/INSTITUTIONAL flow that does NOT require Selenium.
 * 
 * USE WHEN:
 * - CAPTCHA loops
 * - Broker enforcement
 * - Scheduled overnight approval
 * - Compliance-heavy environments
 * 
 * FLOW:
 * 1. POST /v3/login/auth/token/request/{client_id}
 * 2. User notified in Upstox app + WhatsApp
 * 3. User approves
 * 4. Token delivered via webhook
 *
 * @since 2.2.0
 */
public class TokenRequestV3Client {

    private static final Logger logger = LoggerFactory.getLogger(TokenRequestV3Client.class);
    private static final String V3_TOKEN_REQUEST_URL = "https://api.upstox.com/v3/login/auth/token/request/";
    private static final Gson gson = new Gson();

    private final HttpClient httpClient;

    public TokenRequestV3Client() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * Response from v3 token request initiation.
     */
    public static class TokenRequestResponse {
        private boolean success;
        private String message;
        private String requestId;
        private String errorCode;

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getRequestId() {
            return requestId;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }

    /**
     * Initiate v3 token request (fallback flow).
     * 
     * This sends a notification to the user's Upstox app and WhatsApp.
     * User must approve the request within the validity window (until 03:30 AM).
     * Token will be delivered via webhook after approval.
     * 
     * @param clientId     Upstox client ID
     * @param clientSecret Upstox client secret
     * @return TokenRequestResponse indicating if request was accepted
     */
    public TokenRequestResponse initiateTokenRequest(String clientId, String clientSecret) {
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("Initiating v3 Token Request (Institutional Fallback)");
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("Client ID: {}...", clientId.substring(0, Math.min(8, clientId.length())));

        try {
            // Build request body
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("client_secret", clientSecret);

            String url = V3_TOKEN_REQUEST_URL + clientId;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            logger.info("Sending token request to: {}", url);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            logger.info("Response status: {}", response.statusCode());
            logger.debug("Response body: {}", response.body());

            TokenRequestResponse result = new TokenRequestResponse();

            if (response.statusCode() == 200 || response.statusCode() == 202) {
                result.success = true;
                result.message = "Token request sent. User will receive notification in Upstox app and WhatsApp.";

                // Parse response if available
                try {
                    JsonObject respJson = gson.fromJson(response.body(), JsonObject.class);
                    if (respJson.has("request_id")) {
                        result.requestId = respJson.get("request_id").getAsString();
                    }
                } catch (Exception e) {
                    // Ignore parse errors
                }

                logger.info("╔═══════════════════════════════════════════════════════════════╗");
                logger.info("║           TOKEN REQUEST INITIATED SUCCESSFULLY                ║");
                logger.info("╚═══════════════════════════════════════════════════════════════╝");
                logger.info("User will receive notification in Upstox app and WhatsApp.");
                logger.info("Approval window valid until 03:30 AM IST.");
                logger.info("Token will be delivered via webhook after approval.");
            } else {
                result.success = false;
                result.message = "Token request failed: " + response.body();
                result.errorCode = String.valueOf(response.statusCode());

                logger.error("╔═══════════════════════════════════════════════════════════════╗");
                logger.error("║           TOKEN REQUEST FAILED                                ║");
                logger.error("╚═══════════════════════════════════════════════════════════════╝");
                logger.error("Status: {}", response.statusCode());
                logger.error("Body: {}", response.body());
            }

            return result;

        } catch (Exception e) {
            logger.error("Token request error: {}", e.getMessage(), e);

            TokenRequestResponse errorResult = new TokenRequestResponse();
            errorResult.success = false;
            errorResult.message = "Request failed: " + e.getMessage();
            return errorResult;
        }
    }

    /**
     * Log guidance for webhook setup.
     */
    public void logWebhookGuidance() {
        logger.info("");
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("WEBHOOK CONFIGURATION REQUIRED");
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("");
        logger.info("To receive tokens from v3 flow, configure webhook in Upstox app:");
        logger.info("");
        logger.info("Expected webhook payload:");
        logger.info("{");
        logger.info("  \"client_id\": \"...\",");
        logger.info("  \"user_id\": \"...\",");
        logger.info("  \"access_token\": \"...\",");
        logger.info("  \"expires_at\": \"...\",");
        logger.info("  \"message_type\": \"access_token\"");
        logger.info("}");
        logger.info("");
        logger.info("Webhook rules:");
        logger.info("  1. Verify signature (if enabled)");
        logger.info("  2. Match client_id");
        logger.info("  3. Store token");
        logger.info("  4. Mark API key ACTIVE");
        logger.info("");
    }
}
