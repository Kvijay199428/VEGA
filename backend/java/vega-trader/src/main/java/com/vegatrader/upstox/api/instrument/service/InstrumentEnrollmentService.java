package com.vegatrader.upstox.api.instrument.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vegatrader.upstox.api.instrument.filter.InstrumentFilterCriteria;
import com.vegatrader.upstox.api.instrument.filter.InstrumentFilterService;
import com.vegatrader.upstox.api.response.instrument.InstrumentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.GZIPInputStream;

/**
 * Service for loading and managing instrument master data.
 * 
 * <p>
 * Loads instrument data from Upstox JSON files and provides
 * filtering and enrollment capabilities for MarketDataStreamerV3.
 * 
 * @since 3.0.0
 */
@Service
public class InstrumentEnrollmentService {

    private static final Logger logger = LoggerFactory.getLogger(InstrumentEnrollmentService.class);

    /**
     * Default instrument JSON URL for NSE.
     */
    public static final String DEFAULT_NSE_URL = "https://assets.upstox.com/market-quote/instruments/exchange/NSE.json.gz";

    /**
     * Default instrument JSON URL for BSE.
     */
    public static final String DEFAULT_BSE_URL = "https://assets.upstox.com/market-quote/instruments/exchange/BSE.json.gz";

    /**
     * Cache TTL values.
     */
    private static final long TTL_STABLE_MS = 24 * 60 * 60 * 1000; // 24 hours (Equity/Index)
    private static final long TTL_VOLATILE_MS = 2 * 60 * 60 * 1000; // 2 hours (F&O)

    private final InstrumentFilterService filterService;
    private final Gson gson;
    private final Map<String, CacheEntry> cache;
    private final ScheduledExecutorService refreshScheduler;
    private final com.vegatrader.upstox.api.instrument.enrollment.SubscriptionEligibilityValidator validator;

