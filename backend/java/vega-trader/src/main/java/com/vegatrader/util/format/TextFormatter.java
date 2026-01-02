package com.vegatrader.util.format;

import java.time.Instant;

/**
 * Text formatting interface for locale-independent, deterministic output.
 * 
 * <p>
 * Ensures consistent formatting across all environments for:
 * <ul>
 * <li>Decimal numbers</li>
 * <li>Currency values</li>
 * <li>Timestamps</li>
 * <li>Percentages</li>
 * </ul>
 * 
 * <p>
 * Critical for:
 * <ul>
 * <li>Market Replay logs</li>
 * <li>Audit trails</li>
 * <li>QA validation</li>
 * <li>Cross-region consistency</li>
 * </ul>
 * 
 * @since 5.0.0
 */
public interface TextFormatter {

    /**
     * Format a decimal number (prices, quantities).
     * 
     * @param value the value to format
     * @return formatted string
     */
    String formatDecimal(double value);

    /**
     * Format a decimal with specific precision.
     * 
     * @param value    the value to format
     * @param decimals number of decimal places
     * @return formatted string
     */
    String formatDecimal(double value, int decimals);

    /**
     * Format a currency value (INR).
     * 
     * @param value the value to format
     * @return formatted currency string
     */
    String formatCurrency(double value);

    /**
     * Format an instant timestamp.
     * 
     * @param timestamp the instant to format
     * @return formatted timestamp string
     */
    String formatInstant(Instant timestamp);

    /**
     * Format a percentage value.
     * 
     * @param value the value (0.0 to 1.0 or 0 to 100)
     * @return formatted percentage string
     */
    String formatPercentage(double value);

    /**
     * Format a quantity (integer-like with thousands separator).
     * 
     * @param quantity the quantity
     * @return formatted quantity string
     */
    String formatQuantity(long quantity);

    /**
     * Convert string to uppercase (locale-safe).
     * 
     * @param value input string
     * @return uppercase string
     */
    String upper(String value);

    /**
     * Convert string to lowercase (locale-safe).
     * 
     * @param value input string
     * @return lowercase string
     */
    String lower(String value);
}
