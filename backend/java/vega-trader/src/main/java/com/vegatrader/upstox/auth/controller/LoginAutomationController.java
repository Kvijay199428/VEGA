package com.vegatrader.upstox.auth.controller;

import com.vegatrader.upstox.auth.selenium.config.LoginCredentials;
import com.vegatrader.upstox.auth.selenium.config.SeleniumConfig;
import com.vegatrader.upstox.auth.selenium.integration.ApiConfig;
import com.vegatrader.upstox.auth.selenium.integration.AuthenticationOrchestrator;
import com.vegatrader.upstox.auth.selenium.workflow.MultiLoginOrchestrator;
import com.vegatrader.upstox.auth.response.TokenResponse;
import com.vegatrader.upstox.auth.service.TokenStorageService;
import com.vegatrader.upstox.auth.service.BatchAuthenticationService;
import com.vegatrader.upstox.auth.service.BatchAuthenticationService.BatchAuthResult;
import com.vegatrader.upstox.auth.event.AuthProgressEvent;
import com.vegatrader.upstox.auth.event.AuthProgressPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for login automation endpoints.
 * 
 * Base URL: http://localhost:28020/api/v1/auth/selenium
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/auth/selenium")
public class LoginAutomationController {

    private static final Logger logger = LoggerFactory.getLogger(LoginAutomationController.class);

    @Autowired
    private TokenStorageService tokenStorageService;

    @Autowired
    private BatchAuthenticationService batchAuthService;

    @Autowired
    private AuthProgressPublisher progressPublisher;

    /**
     * Initiate single API login.
     *
     * POST http://localhost:28021/api/v1/auth/selenium/login
     */
    @org.springframework.beans.factory.annotation.Value("${upstox.auth.auto.username:}")
    private String autoUsername;

    @org.springframework.beans.factory.annotation.Value("${upstox.auth.auto.password:}")
    private String autoPassword;

    @org.springframework.beans.factory.annotation.Value("${upstox.auth.auto.totp-secret:}")
    private String autoTotpSecret;

    @org.springframework.beans.factory.annotation.Value("${upstox.auth.auto.client-id:}")
    private String autoClientId;

    @org.springframework.beans.factory.annotation.Value("${upstox.auth.auto.client-secret:}")
    private String autoClientSecret;

    @org.springframework.beans.factory.annotation.Value("${upstox.redirect-uri}")
    private String autoRedirectUri;

