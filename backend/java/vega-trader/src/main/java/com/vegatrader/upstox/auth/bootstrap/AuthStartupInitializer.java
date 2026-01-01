package com.vegatrader.upstox.auth.bootstrap;

import com.vegatrader.upstox.auth.config.AuthConstants;
import com.vegatrader.upstox.auth.repository.TokenRepository;
import com.vegatrader.upstox.auth.state.AuthSessionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Bootstrap component to hydrate AuthSessionState on startup.
 */
@Component
public class AuthStartupInitializer {

    private static final Logger log = LoggerFactory.getLogger(AuthStartupInitializer.class);

    private final TokenRepository tokenRepository;
    private final AuthSessionState authSessionState;

    public AuthStartupInitializer(TokenRepository tokenRepository, AuthSessionState authSessionState) {
        this.tokenRepository = tokenRepository;
        this.authSessionState = authSessionState;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void hydrate() {
        log.info("[AUTH-BOOT] Hydrating tokens from database...");

        try {
            authSessionState.hydrateFromDatabase(
                    tokenRepository.findAllActive(),
                    AuthConstants.API_ORDER);
        } catch (Exception e) {
            log.error("[AUTH-BOOT] Failed to hydrate tokens: {}", e.getMessage(), e);
        }
    }
}
