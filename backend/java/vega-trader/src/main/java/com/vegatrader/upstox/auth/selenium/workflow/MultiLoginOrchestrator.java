package com.vegatrader.upstox.auth.selenium.workflow;

import com.vegatrader.upstox.auth.selenium.config.LoginCredentials;
import com.vegatrader.upstox.auth.selenium.config.SeleniumConfig;
import com.vegatrader.upstox.auth.selenium.integration.ApiConfig;
import com.vegatrader.upstox.auth.selenium.integration.AuthenticationOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Multi-login orchestrator for multiple API configurations.
 * Sequentially authenticates all API configs (PRIMARY, WEBSOCKET1-3,
 * OPTIONCHAIN1-2).
 *
 * @since 2.0.0
 */
public class MultiLoginOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(MultiLoginOrchestrator.class);

    private final List<ApiConfig> apiConfigs;
    private final LoginCredentials credentials;
    private final SeleniumConfig seleniumConfig;
    private final AuthenticationOrchestrator authOrchestrator;

    private final List<String> successfulLogins = new ArrayList<>();
    private final List<String> failedLogins = new ArrayList<>();

    public MultiLoginOrchestrator(List<ApiConfig> apiConfigs, LoginCredentials credentials,
            SeleniumConfig seleniumConfig,
            AuthenticationOrchestrator authOrchestrator) {
        this.apiConfigs = apiConfigs;
        this.credentials = credentials;
        this.seleniumConfig = seleniumConfig;
        this.authOrchestrator = authOrchestrator;
    }

    /**
     * Perform login for all API configurations.
     *
     * @return summary of results
     */
    public MultiLoginResult loginAll() {
        logger.info("╔═══════════════════════════════════════════════════════╗");
        logger.info("║  MULTI-LOGIN ORCHESTRATOR  ║");
        logger.info("║  Total APIs: {}                                        ║", apiConfigs.size());
        logger.info("╚═══════════════════════════════════════════════════════╝");

        successfulLogins.clear();
        failedLogins.clear();

        for (int i = 0; i < apiConfigs.size(); i++) {
            ApiConfig config = apiConfigs.get(i);

            logger.info("");
            logger.info("┌───────────────────────────────────────────────────────┐");
            logger.info("│ API {}/{}: {}                                   │",
                    (i + 1), apiConfigs.size(), config.getApiName());
            logger.info("└───────────────────────────────────────────────────────┘");

            try {
                authOrchestrator.authenticate(
                        config.getApiName(),
                        config.getClientId(),
                        config.getClientSecret(),
                        config.getRedirectUri(),
                        credentials,
                        config.isPrimary());

                successfulLogins.add(config.getApiName());
                logger.info("✓ SUCCESS: {}", config.getApiName());

            } catch (Exception e) {
                failedLogins.add(config.getApiName());
                logger.error("✗ FAILED: {}", config.getApiName());
                logger.error("Error: {}", e.getMessage());
            }

            // Delay between logins to avoid rate limiting
            if (i < apiConfigs.size() - 1) {
                try {
                    logger.info("Waiting 5 seconds before next login...");
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // Print summary
        logger.info("");
        logger.info("╔═══════════════════════════════════════════════════════╗");
        logger.info("║  MULTI-LOGIN SUMMARY  ║");
        logger.info("╠═══════════════════════════════════════════════════════╣");
        logger.info("║  Total APIs: {}                                        ║", apiConfigs.size());
        logger.info("║  Successful: {}                                        ║", successfulLogins.size());
        logger.info("║  Failed: {}                                            ║", failedLogins.size());
        logger.info("╚═══════════════════════════════════════════════════════╝");

        if (!successfulLogins.isEmpty()) {
            logger.info("Successful logins:");
            successfulLogins.forEach(api -> logger.info("  ✓ {}", api));
        }

        if (!failedLogins.isEmpty()) {
            logger.error("Failed logins:");
            failedLogins.forEach(api -> logger.error("  ✗ {}", api));
        }

        return new MultiLoginResult(successfulLogins, failedLogins);
    }

    /**
     * Multi-login result summary.
     */
    public static class MultiLoginResult {
        private final List<String> successful;
        private final List<String> failed;

        public MultiLoginResult(List<String> successful, List<String> failed) {
            this.successful = new ArrayList<>(successful);
            this.failed = new ArrayList<>(failed);
        }

        public List<String> getSuccessful() {
            return successful;
        }

        public List<String> getFailed() {
            return failed;
        }

        public boolean isAllSuccessful() {
            return failed.isEmpty();
        }

        public int getTotalCount() {
            return successful.size() + failed.size();
        }

        public int getSuccessCount() {
            return successful.size();
        }

        public int getFailedCount() {
            return failed.size();
        }
    }
}
