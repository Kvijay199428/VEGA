package com.vegatrader.upstox.auth.service;

import com.vegatrader.upstox.auth.db.ApiName;
import com.vegatrader.upstox.auth.db.UpstoxTokenRepository;
import com.vegatrader.upstox.auth.db.entity.UpstoxTokenEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Token Decision Engine - Core Intelligence.
 * Classifies all tokens as VALID, INVALID, or MISSING.
 *
 * @since 2.2.0
 */
public class TokenDecisionEngine {

    private static final Logger logger = LoggerFactory.getLogger(TokenDecisionEngine.class);

    private final UpstoxTokenRepository repository;
    private final TokenValidityService validityService;
    private final ProfileVerificationService profileService;

    public TokenDecisionEngine(
            UpstoxTokenRepository repository,
            TokenValidityService validityService,
            ProfileVerificationService profileService) {
        this.repository = repository;
        this.validityService = validityService;
        this.profileService = profileService;
    }

    /**
     * Evaluate all tokens and classify them.
     */
    public TokenDecisionReport evaluate() {
        logger.info("Evaluating all tokens...");

        List<UpstoxTokenEntity> tokens = repository.findAll();
        Map<String, UpstoxTokenEntity> tokenMap = tokens.stream()
                .collect(Collectors.toMap(
                        UpstoxTokenEntity::getApiName,
                        Function.identity(),
                        (a, b) -> a));

        List<ApiName> valid = new ArrayList<>();
        List<ApiName> invalid = new ArrayList<>();
        List<ApiName> missing = new ArrayList<>();

        for (ApiName api : ApiName.values()) {
            UpstoxTokenEntity token = tokenMap.get(api.name());

            if (token == null) {
                missing.add(api);
                logger.debug("Token MISSING: {}", api);
                continue;
            }

            // Layer 1: Time-based check
            boolean timeOk = validityService.isTimeValid(token);
            if (!timeOk) {
                invalid.add(api);
                logger.debug("Token EXPIRED (time): {}", api);
                continue;
            }

            // Layer 2: Profile API verification
            boolean profileOk = profileService.isValid(token.getAccessToken());
            if (profileOk) {
                valid.add(api);
                logger.debug("Token VALID: {}", api);
            } else {
                invalid.add(api);
                logger.debug("Token INVALID (profile): {}", api);
            }
        }

        TokenDecisionReport report = new TokenDecisionReport(valid, invalid, missing);
        logger.info("Evaluation complete: {}", report);
        return report;
    }

    /**
     * Evaluate single token.
     */
    public TokenStatus evaluateSingle(ApiName apiName) {
        Optional<UpstoxTokenEntity> tokenOpt = repository.findByApiName(apiName);

        if (tokenOpt.isEmpty()) {
            return TokenStatus.MISSING;
        }

        UpstoxTokenEntity token = tokenOpt.get();

        // Layer 1: Time check
        if (!validityService.isTimeValid(token)) {
            return TokenStatus.EXPIRED;
        }

        // Layer 2: Profile check
        if (!profileService.isValid(token.getAccessToken())) {
            return TokenStatus.INVALID;
        }

        return TokenStatus.VALID;
    }

    /**
     * Token status enum.
     */
    public enum TokenStatus {
        VALID,
        INVALID,
        EXPIRED,
        MISSING
    }
}
