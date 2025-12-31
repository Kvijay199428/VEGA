package com.vegatrader.upstox.auth.service;

import com.vegatrader.upstox.auth.db.ApiName;
import com.vegatrader.upstox.auth.db.UpstoxTokenRepository;
import com.vegatrader.upstox.auth.db.UpstoxTokenRepositoryImpl;
import com.vegatrader.upstox.auth.db.entity.UpstoxTokenEntity;
import com.vegatrader.upstox.auth.selenium.v2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Token Generation Service - Orchestrates login automation and persistence.
 * 
 * Modes:
 * - ALL: Generate all 6 tokens
 * - INVALID_ONLY: Generate only invalid/missing tokens
 * - PARTIAL: Generate selected tokens
 *
 * @since 2.2.0
 */
public class TokenGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(TokenGenerationService.class);

    public enum GenerationMode {
        ALL,
        INVALID_ONLY,
        PARTIAL
    }

    private final UpstoxTokenRepository tokenRepository;
    private final TokenDecisionEngine decisionEngine;
    private final TokenValidityService validityService;
    private final EnvConfigLoaderV2 configLoader;

    public TokenGenerationService() {
        this.tokenRepository = new UpstoxTokenRepositoryImpl();
        this.validityService = new TokenValidityService();
        ProfileVerificationService profileService = new ProfileVerificationService();
        this.decisionEngine = new TokenDecisionEngine(tokenRepository, validityService, profileService);
        this.configLoader = new EnvConfigLoaderV2();
    }

    /**
     * Generate all tokens.
     */
    public List<GenerationResult> generateAll() {
        logger.info("Generating ALL tokens...");
        List<ApiName> apis = List.of(ApiName.values());
        return generateForApis(apis);
    }

    /**
     * Generate only invalid/missing tokens.
     */
    public List<GenerationResult> generateInvalidOnly() {
        logger.info("Generating INVALID_ONLY tokens...");
        TokenDecisionReport report = decisionEngine.evaluate();
        return generateForApis(report.getNeedRegeneration());
    }

    /**
     * Generate selected tokens.
     */
    public List<GenerationResult> generatePartial(List<ApiName> apiNames) {
        logger.info("Generating PARTIAL tokens: {}", apiNames);
        return generateForApis(apiNames);
    }

    /**
     * Generate tokens for specified APIs.
     */
    private List<GenerationResult> generateForApis(List<ApiName> apis) {
        List<GenerationResult> results = new ArrayList<>();

        for (ApiName api : apis) {
            try {
                logger.info("═══════════════════════════════════════════════════════");
                logger.info("Generating token for: {}", api);
                logger.info("═══════════════════════════════════════════════════════");

                // Build config for this API
                LoginConfigV2 config = buildConfig(api);
                if (config == null) {
                    logger.warn("No config found for API: {}", api);
                    results.add(new GenerationResult(api, false, "No config found"));
                    continue;
                }

                // Perform login
                OAuthLoginAutomationV2 automation = new OAuthLoginAutomationV2();
                LoginResultV2 loginResult = automation.performLogin(config);

                if (loginResult.isSuccess()) {
                    // CRITICAL: Persist immediately
                    UpstoxTokenEntity entity = UpstoxTokenMapper.from(loginResult, config);
                    tokenRepository.upsertToken(entity);

                    logger.info("✓ Token generated and persisted for: {}", api);
                    results.add(new GenerationResult(api, true, "Success"));
                } else {
                    logger.error("✗ Token generation failed for: {}", api);
                    results.add(new GenerationResult(api, false, loginResult.getErrorMessage()));
                }

                // 5-second halt between tokens per user request
                Thread.sleep(5000);

            } catch (Exception e) {
                logger.error("Error generating token for {}: {}", api, e.getMessage());
                results.add(new GenerationResult(api, false, e.getMessage()));
            }
        }

        return results;
    }

    /**
     * Build login config for API.
     */
    private LoginConfigV2 buildConfig(ApiName api) {
        int index = api.getIndex();

        String clientId = configLoader.getClientId(index);
        String clientSecret = configLoader.getClientSecret(index);

        if (clientId == null || clientSecret == null) {
            return null;
        }

        return LoginConfigV2.builder()
                .apiName(api.name())
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(configLoader.getRedirectUri())
                .credentials(
                        configLoader.getMobileNumber(),
                        configLoader.getPin(),
                        configLoader.getTotpSecret())
                .primary(api.isPrimary())
                .headless(false) // Visible browser
                .build();
    }

    /**
     * Get current token status report.
     */
    public TokenDecisionReport getStatus() {
        return decisionEngine.evaluate();
    }

    /**
     * Generation result for single API.
     */
    public static class GenerationResult {
        private final ApiName apiName;
        private final boolean success;
        private final String message;

        public GenerationResult(ApiName apiName, boolean success, String message) {
            this.apiName = apiName;
            this.success = success;
            this.message = message;
        }

        public ApiName getApiName() {
            return apiName;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return apiName + ": " + (success ? "✓ Success" : "✗ " + message);
        }
    }
}
