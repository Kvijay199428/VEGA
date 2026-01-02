package com.vegatrader.util.time;

import java.time.ZoneId;
import java.util.Locale;

public class LocaleConstants {
    public static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    // Add missing constants referenced by DefaultTextFormatter and others
    public static final ZoneId DEFAULT_ZONE = IST;
    public static final Locale DEFAULT_LOCALE = Locale.US;
    public static final Locale ROOT_LOCALE = Locale.ROOT;

    // Constructor
    private LocaleConstants() {
    } // Prevent instantiation
}
