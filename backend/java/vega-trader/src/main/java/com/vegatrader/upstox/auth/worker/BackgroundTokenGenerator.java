package com.vegatrader.upstox.auth.worker;

import com.vegatrader.upstox.auth.selenium.config.LoginCredentials;
import com.vegatrader.upstox.auth.selenium.config.SeleniumConfig;
import com.vegatrader.upstox.auth.selenium.integration.AuthenticationOrchestrator;
import com.vegatrader.upstox.auth.service.BatchAuthenticationService;
import com.vegatrader.upstox.auth.service.TokenStorageService;
import com.vegatrader.upstox.auth.state.AuthSessionState;
import com.vegatrader.upstox.auth.state.OperatorControlState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Background worker to generate missing tokens without blocking the UI.
 * Respects Operator Kill Switch.
 */
@Component
public class BackgroundTokenGenerator {

    private static final Logger log = LoggerFactory.getLogger(BackgroundTokenGenerator.class);

    private final AuthSessionState authSessionState;
    private final OperatorControlState operatorControlState;
    private final TokenStorageService tokenStorageService;
    private final BatchAuthenticationService batchAuthenticationService;

    public BackgroundTokenGenerator(AuthSessionState authSessionState,
            OperatorControlState operatorControlState,
            TokenStorageService tokenStorageService,
            BatchAuthenticationService batchAuthenticationService) {
        this.authSessionState = authSessionState;
        this.operatorControlState = operatorControlState;
        this.tokenStorageService = tokenStorageService;
        this.batchAuthenticationService = batchAuthenticationService;
    }

    @Value("${upstox.auth.auto.username:}")
    private String autoUsername;

    @Value("${upstox.auth.auto.password:}")
    private String autoPassword;

    @Value("${upstox.auth.auto.totp-secret:}")
    private String autoTotpSecret;

    @Value("${upstox.auth.auto.client-id:}")
    private String autoClientId;

    @Value("${upstox.auth.auto.client-secret:}")
    private String autoClientSecret;

    @Value("${upstox.redirect-uri}")
    private String autoRedirectUri;

    @Async
    public void generateMissingTokens(String sessionId) {
        if (!operatorControlState.isAutomationEnabled()) {
            log.warn("[BG-GEN] Automation disabled, skipping generation");
            return;
        }

        MDC.put("sessionId", sessionId);

        Set<String> missing = authSessionState.getMissingApis();
        log.info("[BG-GEN] Missing APIs: {}", missing);

        if (missing.isEmpty()) {
            log.info("[BG-GEN] No missing tokens.");
            MDC.clear();
            return;
        }

        // Setup common components
        LoginCredentials credentials = new LoginCredentials(autoUsername, autoPassword, autoTotpSecret);
        SeleniumConfig seleniumConfig = new SeleniumConfig("chrome", true); // Headless
        AuthenticationOrchestrator orchestrator = new AuthenticationOrchestrator(seleniumConfig, tokenStorageService);

        for (String api : missing) {
            if (!operatorControlState.isAutomationEnabled()) {
                log.warn("[BG-GEN] Kill-switch activated mid-process, stopping");
                break;
            }

            try {
                // Ideally delegate
                log.info("[BG-GEN] Delegating generation for {} to BatchService", api);
            } catch (Exception e) {
                log.error("[BG-GEN] Failed {}", api, e);
            }
        }

        // Better Approach: Call BatchAuthenticationService to "Fill Missing".
        batchAuthenticationService.generateRemainingTokens(true);

        MDC.clear();
    }
}
