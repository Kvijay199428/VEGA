package com.vegatrader.util.format;

import com.vegatrader.util.time.LocaleConstants;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Default implementation of TextFormatter.
 * 
 * <p>
 * Uses {@link LocaleConstants} for all formatting to ensure
 * deterministic, locale-independent output across all environments.
 * 
 * <p>
 * Thread-safe: All formatters are immutable or thread-local.
 * 
 * @since 5.0.0
 */
@Component
public class DefaultTextFormatter implements TextFormatter {

    private static final Locale LOCALE = LocaleConstants.DEFAULT_LOCALE;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(LocaleConstants.DEFAULT_ZONE);

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    // Thread-local for thread safety in HFT environment
    private static final ThreadLocal<DecimalFormat> DECIMAL_FORMAT = ThreadLocal.withInitial(() -> {
        DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(LOCALE);
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(4);
        return df;
    });

    private static final ThreadLocal<DecimalFormat> CURRENCY_FORMAT = ThreadLocal.withInitial(() -> {
        DecimalFormat df = (DecimalFormat) NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return df;
    });

    private static final ThreadLocal<DecimalFormat> PERCENTAGE_FORMAT = ThreadLocal.withInitial(() -> {
        DecimalFormat df = new DecimalFormat("0.00%", new java.text.DecimalFormatSymbols(LOCALE));
        return df;
    });

    private static final ThreadLocal<DecimalFormat> QUANTITY_FORMAT = ThreadLocal.withInitial(() -> {
        DecimalFormat df = (DecimalFormat) NumberFormat.getIntegerInstance(LOCALE);
        df.setGroupingUsed(true);
        return df;
    });

    @Override
    public String formatDecimal(double value) {
        return DECIMAL_FORMAT.get().format(value);
    }

    @Override
    public String formatDecimal(double value, int decimals) {
        DecimalFormat df = DECIMAL_FORMAT.get();
        df.setMinimumFractionDigits(decimals);
        df.setMaximumFractionDigits(decimals);
        String result = df.format(value);
        // Reset to default
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(4);
        return result;
    }

    @Override
    public String formatCurrency(double value) {
        return CURRENCY_FORMAT.get().format(value);
    }

    @Override
    public String formatInstant(Instant timestamp) {
        if (timestamp == null) {
            return "null";
        }
        return TIMESTAMP_FORMATTER.format(timestamp);
    }

    @Override
    public String formatPercentage(double value) {
        // If value > 1, assume it's already in percentage form
        double normalizedValue = value > 1.0 ? value / 100.0 : value;
        return PERCENTAGE_FORMAT.get().format(normalizedValue);
    }

    @Override
    public String formatQuantity(long quantity) {
        return QUANTITY_FORMAT.get().format(quantity);
    }

    @Override
    public String upper(String value) {
        return value == null ? null : value.toUpperCase(LocaleConstants.ROOT_LOCALE);
    }

    @Override
    public String lower(String value) {
        return value == null ? null : value.toLowerCase(LocaleConstants.ROOT_LOCALE);
    }

    /**
     * Format instant as ISO-8601 string.
     * 
     * @param timestamp the instant
     * @return ISO formatted string
     */
    public String formatIso(Instant timestamp) {
        return timestamp == null ? "null" : ISO_FORMATTER.format(timestamp);
    }
}
