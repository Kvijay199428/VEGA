package com.vegatrader.config;

import com.vegatrader.util.time.ReplayTimeProvider;
import com.vegatrader.util.time.SystemTimeProvider;
import com.vegatrader.util.time.TimeProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.util.List;

/**
 * Time configuration for live vs replay modes.
 * 
 * <p>
 * Use Spring profiles to switch between:
 * <ul>
 * <li>{@code default} / {@code live}: System clock</li>
 * <li>{@code replay}: Deterministic replay clock</li>
 * </ul>
 * 
 * @since 5.0.0
 */
@Configuration
public class TimeConfig {

    /**
     * Default time provider using system clock.
     * Active for all profiles except 'replay'.
     */
    @Bean
    @Primary
    @Profile("!replay")
    public TimeProvider liveTimeProvider() {
        return new SystemTimeProvider();
    }

    /**
     * Replay time provider for deterministic Market Replay.
     * Active only when 'replay' profile is enabled.
     * 
     * <p>
     * In production, inject journal timestamps dynamically.
     */
    @Bean
    @Profile("replay")
    public TimeProvider replayTimeProvider(@Value("${replay.start-time:2026-01-01T09:15:00Z}") String startTime) {
        // Default placeholder - in real usage, load from journal
        return new ReplayTimeProvider(List.of(
                Instant.parse(startTime),
                Instant.parse(startTime).plusSeconds(1),
                Instant.parse(startTime).plusSeconds(2)));
    }
}
