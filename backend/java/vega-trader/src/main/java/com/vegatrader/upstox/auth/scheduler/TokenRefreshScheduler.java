package com.vegatrader.upstox.auth.scheduler;

import com.vegatrader.upstox.auth.entity.UpstoxTokenEntity;
import com.vegatrader.upstox.auth.repository.TokenRepository;
import com.vegatrader.upstox.auth.state.OperatorControlState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Scheduler to auto-refresh tokens before they expire.
 * Zero-downtime strategy.
 */
@Component
public class TokenRefreshScheduler {

    private static final Logger log = LoggerFactory.getLogger(TokenRefreshScheduler.class);

    private final TokenRepository tokenRepository;
    private final OperatorControlState operatorControlState;

    public TokenRefreshScheduler(TokenRepository tokenRepository, OperatorControlState operatorControlState) {
        this.tokenRepository = tokenRepository;
        this.operatorControlState = operatorControlState;
    }

    @Scheduled(fixedDelay = 300_000) // every 5 min
    public void refreshExpiringTokens() {
        if (!operatorControlState.isAutomationEnabled()) {
            return;
        }

        List<UpstoxTokenEntity> active = tokenRepository.findAllActive();
        for (UpstoxTokenEntity token : active) {
            if (isExpiringSoon(token)) {
                log.info("[TOKEN-REFRESH] Token {} is expiring soon. Triggering refresh...", token.getApiName());
                // TODO: Implement TokenRefreshService
            }
        }
    }

    private boolean isExpiringSoon(UpstoxTokenEntity token) {
        try {
            LocalDateTime validity = LocalDateTime.parse(token.getValidityAt(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime threshold = LocalDateTime.now().plusMinutes(15);
            return validity.isBefore(threshold);
        } catch (Exception e) {
            return true; // Treat as expiring if invalid
        }
    }
}
