package com.vegatrader.util.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Time provider interface for deterministic timestamp generation.
 * 
 * <p>
 * Abstracts time source to enable:
 * <ul>
 * <li>Live mode: System clock</li>
 * <li>Replay mode: Journal event timestamps</li>
 * <li>Testing: Fixed/controllable time</li>
 * </ul>
 * 
 * <p>
 * All critical HFT paths should use this interface instead of direct
 * {@code Instant.now()} or {@code System.currentTimeMillis()} calls.
 * 
 * @since 5.0.0
 */
public interface TimeProvider {

    /**
     * Get current instant.
     * 
     * @return current time as Instant
     */
    Instant now();

    /**
     * Get current time in milliseconds since epoch.
     * 
     * @return current time in millis
     */
    default long millis() {
        return now().toEpochMilli();
    }

    /**
     * Get current time in nanoseconds since epoch.
     * 
     * @return current time in nanos
     */
    default long nanos() {
        Instant instant = now();
        return instant.getEpochSecond() * 1_000_000_000L + instant.getNano();
    }

    /**
     * Get current date in specified timezone.
     * 
     * @param zone the timezone
     * @return current date
     */
    default LocalDate today(ZoneId zone) {
        return now().atZone(zone).toLocalDate();
    }

    /**
     * Get current date-time in specified timezone.
     * 
     * @param zone the timezone
     * @return current date-time
     */
    default LocalDateTime dateTime(ZoneId zone) {
        return now().atZone(zone).toLocalDateTime();
    }
}
