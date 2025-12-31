package com.vegatrader.upstox.api.expired.service;

import com.vegatrader.upstox.api.expired.model.*;
import com.vegatrader.upstox.api.settings.service.UserSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ExpiredInstrumentFetcher - Integrates expired instruments with user settings.
 * Implements the integration flow from b1.md/b2.md specs.
 * 
 * Flow: UserSettings → SectorFilter → ExpiredInstruments → HistoricalData
 * 
 * @since 4.4.0
 */
@Service
public class ExpiredInstrumentFetcher {

    private static final Logger logger = LoggerFactory.getLogger(ExpiredInstrumentFetcher.class);

    private final UserSettingsService settingsService;
    private final ExpiredInstrumentService expiredService;
    private final HistoricalMarketDataService marketDataService;

    public ExpiredInstrumentFetcher(
            UserSettingsService settingsService,
            ExpiredInstrumentService expiredService,
            HistoricalMarketDataService marketDataService) {
        this.settingsService = settingsService;
        this.expiredService = expiredService;
        this.marketDataService = marketDataService;
    }

    /**
     * Fetch historical data for an underlying instrument using user settings.
     * 
     * @param userId        User ID for settings lookup
     * @param underlyingKey e.g., "NSE_INDEX|Nifty 50"
     * @return List of candles based on user preferences
     */
    public List<Candle> fetchWithUserSettings(String userId, String underlyingKey) {
        // Step 1: Load user settings
        ExpiredFetchSettings settings = loadUserSettings(userId);
        logger.info("Fetching expired data for {} with settings: interval={}, maxDays={}",
                underlyingKey, settings.interval(), settings.maxHistoricalDays());

        // Step 2: Fetch expiries
        List<LocalDate> expiries = expiredService.fetchExpiries(underlyingKey);
        if (expiries.isEmpty()) {
            logger.warn("No expiries found for {}", underlyingKey);
            return List.of();
        }

        // Step 3: Filter by maxHistoricalDays
        LocalDate cutoffDate = LocalDate.now().minusDays(settings.maxHistoricalDays());
        List<LocalDate> filteredExpiries = expiries.stream()
                .filter(e -> e.isAfter(cutoffDate))
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        // Step 4: Apply "latest" or "all" setting
        if ("latest".equalsIgnoreCase(settings.expiryFetch()) && !filteredExpiries.isEmpty()) {
            filteredExpiries = List.of(filteredExpiries.get(0));
        }

        // Step 5: Fetch contracts based on instrument type
        List<String> expiredKeys = new ArrayList<>();
        for (LocalDate expiry : filteredExpiries) {
            if ("options".equalsIgnoreCase(settings.instrumentType())
                    || "both".equalsIgnoreCase(settings.instrumentType())) {
                expiredKeys.addAll(fetchOptionKeys(underlyingKey, expiry));
            }
            if ("futures".equalsIgnoreCase(settings.instrumentType())
                    || "both".equalsIgnoreCase(settings.instrumentType())) {
                expiredKeys.addAll(fetchFutureKeys(underlyingKey, expiry));
            }
        }

        // Step 6: Fetch historical candles
        List<Candle> allCandles = new ArrayList<>();
        LocalDate fromDate = cutoffDate;
        LocalDate toDate = LocalDate.now();

        for (String expiredKey : expiredKeys) {
            List<Candle> candles = marketDataService.fetchExpiredHistoricalCandles(
                    expiredKey, settings.interval(), fromDate, toDate);
            allCandles.addAll(candles);
        }

        logger.info("Fetched {} candles for {} ({} expired keys)",
                allCandles.size(), underlyingKey, expiredKeys.size());
        return allCandles;
    }

    /**
     * Fetch expiries only for an underlying.
     */
    public List<LocalDate> fetchExpiries(String userId, String underlyingKey) {
        ExpiredFetchSettings settings = loadUserSettings(userId);
        List<LocalDate> expiries = expiredService.fetchExpiries(underlyingKey);

        // Apply maxHistoricalDays filter
        LocalDate cutoffDate = LocalDate.now().minusDays(settings.maxHistoricalDays());
        return expiries.stream()
                .filter(e -> e.isAfter(cutoffDate))
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    /**
     * Fetch expired option contracts for a specific expiry.
     */
    public List<ExpiredOptionContract> fetchOptions(String userId, String underlyingKey, LocalDate expiry) {
        ExpiredFetchSettings settings = loadUserSettings(userId);
        List<ExpiredOptionContract> options = expiredService.fetchExpiredOptions(underlyingKey, expiry);

        // Filter weekly options if setting is disabled
        if (!settings.showWeeklyOptions()) {
            options = options.stream()
                    .filter(o -> isMonthlyExpiry(o.expiry()))
                    .collect(Collectors.toList());
        }

        return options;
    }

    /**
     * Fetch expired future contracts for a specific expiry.
     */
    public List<ExpiredFutureContract> fetchFutures(String userId, String underlyingKey, LocalDate expiry) {
        return expiredService.fetchExpiredFutures(underlyingKey, expiry);
    }

    /**
     * Fetch historical candles for a specific expired instrument.
     */
    public List<Candle> fetchCandles(String userId, String expiredInstrumentKey, LocalDate fromDate, LocalDate toDate) {
        ExpiredFetchSettings settings = loadUserSettings(userId);
        return marketDataService.fetchExpiredHistoricalCandles(
                expiredInstrumentKey, settings.interval(), fromDate, toDate);
    }

    // === Private Helpers ===

    private ExpiredFetchSettings loadUserSettings(String userId) {
        String expiryFetch = settingsService.getSetting(userId, "expired.default_expiry_fetch");
        String instrumentType = settingsService.getSetting(userId, "expired.default_instrument_type");
        String interval = settingsService.getSetting(userId, "expired.default_interval");
        int maxDays = settingsService.getInt(userId, "expired.max_historical_days", 365);
        boolean showWeekly = settingsService.getBoolean(userId, "expired.show_weekly_options", true);
        boolean cacheExpiries = settingsService.getBoolean(userId, "expired.auto_cache_expiries", true);
        boolean cacheContracts = settingsService.getBoolean(userId, "expired.auto_cache_contracts", true);

        return new ExpiredFetchSettings(
                expiryFetch != null ? expiryFetch : "latest",
                instrumentType != null ? instrumentType : "both",
                interval != null ? interval : "day",
                maxDays,
                showWeekly,
                cacheExpiries,
                cacheContracts);
    }

    private List<String> fetchOptionKeys(String underlyingKey, LocalDate expiry) {
        return expiredService.fetchExpiredOptions(underlyingKey, expiry).stream()
                .map(ExpiredOptionContract::toExpiredInstrumentKey)
                .collect(Collectors.toList());
    }

    private List<String> fetchFutureKeys(String underlyingKey, LocalDate expiry) {
        return expiredService.fetchExpiredFutures(underlyingKey, expiry).stream()
                .map(ExpiredFutureContract::toExpiredInstrumentKey)
                .collect(Collectors.toList());
    }

    private boolean isMonthlyExpiry(LocalDate expiry) {
        // Monthly expiries are typically the last Thursday of the month
        // Simplified: check if it's in the last week of the month
        return expiry.getDayOfMonth() >= 23;
    }

    /**
     * User settings record for expired instruments.
     */
    public record ExpiredFetchSettings(
            String expiryFetch, // "latest" or "all"
            String instrumentType, // "options", "futures", or "both"
            String interval, // candle interval
            int maxHistoricalDays,
            boolean showWeeklyOptions,
            boolean cacheExpiries,
            boolean cacheContracts) {
    }
}
