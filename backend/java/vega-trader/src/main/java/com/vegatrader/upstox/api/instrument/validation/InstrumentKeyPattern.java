package com.vegatrader.upstox.api.instrument.validation;

import java.util.regex.Pattern;

/**
 * Defines regex patterns for validating Upstox instrument keys and identifiers.
 * 
 * <p>
 * Based on Upstox Field Pattern Appendix.
 * 
 * @since 4.0.0
 */
public final class InstrumentKeyPattern {

    private InstrumentKeyPattern() {
    } // Utility class

    // --- Segment Values ---

    /**
     * All valid market segments.
     */
    public static final String SEGMENT_VALUES = "NSE_EQ|NSE_FO|NCD_FO|BSE_EQ|BSE_FO|BCD_FO|MCX_FO|NSE_COM|NSE_INDEX|BSE_INDEX|MCX_INDEX";

    // --- Single Instrument Key ---

    /**
     * Pattern for a single instrument key.
     * Format: SEGMENT|IDENTIFIER (e.g., NSE_EQ|INE002A01018)
     */
    public static final String SINGLE_KEY_REGEX = "^(" + SEGMENT_VALUES + ")\\|[\\w ]+$";

    public static final Pattern SINGLE_KEY_PATTERN = Pattern.compile(SINGLE_KEY_REGEX);

    // --- Multiple Instrument Keys (comma-separated) ---

    /**
     * Pattern for comma-separated instrument keys.
     * Used in market quote APIs and WebSocket subscriptions.
     */
    public static final String MULTI_KEY_REGEX = "^(" + SEGMENT_VALUES + ")\\|[\\w ]+(,(" + SEGMENT_VALUES
            + ")\\|[\\w ]+)*$";

    public static final Pattern MULTI_KEY_PATTERN = Pattern.compile(MULTI_KEY_REGEX);

    // --- Expired Instrument Key ---

    /**
     * Pattern for expired instrument key.
     * Format: SEGMENT|SYMBOL|DD-MM-YYYY (e.g., NSE_FO|RELIANCE|27-06-2024)
     */
    public static final String EXPIRED_KEY_REGEX = "^(" + SEGMENT_VALUES
            + ")\\|[\\w\\d\\-]+\\|(0[1-9]|[12]\\d|3[01])-(0[1-9]|1[0-2])-(\\d{4})$";

    public static final Pattern EXPIRED_KEY_PATTERN = Pattern.compile(EXPIRED_KEY_REGEX);

    // --- Exchange Values ---

    /**
     * Valid exchange codes.
     */
    public static final String EXCHANGE_VALUES = "NSE|NFO|CDS|BSE|BFO|BCD|MCX|NSCOM";

    public static final Pattern EXCHANGE_PATTERN = Pattern.compile("^(" + EXCHANGE_VALUES + ")$");

    // --- Validation Methods ---

    /**
     * Validates a single instrument key.
     */
    public static boolean isValidSingleKey(String key) {
        return key != null && SINGLE_KEY_PATTERN.matcher(key).matches();
    }

    /**
     * Validates comma-separated instrument keys.
     */
    public static boolean isValidMultiKey(String keys) {
        return keys != null && MULTI_KEY_PATTERN.matcher(keys).matches();
    }

    /**
     * Validates an expired instrument key.
     */
    public static boolean isValidExpiredKey(String key) {
        return key != null && EXPIRED_KEY_PATTERN.matcher(key).matches();
    }

    /**
     * Validates an exchange code.
     */
    public static boolean isValidExchange(String exchange) {
        return exchange != null && EXCHANGE_PATTERN.matcher(exchange).matches();
    }

    /**
     * Extracts segment from instrument key.
     */
    public static String extractSegment(String instrumentKey) {
        if (instrumentKey == null || !instrumentKey.contains("|")) {
            return null;
        }
        return instrumentKey.substring(0, instrumentKey.indexOf('|'));
    }

    /**
     * Extracts identifier from instrument key.
     */
    public static String extractIdentifier(String instrumentKey) {
        if (instrumentKey == null || !instrumentKey.contains("|")) {
            return null;
        }
        return instrumentKey.substring(instrumentKey.indexOf('|') + 1);
    }

    /**
     * Splits comma-separated keys into array.
     */
    public static String[] splitKeys(String multiKey) {
        if (multiKey == null || multiKey.isEmpty()) {
            return new String[0];
        }
        return multiKey.split(",");
    }

    /**
     * Builds an instrument key from segment and identifier.
     */
    public static String buildKey(String segment, String identifier) {
        if (segment == null || identifier == null) {
            throw new IllegalArgumentException("Segment and identifier cannot be null");
        }
        return segment + "|" + identifier;
    }
}
