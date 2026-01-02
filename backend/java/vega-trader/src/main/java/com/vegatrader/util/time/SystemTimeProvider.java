package com.vegatrader.util.time;

import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * System clock implementation of TimeProvider.
 * 
 * <p>
 * Uses the actual system clock for live trading operations.
 * This is the default provider injected by Spring.
 * 
 * @since 5.0.0
 */
@Component("systemTimeProvider")
public class SystemTimeProvider implements TimeProvider {

    @Override
    public Instant now() {
        return Instant.now();
    }

    @Override
    public long millis() {
        return System.currentTimeMillis();
    }

    @Override
    public long nanos() {
        return System.nanoTime();
    }
}
