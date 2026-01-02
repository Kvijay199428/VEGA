package com.vegatrader.util.locale;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Locale;

/**
 * Central locale constants for deterministic, locale-independent execution.
 * 
 * <p>
 * All parsers, formatters, and components should reference these constants
 * to ensure consistent behavior across different environments, containers, and
 * JVMs.
 * 
 * <p>
 * Critical for:
 * <ul>
 * <li>Multi-region deployments</li>
 * <li>Regulatory audits</li>
 * <li>Deterministic Market Replay</li>
 * <li>Backtesting consistency</li>
 * </ul>
 * 
 * @since 5.0.0
 */
public final class LocaleConstants {

    /** Default locale for all formatting operations (deterministic, US-based) */
    public static final Locale DEFAULT_LOCALE = Locale.US;

    /** Root locale for case transformations and string operations */
    public static final Locale ROOT_LOCALE = Locale.ROOT;

    /** Default charset for all I/O operations */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /** Default timezone for replay and backtesting (UTC for determinism) */
    public static final ZoneId DEFAULT_ZONE = ZoneId.of("UTC");

    /** Indian Standard Time for live trading context */
    public static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    private LocaleConstants() {
        // Prevent instantiation
    }
}
