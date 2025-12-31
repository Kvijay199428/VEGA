package com.vegatrader.upstox.auth.controller;

import com.vegatrader.upstox.auth.db.ApiName;
import com.vegatrader.upstox.auth.db.UpstoxTokenRepository;
import com.vegatrader.upstox.auth.db.UpstoxTokenRepositoryImpl;
import com.vegatrader.upstox.auth.db.entity.UpstoxTokenEntity;
import com.vegatrader.upstox.auth.dto.LoginSuccessResponse;
import com.vegatrader.upstox.auth.dto.ProfileView;
import com.vegatrader.upstox.auth.service.ProfileVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Login Success Controller - Displays confirmation after successful login.
 * 
 * GET /api/auth/upstox/login-success/{apiName}
 *
 * @since 2.3.0
 */
@RestController
@RequestMapping("/api/auth/upstox")
public class LoginSuccessController {

    private static final Logger logger = LoggerFactory.getLogger(LoginSuccessController.class);
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final UpstoxTokenRepository tokenRepository;
    private final ProfileVerificationService profileService;

    public LoginSuccessController() {
        this.tokenRepository = new UpstoxTokenRepositoryImpl();
        this.profileService = new ProfileVerificationService();
    }

    /**
     * Get login success info for specific API.
     * 
     * This endpoint is:
     * - Read-only and safe to refresh
     * - Fetches profile if token is valid
     * - Shows token status
     */
    @GetMapping("/login-success/{apiName}")
    public ResponseEntity<LoginSuccessResponse> getLoginSuccess(@PathVariable String apiName) {
        logger.info("Login success request for: {}", apiName);

        try {
            ApiName api = ApiName.fromString(apiName);
            Optional<UpstoxTokenEntity> tokenOpt = tokenRepository.findByApiName(api);

            if (tokenOpt.isEmpty()) {
                return ResponseEntity.ok(LoginSuccessResponse.notFound(apiName));
            }

            UpstoxTokenEntity token = tokenOpt.get();

            // Parse expiry
            Instant tokenExpiry = null;
            if (token.getValidityAt() != null) {
                tokenExpiry = LocalDateTime.parse(token.getValidityAt(), DATETIME_FORMAT)
                        .atZone(IST).toInstant();
            }

            // Fetch fresh profile
            ProfileView profile = fetchProfile(token.getAccessToken());

            LoginSuccessResponse response = LoginSuccessResponse.success(
                    token.getApiName(),
                    tokenExpiry,
                    token.getGeneratedAt(),
                    profile);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(LoginSuccessResponse.notFound(apiName));
        }
    }

    /**
     * Get all login success info (all APIs).
     */
    @GetMapping("/login-success")
    public ResponseEntity<List<LoginSuccessResponse>> getAllLoginSuccess() {
        logger.info("Login success request for all APIs");

        List<UpstoxTokenEntity> tokens = tokenRepository.findAll();

        List<LoginSuccessResponse> responses = tokens.stream()
                .map(token -> {
                    Instant expiry = null;
                    if (token.getValidityAt() != null) {
                        expiry = LocalDateTime.parse(token.getValidityAt(), DATETIME_FORMAT)
                                .atZone(IST).toInstant();
                    }
                    ProfileView profile = fetchProfile(token.getAccessToken());
                    return LoginSuccessResponse.success(
                            token.getApiName(), expiry, token.getGeneratedAt(), profile);
                })
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * Fetch profile using access token.
     */
    private ProfileView fetchProfile(String accessToken) {
        try {
            // For now, return minimal profile
            // TODO: Integrate with actual profile fetch
            if (profileService.isValid(accessToken)) {
                return ProfileView.builder()
                        .broker("UPSTOX")
                        .active(true)
                        .exchanges(List.of("NSE", "NFO", "BSE", "CDS", "BFO", "BCD"))
                        .products(List.of("D", "CO", "I"))
                        .orderTypes(List.of("MARKET", "LIMIT", "SL", "SL-M"))
                        .build();
            }
        } catch (Exception e) {
            logger.warn("Failed to fetch profile: {}", e.getMessage());
        }
        return null;
    }
}
