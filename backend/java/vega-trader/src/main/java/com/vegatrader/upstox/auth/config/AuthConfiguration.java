package com.vegatrader.upstox.auth.config;

import com.vegatrader.upstox.auth.provider.TokenProvider;
import com.vegatrader.upstox.auth.service.TokenValidationService;
import com.vegatrader.upstox.auth.scheduler.TokenRefreshScheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot configuration for authentication components.
 *
 * @since 2.0.0
 */
@Configuration
@EnableScheduling
public class AuthConfiguration {

    /**
     * Token provider bean (singleton).
     */
    @Bean
    public TokenProvider tokenProvider() {
        return new TokenProvider();
    }

    // NOTE: TokenStorageService is now @Service annotated and auto-wired by Spring

    /**
     * Token validation service bean.
     */
    @Bean
    public TokenValidationService tokenValidationService() {
        return new TokenValidationService();
    }

    /**
     * Token refresh scheduler bean.
     */
    @Bean
    public TokenRefreshScheduler tokenRefreshScheduler(TokenProvider tokenProvider) {
        return new TokenRefreshScheduler(tokenProvider);
    }
}