    public InstrumentEnrollmentService(InstrumentFilterService filterService) {
        this.filterService = filterService;
        this.gson = new Gson();
        this.cache = new ConcurrentHashMap<>();
        this.refreshScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("Instrument-Refresher");
            t.setDaemon(true);
            return t;
        });
        this.validator = new com.vegatrader.upstox.api.instrument.enrollment.SubscriptionEligibilityValidator();
    }

    @PostConstruct
    public void initialize() {
        logger.info("Instrument Enrollment Service initialized");
    }

    /**
     * Loads instruments from default NSE source.
     * 
     * @return list of instruments
     */
    public List<InstrumentResponse> loadNSEInstruments() {
        return loadInstruments(DEFAULT_NSE_URL, "NSE");
    }

    /**
     * Loads instruments from default BSE source.
     * 
     * @return list of instruments
     */
    public List<InstrumentResponse> loadBSEInstruments() {
        return loadInstruments(DEFAULT_BSE_URL, "BSE");
    }

    /**
     * Loads instruments from URL with TTL logic and background refresh.
     * 
     * @param url      the URL to load from
     * @param exchange exchange name for caching
     * @return list of instruments
     */
    public List<InstrumentResponse> loadInstruments(String url, String exchange) {
        CacheEntry entry = cache.get(exchange);

        if (entry != null) {
            if (!entry.isExpired()) {
                logger.debug("✓ Using valid cache for {}", exchange);
                return entry.getInstruments();
            } else {
                logger.warn("⚠ Cache expired for {}, triggering background refresh", exchange);
                triggerBackgroundRefresh(url, exchange);
                return entry.getInstruments(); // Serve stale data
            }
        }

        // Cold start - must block
        logger.info("❄ Cold start for {}, loading from: {}", exchange, url);
        try {
            List<InstrumentResponse> instruments = downloadAndParseInstruments(url);
            long ttl = getTTLForExchange(exchange);
            cache.put(exchange, new CacheEntry(instruments, ttl));
            logger.info("✓ Loaded {} instruments for {}", instruments.size(), exchange);
            return instruments;
        } catch (Exception e) {
            logger.error("✗ Failed to load instruments for {}: {}", exchange, e.getMessage());
            return List.of();
        }
    }

    private void triggerBackgroundRefresh(String url, String exchange) {
        refreshScheduler.submit(() -> {
            try {
                logger.info("↺ Refreshing instruments for {} in background...", exchange);
                List<InstrumentResponse> instruments = downloadAndParseInstruments(url);
                long ttl = getTTLForExchange(exchange);
                cache.put(exchange, new CacheEntry(instruments, ttl));
                logger.info("✓ Background refresh complete for {}", exchange);
            } catch (Exception e) {
                logger.error("✗ Background refresh failed for {}: {}", exchange, e.getMessage());
            }
        });
    }

    private long getTTLForExchange(String exchange) {
        // Multi-tier TTL
        if (exchange.contains("FO") || exchange.contains("VOLATILE")) {
            return TTL_VOLATILE_MS;
        }
        return TTL_STABLE_MS;
    }

    private List<InstrumentResponse> downloadAndParseInstruments(String urlString) throws IOException {
        URL url = java.net.URI.create(urlString).toURL();
        try (InputStream inputStream = url.openStream();
                GZIPInputStream gzipStream = new GZIPInputStream(inputStream);
                InputStreamReader reader = new InputStreamReader(gzipStream)) {

            java.lang.reflect.Type listType = new TypeToken<List<InstrumentResponse>>() {
            }.getType();
            return gson.fromJson(reader, listType);
        }
    }

    /**
     * Enrolls instruments based on filter criteria.
     * 
     * @param criteria filter criteria
     * @return list of enrolled instrument keys
     */
    public List<String> enrollInstruments(InstrumentFilterCriteria criteria) {
        List<InstrumentResponse> merged = new ArrayList<>();

        // Multi-exchange enrollment (NSE + BSE)
        merged.addAll(loadNSEInstruments());
        merged.addAll(loadBSEInstruments());

        return filterService.extractInstrumentKeys(merged, criteria);
    }

    /**
     * Enrolls instruments for a specific segment and type.
     * 
     * @param segment        segment (e.g., "NSE_EQ", "NSE_FO")
     * @param instrumentType instrument type (e.g., "EQ", "OPTION")
     * @return list of instrument keys
     */
    public List<String> enrollBySegmentAndType(String segment, String instrumentType) {
        InstrumentFilterCriteria criteria = InstrumentFilterCriteria.builder()
                .segment(segment)
                .instrumentType(instrumentType)
                .build();

        return enrollInstruments(criteria);
    }

    /**
     * Enrolls instruments by trading symbol pattern.
     * 
     * @param segment        segment
     * @param instrumentType instrument type
     * @param symbolPattern  symbol pattern
     * @param limit          max number of instruments
     * @return list of instrument keys
     */
    public List<String> enrollByPattern(String segment, String instrumentType,
            String symbolPattern, int limit) {
        InstrumentFilterCriteria criteria = InstrumentFilterCriteria.builder()
                .segment(segment)
                .instrumentType(instrumentType)
                .tradingSymbolPattern(symbolPattern)
                .limit(limit)
                .build();

        return enrollInstruments(criteria);
    }

    /**
     * Enrolls Reliance Equity (example).
     * 
     * @return Reliance equity instrument keys
     */
    public List<String> enrollRelianceEquity() {
        return enrollByPattern("NSE_EQ", "EQ", "RELIANCE", 1);
    }

    /**
     * Enrolls Nifty 50 index.
     * 
     * @return Nifty 50 instrument key
     */
    public List<String> enrollNifty50() {
        return enrollByPattern("NSE_INDEX", "INDEX", "NIFTY 50", 1);
    }

    /**
     * Enrolls Bank Nifty index.
     * 
     * @return Bank Nifty instrument key
     */
    public List<String> enrollBankNifty() {
        return enrollByPattern("NSE_INDEX", "INDEX", "NIFTY BANK", 1);
    }

    /**
     * Gets instrument details.
     * 
     * @param instrumentKey the instrument key
     * @return instrument details or null
     */
    public InstrumentResponse getInstrumentDetails(String instrumentKey) {
        List<InstrumentResponse> all = new ArrayList<>();
        all.addAll(loadNSEInstruments());
        all.addAll(loadBSEInstruments());

        return all.stream()
                .filter(i -> i.getInstrumentKey().equals(instrumentKey))
                .findFirst()
                .orElse(null);
    }

    /**
     * Enrolls configured instruments for default subscription.
     * 
     * <p>
     * This provides a default set of instruments suitable for
     * general market monitoring:
     * <ul>
     * <li>Nifty 50 Index</li>
     * <li>Bank Nifty Index</li>
     * <li>Reliance Equity</li>
     * </ul>
     * 
     * @return set of instrument keys ready for subscription
     * @since 3.1.0
     */
    public Set<String> enrollConfiguredInstruments() {
        Set<String> keys = new java.util.LinkedHashSet<>();

        // Enroll major indices
        keys.addAll(enrollNifty50());
        keys.addAll(enrollBankNifty());

        // Enroll sample equity
        keys.addAll(enrollRelianceEquity());

        logger.info("Enrolled {} configured instruments", keys.size());
        return keys;
    }

    /**
     * Enrolls requested instruments with mode validation.
     * 
     * <p>
     * Validates that the requested subscription count does not
     * exceed the limit for the specified mode.
     * 
     * @param requestedKeys instrument keys to enroll
     * @param mode          subscription mode with limit
     * @return validated set of instrument keys
     * @throws IllegalStateException if count exceeds mode limit
     * @since 3.1.0
     */
    public Set<String> enroll(Set<String> requestedKeys, com.vegatrader.upstox.api.websocket.Mode mode) {
        validator.validate(requestedKeys.size(), mode);
        logger.info("Enrolled {} instruments for mode {}", requestedKeys.size(), mode);
        return requestedKeys;
    }

    public void clearCache() {
        cache.clear();
        logger.info("Instrument cache cleared");
    }

    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cached_exchanges", cache.keySet());
        stats.put("total_cached_instruments",
                cache.values().stream().mapToInt(e -> e.getInstruments().size()).sum());
        stats.put("expiry_status", getExpiryStatus());
        return stats;
    }

    private Map<String, Boolean> getExpiryStatus() {
        Map<String, Boolean> status = new HashMap<>();
        cache.forEach((k, v) -> status.put(k, v.isExpired()));
        return status;
    }

    /**
     * Internal cache entry with TTL.
     */
    private static class CacheEntry {
        private final List<InstrumentResponse> instruments;
        private final Instant expiresAt;

        public CacheEntry(List<InstrumentResponse> instruments, long ttlMs) {
            this.instruments = Collections.unmodifiableList(instruments);
            this.expiresAt = Instant.now().plusMillis(ttlMs);
        }

        public List<InstrumentResponse> getInstruments() {
            return instruments;
        }

        public boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
