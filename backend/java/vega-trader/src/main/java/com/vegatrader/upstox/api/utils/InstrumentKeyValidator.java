package com.vegatrader.upstox.api.utils;

import com.vegatrader.upstox.api.instrument.validation.InstrumentKeyPattern;

/**
 * Utility class for validating instrument keys.
 * 
 * <p>
 * Updated in v4.0.0 to use strict regex patterns from
 * {@link InstrumentKeyPattern}.
 *
 * @since 2.0.0
 * @see InstrumentKeyPattern
 */
public final class InstrumentKeyValidator {

    private InstrumentKeyValidator() {
        // Utility class - no instantiation
    }

    /**
     * Validates instrument key format using strict regex pattern.
     * Expected format: SEGMENT|IDENTIFIER (e.g., NSE_EQ|INE528G01035)
     *
     * @param instrumentKey the instrument key to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String instrumentKey) {
        return InstrumentKeyPattern.isValidSingleKey(instrumentKey);
    }

    /**
     * Validates multiple comma-separated instrument keys.
     * 
     * @param instrumentKeys comma-separated keys
     * @return true if all keys are valid
     */
    public static boolean isValidMultiple(String instrumentKeys) {
        return InstrumentKeyPattern.isValidMultiKey(instrumentKeys);
    }

    /**
     * Validates expired instrument key format.
     * Expected format: SEGMENT|SYMBOL|DD-MM-YYYY
     * 
     * @param expiredKey the expired instrument key
     * @return true if valid
     */
    public static boolean isValidExpired(String expiredKey) {
        return InstrumentKeyPattern.isValidExpiredKey(expiredKey);
    }

    /**
     * Validates and throws exception if invalid.
     *
     * @param instrumentKey the instrument key to validate
     * @throws IllegalArgumentException if invalid
     */
    public static void validate(String instrumentKey) {
        if (!isValid(instrumentKey)) {
            throw new IllegalArgumentException(
                    "Invalid instrument key format. Expected: SEGMENT|IDENTIFIER (e.g., NSE_EQ|INE528G01035). " +
                            "Valid segments: " + InstrumentKeyPattern.SEGMENT_VALUES);
        }
    }

    /**
     * Validates multiple keys and throws exception if any is invalid.
     * 
     * @param instrumentKeys comma-separated keys
     * @throws IllegalArgumentException if invalid
     */
    public static void validateMultiple(String instrumentKeys) {
        if (!isValidMultiple(instrumentKeys)) {
            throw new IllegalArgumentException(
                    "Invalid instrument keys format. Expected: SEGMENT|ID,SEGMENT|ID,...");
        }
    }

    /**
     * Extracts segment from instrument key.
     *
     * @param instrumentKey the instrument key
     * @return segment part
     */
    public static String getSegment(String instrumentKey) {
        validate(instrumentKey);
        return InstrumentKeyPattern.extractSegment(instrumentKey);
    }

    /**
     * Extracts exchange from instrument key (alias for getSegment).
     *
     * @param instrumentKey the instrument key
     * @return exchange/segment part
     */
    public static String getExchange(String instrumentKey) {
        return getSegment(instrumentKey);
    }

    /**
     * Extracts identifier from instrument key.
     *
     * @param instrumentKey the instrument key
     * @return identifier part
     */
    public static String getIdentifier(String instrumentKey) {
        validate(instrumentKey);
        return InstrumentKeyPattern.extractIdentifier(instrumentKey);
    }

    /**
     * Splits comma-separated keys into array.
     * 
     * @param instrumentKeys comma-separated keys
     * @return array of individual keys
     */
    public static String[] splitKeys(String instrumentKeys) {
        validateMultiple(instrumentKeys);
        return InstrumentKeyPattern.splitKeys(instrumentKeys);
    }

    /**
     * Checks if instrument is from NSE Equity.
     *
     * @param instrumentKey the instrument key
     * @return true if NSE_EQ
     */
    public static boolean isNSEEquity(String instrumentKey) {
        return instrumentKey != null && instrumentKey.startsWith("NSE_EQ|");
    }

    /**
     * Checks if instrument is from NSE F&O.
     *
     * @param instrumentKey the instrument key
     * @return true if NSE_FO
     */
    public static boolean isNSEFO(String instrumentKey) {
        return instrumentKey != null && instrumentKey.startsWith("NSE_FO|");
    }

    /**
     * Checks if instrument is from BSE Equity.
     *
     * @param instrumentKey the instrument key
     * @return true if BSE_EQ
     */
    public static boolean isBSEEquity(String instrumentKey) {
        return instrumentKey != null && instrumentKey.startsWith("BSE_EQ|");
    }

    /**
     * Checks if instrument is from BSE F&O.
     *
     * @param instrumentKey the instrument key
     * @return true if BSE_FO
     */
    public static boolean isBSEFO(String instrumentKey) {
        return instrumentKey != null && instrumentKey.startsWith("BSE_FO|");
    }

    /**
     * Checks if instrument is from MCX.
     *
     * @param instrumentKey the instrument key
     * @return true if MCX_FO
     */
    public static boolean isMCX(String instrumentKey) {
        return instrumentKey != null && instrumentKey.startsWith("MCX_FO|");
    }

    /**
     * Checks if instrument is an index.
     *
     * @param instrumentKey the instrument key
     * @return true if *_INDEX
     */
    public static boolean isIndex(String instrumentKey) {
        return instrumentKey != null &&
                (instrumentKey.startsWith("NSE_INDEX|") ||
                        instrumentKey.startsWith("BSE_INDEX|") ||
                        instrumentKey.startsWith("MCX_INDEX|"));
    }

    /**
     * Checks if instrument is an equity.
     * 
     * @param instrumentKey the instrument key
     * @return true if *_EQ
     */
    public static boolean isEquity(String instrumentKey) {
        return instrumentKey != null &&
                (instrumentKey.startsWith("NSE_EQ|") || instrumentKey.startsWith("BSE_EQ|"));
    }

    /**
     * Checks if instrument is a derivative (F&O).
     * 
     * @param instrumentKey the instrument key
     * @return true if *_FO
     */
    public static boolean isDerivative(String instrumentKey) {
        return instrumentKey != null &&
                (instrumentKey.startsWith("NSE_FO|") ||
                        instrumentKey.startsWith("BSE_FO|") ||
                        instrumentKey.startsWith("MCX_FO|") ||
                        instrumentKey.startsWith("NCD_FO|") ||
                        instrumentKey.startsWith("BCD_FO|"));
    }

    /**
     * Builds an instrument key.
     *
     * @param segment    the segment
     * @param identifier the identifier
     * @return formatted instrument key
     */
    public static String build(String segment, String identifier) {
        return InstrumentKeyPattern.buildKey(segment, identifier);
    }
}