    @PostMapping("/login")
    public ResponseEntity<?> loginSingleApi(@RequestBody LoginRequest request) {
        logger.info("Initiating login for API: {}", request.getApiName());

        try {
            // Check for AUTO mode and populate if needed
            if (isAuto(request.getUsername()))
                request.setUsername(autoUsername);
            if (isAuto(request.getPassword()))
                request.setPassword(autoPassword);
            if (isAuto(request.getTotpSecret()))
                request.setTotpSecret(autoTotpSecret);

            // Auto-fill redirect URI if missing
            if (request.getRedirectUri() == null || request.getRedirectUri().isEmpty()) {
                request.setRedirectUri(autoRedirectUri);
            }

            // Auto-fill Client ID/Secret if missing or default
            if (isAuto(request.getClientId())) {
                request.setClientId(autoClientId);
            }
            if (isAuto(request.getClientSecret())) {
                request.setClientSecret(autoClientSecret);
            }

            // Validate request
            request.validate();

            // Create credentials
            LoginCredentials credentials = new LoginCredentials(
                    request.getUsername(),
                    request.getPassword(),
                    request.getTotpSecret());

            // Create Selenium config
            SeleniumConfig seleniumConfig = new SeleniumConfig(
                    request.getBrowser() != null ? request.getBrowser() : "chrome",
                    request.isHeadless());

            // Create orchestrator
            AuthenticationOrchestrator orchestrator = new AuthenticationOrchestrator(
                    seleniumConfig, tokenStorageService);

            // Perform authentication
            TokenResponse token = orchestrator.authenticate(
                    request.getApiName(),
                    request.getClientId(),
                    request.getClientSecret(),
                    request.getRedirectUri(),
                    credentials,
                    request.isPrimary());

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Login successful");
            response.put("apiName", request.getApiName());
            response.put("tokenType", token.getTokenType());
            response.put("expiresIn", token.getExpiresIn());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Login failed", e);

            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Login failed: " + e.getMessage());

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Batch login for all configured APIs.
     * Authenticates all discovered Upstox apps and generates tokens for each.
     *
     * POST http://localhost:28020/api/v1/auth/selenium/batch-login?headless=true
     */
    @PostMapping("/batch-login")
    public ResponseEntity<?> batchLogin(@RequestParam(defaultValue = "false") boolean headless) {
        logger.info("AUDIT: Batch login initiated | Headless: {}", headless);
        long startTime = System.currentTimeMillis();

        try {
            BatchAuthResult result = batchAuthService.startBatchLogin(headless);
            long duration = System.currentTimeMillis() - startTime;

            logger.info("AUDIT: Batch login completed | Duration: {}ms | Success: {}/{} | Errors: {}",
                    duration, result.getSuccessfulTokens(), result.getTotalApis(), result.getErrors().size());

            Map<String, Object> response = new HashMap<>();
            response.put("status", result.isFullyAuthenticated() ? "success" : "partial");
            response.put("totalApis", result.getTotalApis());
            response.put("successfulTokens", result.getSuccessfulTokens());
            response.put("authenticated", result.isFullyAuthenticated());
            response.put("errors", result.getErrors());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("AUDIT: Batch login failed", e);

            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Batch login failed: " + e.getMessage());

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Async batch login: Generate PRIMARY first, remaining in background.
     * Returns immediately after PRIMARY token is generated (~15s).
     * 
     * POST
     * http://localhost:28020/api/v1/auth/selenium/batch-login-async?headless=true
     */
    @PostMapping("/batch-login-async")
    public ResponseEntity<?> batchLoginAsync(@RequestParam(defaultValue = "false") boolean headless) {
        logger.info("AUDIT: Async batch login initiated | Headless: {}", headless);
        long startTime = System.currentTimeMillis();

        try {
            BatchAuthResult result = batchAuthService.startBatchLoginAsync(headless);
            long duration = System.currentTimeMillis() - startTime;

            logger.info("AUDIT: Async batch login PRIMARY complete | Duration: {}ms", duration);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "primary_generated");
            response.put("message", "PRIMARY token generated, remaining tokens in background");
            response.put("primaryGenerated", result.getSuccessfulTokens() > 0);
            response.put("backgroundInProgress", true);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("AUDIT: Async batch login failed", e);

            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Async batch login failed: " + e.getMessage());

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Subscribe to real-time auth progress events (SSE).
     * 
     * GET http://localhost:28020/api/v1/auth/selenium/progress
     */
    @GetMapping(value = "/progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<AuthProgressEvent>> streamProgress() {
        return progressPublisher.getStream()
                .map(event -> ServerSentEvent.<AuthProgressEvent>builder()
                        .event("auth-progress")
                        .data(event)
                        .build())
                .doOnSubscribe(subscription -> logger.info("[SSE] Client subscribed to auth progress"))
                .doOnCancel(() -> logger.info("[SSE] Client disconnected from auth progress"));
    }

    private boolean isAuto(String value) {
        return value == null || value.isEmpty() || "AUTO".equalsIgnoreCase(value) || "default".equalsIgnoreCase(value);
    }

    /**
     * Proceed with Primary token and generate remaining in background.
     * 
     * POST http://localhost:28020/api/v1/auth/selenium/proceed-primary
     */
    @PostMapping("/proceed-primary")
    public ResponseEntity<?> proceedWithPrimary() {
        logger.info("Proceed with primary request received");

        if (!batchAuthService.isPrimaryReady()) {
            return ResponseEntity.badRequest().body("Primary token is not active");
        }

        // Trigger background generation for remaining tokens (Headless)
        new Thread(() -> {
            try {
                logger.info("Starting background token generation for remaining APIs...");
                batchAuthService.generateRemainingTokens(true);
            } catch (Exception e) {
                logger.error("Error in background token generation", e);
            }
        }).start();

        return ResponseEntity.ok(Map.of("message", "Proceeding with Primary. Background generation started."));
    }

    /**
     * Initiate multi-login for multiple APIs.
     *
     * POST http://localhost:28021/api/v1/auth/selenium/multi-login
     */
    @PostMapping("/multi-login")
    public ResponseEntity<?> loginMultipleApis(@RequestBody MultiLoginRequest request) {
        logger.info("Initiating multi-login for {} APIs", request.getApiConfigs().size());

        try {
            // Create credentials
            LoginCredentials credentials = new LoginCredentials(
                    request.getUsername(),
                    request.getPassword(),
                    request.getTotpSecret());

            // Create Selenium config
            SeleniumConfig seleniumConfig = new SeleniumConfig("chrome", true);

            // Create orchestrator
            AuthenticationOrchestrator authOrchestrator = new AuthenticationOrchestrator(
                    seleniumConfig, tokenStorageService);

            // Create multi-login orchestrator
            MultiLoginOrchestrator multiLogin = new MultiLoginOrchestrator(
                    request.getApiConfigs(),
                    credentials,
                    seleniumConfig,
                    authOrchestrator);

            // Perform all logins
            MultiLoginOrchestrator.MultiLoginResult result = multiLogin.loginAll();

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("status", result.isAllSuccessful() ? "success" : "partial");
            response.put("total", result.getTotalCount());
            response.put("successful", result.getSuccessCount());
            response.put("failed", result.getFailedCount());
            response.put("successfulApis", result.getSuccessful());
            response.put("failedApis", result.getFailed());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Multi-login failed", e);

            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Multi-login failed: " + e.getMessage());

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Login request DTO.
     */
    public static class LoginRequest {
        private String apiName;
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String username;
        private String password;
        private String totpSecret;
        private boolean isPrimary;
        private String browser;
        private boolean headless = false;

        public void validate() {
            if (apiName == null || apiName.isEmpty()) {
                throw new IllegalArgumentException("apiName is required");
            }
            if (clientId == null || clientId.isEmpty()) {
                throw new IllegalArgumentException("clientId is required");
            }
            if (username == null || username.isEmpty()) {
                throw new IllegalArgumentException("username is required");
            }
            if (password == null || password.isEmpty()) {
                throw new IllegalArgumentException("password is required");
            }
        }

        // Getters/Setters
        public String getApiName() {
            return apiName;
        }

        public void setApiName(String apiName) {
            this.apiName = apiName;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getTotpSecret() {
            return totpSecret;
        }

        public void setTotpSecret(String totpSecret) {
            this.totpSecret = totpSecret;
        }

        public boolean isPrimary() {
            return isPrimary;
        }

        public void setPrimary(boolean primary) {
            isPrimary = primary;
        }

        public String getBrowser() {
            return browser;
        }

        public void setBrowser(String browser) {
            this.browser = browser;
        }

        public boolean isHeadless() {
            return headless;
        }

        public void setHeadless(boolean headless) {
            this.headless = headless;
        }
    }

    /**
     * Multi-login request DTO.
     */
    public static class MultiLoginRequest {
        private List<ApiConfig> apiConfigs;
        private String username;
        private String password;
        private String totpSecret;

        // Getters/Setters
        public List<ApiConfig> getApiConfigs() {
            return apiConfigs;
        }

        public void setApiConfigs(List<ApiConfig> apiConfigs) {
            this.apiConfigs = apiConfigs;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getTotpSecret() {
            return totpSecret;
        }

        public void setTotpSecret(String totpSecret) {
            this.totpSecret = totpSecret;
        }
    }
}
