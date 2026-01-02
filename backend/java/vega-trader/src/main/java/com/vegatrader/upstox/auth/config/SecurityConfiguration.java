package com.vegatrader.upstox.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security Configuration for Vega Trader.
 * Configures CORS and public access to auth endpoints.
 *
 * @since 2.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Disable CSRF for local API usage
                .csrf(csrf -> csrf.disable())
                // Configure route permissions
                .authorizeHttpRequests(auth -> auth
                        // Allow all Auth endpoints (Session, Login, Generate)
                        .requestMatchers("/api/auth/**").permitAll()
                        // Allow Selenium endpoints
                        .requestMatchers("/api/v1/auth/selenium/**").permitAll()
                        // Allow WebSocket endpoints
                        .requestMatchers("/ws/**").permitAll()
                        // Allow Option Chain endpoints (public data)
                        .requestMatchers("/api/v1/option-chain/**").permitAll()
                        // Allow Actuator/Metrics
                        .requestMatchers("/actuator/**").permitAll()
                        // Require auth for everything else (Order, Admin) - Optional, can be permitAll
                        // for now for Desktop App
                        .anyRequest().permitAll());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow Frontend
        configuration.setAllowedOrigins(
                Arrays.asList("http://localhost:5173", "http://localhost:5174", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
