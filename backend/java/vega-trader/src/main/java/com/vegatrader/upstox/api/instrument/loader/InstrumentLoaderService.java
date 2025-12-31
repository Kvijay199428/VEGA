package com.vegatrader.upstox.api.instrument.loader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vegatrader.upstox.api.instrument.entity.*;
import com.vegatrader.upstox.api.instrument.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Service for loading instrument data from Upstox GZIP JSON files.
 * 
 * <p>
 * Features:
 * <ul>
 * <li>Streaming JSON parsing (O(1) memory)</li>
 * <li>Batch inserts for performance</li>
 * <li>Idempotent per trading day</li>
 * <li>Crash-safe with transaction rollback</li>
 * </ul>
 * 
 * @since 4.0.0
 */
@Service
public class InstrumentLoaderService {

    private static final Logger logger = LoggerFactory.getLogger(InstrumentLoaderService.class);
    private static final int BATCH_SIZE = 500;
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final InstrumentMasterRepository masterRepository;
    private final InstrumentMisRepository misRepository;
    private final InstrumentMtfRepository mtfRepository;
    private final InstrumentSuspensionRepository suspensionRepository;
    private final ObjectMapper objectMapper;

    public InstrumentLoaderService(
            InstrumentMasterRepository masterRepository,
            InstrumentMisRepository misRepository,
            InstrumentMtfRepository mtfRepository,
            InstrumentSuspensionRepository suspensionRepository) {
        this.masterRepository = masterRepository;
        this.misRepository = misRepository;
        this.mtfRepository = mtfRepository;
        this.suspensionRepository = suspensionRepository;

        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Loads BOD instruments from the specified source.
     * 
     * @param source the instrument file source
     * @return number of instruments loaded
     */
    @Transactional
    public int loadBodInstruments(InstrumentFileSource source) {
        if (!source.isBod()) {
            throw new IllegalArgumentException("Source is not a BOD file: " + source);
        }

        logger.info("Loading BOD instruments from: {}", source.getKey());
        LocalDate tradingDate = LocalDate.now(IST);
        int count = 0;

        try (InputStream inputStream = URI.create(source.getUrl()).toURL().openStream();
                GZIPInputStream gzipStream = new GZIPInputStream(inputStream)) {

            JsonFactory factory = new JsonFactory();
            JsonParser parser = factory.createParser(gzipStream);

            List<InstrumentMasterEntity> batch = new ArrayList<>(BATCH_SIZE);

            // Expect array start
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Expected array start in JSON");
            }

            while (parser.nextToken() == JsonToken.START_OBJECT) {
                @SuppressWarnings("unchecked")
                Map<String, Object> record = objectMapper.readValue(parser, Map.class);

                InstrumentMasterEntity entity = mapToEntity(record, tradingDate);
                if (entity != null) {
                    batch.add(entity);
                }

                if (batch.size() >= BATCH_SIZE) {
                    masterRepository.saveAll(batch);
                    count += batch.size();
                    batch.clear();
                    logger.debug("Saved {} instruments so far", count);
                }
            }

            // Save remaining batch
            if (!batch.isEmpty()) {
                masterRepository.saveAll(batch);
                count += batch.size();
            }

            logger.info("✓ Loaded {} BOD instruments from {}", count, source.getKey());
            return count;

        } catch (Exception e) {
            logger.error("✗ Failed to load instruments from {}: {}", source.getKey(), e.getMessage());
            throw new InstrumentLoadException("Failed to load instruments", e);
        }
    }

    /**
     * Loads MIS overlay data.
     */
    @Transactional
    public int loadMisOverlay(InstrumentFileSource source) {
        if (!source.isMis()) {
            throw new IllegalArgumentException("Source is not an MIS file: " + source);
        }

        logger.info("Loading MIS overlay from: {}", source.getKey());
        LocalDate tradingDate = LocalDate.now(IST);
        int count = 0;

        try (InputStream inputStream = URI.create(source.getUrl()).toURL().openStream();
                GZIPInputStream gzipStream = new GZIPInputStream(inputStream)) {

            JsonFactory factory = new JsonFactory();
            JsonParser parser = factory.createParser(gzipStream);

            List<InstrumentMisEntity> batch = new ArrayList<>(BATCH_SIZE);

            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Expected array start in JSON");
            }

            while (parser.nextToken() == JsonToken.START_OBJECT) {
                @SuppressWarnings("unchecked")
                Map<String, Object> record = objectMapper.readValue(parser, Map.class);

                InstrumentMisEntity entity = mapToMisEntity(record, tradingDate);
                if (entity != null) {
                    batch.add(entity);
                }

                if (batch.size() >= BATCH_SIZE) {
                    misRepository.saveAll(batch);
                    count += batch.size();
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                misRepository.saveAll(batch);
                count += batch.size();
            }

            logger.info("✓ Loaded {} MIS overlays from {}", count, source.getKey());
            return count;

        } catch (Exception e) {
            logger.error("✗ Failed to load MIS overlay: {}", e.getMessage());
            throw new InstrumentLoadException("Failed to load MIS overlay", e);
        }
    }

    /**
     * Loads MTF overlay data.
     */
    @Transactional
    public int loadMtfOverlay(InstrumentFileSource source) {
        if (!source.isMtf()) {
            throw new IllegalArgumentException("Source is not an MTF file: " + source);
        }

        logger.info("Loading MTF overlay from: {}", source.getKey());
        LocalDate tradingDate = LocalDate.now(IST);
        int count = 0;

        try (InputStream inputStream = URI.create(source.getUrl()).toURL().openStream();
                GZIPInputStream gzipStream = new GZIPInputStream(inputStream)) {

            JsonFactory factory = new JsonFactory();
            JsonParser parser = factory.createParser(gzipStream);

            List<InstrumentMtfEntity> batch = new ArrayList<>(BATCH_SIZE);

            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Expected array start in JSON");
            }

            while (parser.nextToken() == JsonToken.START_OBJECT) {
                @SuppressWarnings("unchecked")
                Map<String, Object> record = objectMapper.readValue(parser, Map.class);

                InstrumentMtfEntity entity = mapToMtfEntity(record, tradingDate);
                if (entity != null) {
                    batch.add(entity);
                }

                if (batch.size() >= BATCH_SIZE) {
                    mtfRepository.saveAll(batch);
                    count += batch.size();
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                mtfRepository.saveAll(batch);
                count += batch.size();
            }

            logger.info("✓ Loaded {} MTF overlays from {}", count, source.getKey());
            return count;

        } catch (Exception e) {
            logger.error("✗ Failed to load MTF overlay: {}", e.getMessage());
            throw new InstrumentLoadException("Failed to load MTF overlay", e);
        }
    }

    /**
     * Loads suspended instruments overlay.
     */
    @Transactional
    public int loadSuspensionOverlay(InstrumentFileSource source) {
        if (!source.isSuspension()) {
            throw new IllegalArgumentException("Source is not a suspension file: " + source);
        }

        logger.info("Loading suspension overlay from: {}", source.getKey());
        LocalDate tradingDate = LocalDate.now(IST);
        int count = 0;

        try (InputStream inputStream = URI.create(source.getUrl()).toURL().openStream();
                GZIPInputStream gzipStream = new GZIPInputStream(inputStream)) {

            JsonFactory factory = new JsonFactory();
            JsonParser parser = factory.createParser(gzipStream);

            List<InstrumentSuspensionEntity> batch = new ArrayList<>(BATCH_SIZE);

            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Expected array start in JSON");
            }

            while (parser.nextToken() == JsonToken.START_OBJECT) {
                @SuppressWarnings("unchecked")
                Map<String, Object> record = objectMapper.readValue(parser, Map.class);

                InstrumentSuspensionEntity entity = mapToSuspensionEntity(record, tradingDate);
                if (entity != null) {
                    batch.add(entity);
                }

                if (batch.size() >= BATCH_SIZE) {
                    suspensionRepository.saveAll(batch);
                    count += batch.size();
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                suspensionRepository.saveAll(batch);
                count += batch.size();
            }

            logger.info("✓ Loaded {} suspension overlays from {}", count, source.getKey());
            return count;

        } catch (Exception e) {
            logger.error("✗ Failed to load suspension overlay: {}", e.getMessage());
            throw new InstrumentLoadException("Failed to load suspension overlay", e);
        }
    }

    /**
     * Performs full daily refresh sequence.
     */
    @Transactional
    public void performDailyRefresh() {
        logger.info("=== Starting Daily Instrument Refresh ===");
        long start = System.currentTimeMillis();

        try {
            // 1. Load BOD instruments (NSE as primary)
            loadBodInstruments(InstrumentFileSource.NSE);

            // 2. Load overlays
            loadSuspensionOverlay(InstrumentFileSource.SUSPENDED);
            loadMisOverlay(InstrumentFileSource.NSE_MIS);
            loadMtfOverlay(InstrumentFileSource.MTF);

            long elapsed = System.currentTimeMillis() - start;
            logger.info("=== Daily Refresh Complete in {}ms ===", elapsed);

        } catch (Exception e) {
            logger.error("Daily refresh failed: {}", e.getMessage());
            throw e;
        }
    }

    // --- Mapping Methods ---

    private InstrumentMasterEntity mapToEntity(Map<String, Object> record, LocalDate tradingDate) {
        String instrumentKey = getString(record, "instrument_key");
        if (instrumentKey == null || instrumentKey.isEmpty()) {
            return null;
        }

        InstrumentMasterEntity entity = new InstrumentMasterEntity();
        entity.setInstrumentKey(instrumentKey);
        entity.setSegment(getString(record, "segment"));
        entity.setExchange(getString(record, "exchange"));
        entity.setInstrumentType(getString(record, "instrument_type"));
        entity.setTradingSymbol(getString(record, "trading_symbol"));
        entity.setName(getString(record, "name"));
        entity.setShortName(getString(record, "short_name"));
        entity.setIsin(getString(record, "isin"));
        entity.setUnderlyingKey(getString(record, "underlying_key"));
        entity.setUnderlyingSymbol(getString(record, "underlying_symbol"));
        entity.setUnderlyingType(getString(record, "underlying_type"));
        entity.setExpiry(parseExpiry(record.get("expiry")));
        entity.setStrikePrice(getDouble(record, "strike_price"));
        entity.setLotSize(getInteger(record, "lot_size", 1));
        entity.setMinimumLot(getInteger(record, "minimum_lot", null));
        entity.setFreezeQuantity(getInteger(record, "freeze_quantity", null));
        entity.setTickSize(getDouble(record, "tick_size"));
        entity.setExchangeToken(getString(record, "exchange_token"));
        entity.setWeekly(getBoolean(record, "weekly"));
        entity.setSecurityType(getString(record, "security_type"));
        entity.setTradingDate(tradingDate);
        entity.setIsActive(true);

        return entity;
    }

    private InstrumentMisEntity mapToMisEntity(Map<String, Object> record, LocalDate tradingDate) {
        String instrumentKey = getString(record, "instrument_key");
        if (instrumentKey == null)
            return null;

        InstrumentMisEntity entity = new InstrumentMisEntity();
        entity.setInstrumentKey(instrumentKey);
        entity.setIntradayMargin(getDouble(record, "intraday_margin"));
        entity.setIntradayLeverage(getDouble(record, "intraday_leverage"));
        entity.setQtyMultiplier(getDouble(record, "qty_multiplier"));
        entity.setTradingDate(tradingDate);
        return entity;
    }

    private InstrumentMtfEntity mapToMtfEntity(Map<String, Object> record, LocalDate tradingDate) {
        String instrumentKey = getString(record, "instrument_key");
        if (instrumentKey == null)
            return null;

        InstrumentMtfEntity entity = new InstrumentMtfEntity();
        entity.setInstrumentKey(instrumentKey);
        entity.setMtfEnabled(getBoolean(record, "mtf_enabled"));
        entity.setMtfBracket(getDouble(record, "mtf_bracket"));
        entity.setTradingDate(tradingDate);
        return entity;
    }

    private InstrumentSuspensionEntity mapToSuspensionEntity(Map<String, Object> record, LocalDate tradingDate) {
        String instrumentKey = getString(record, "instrument_key");
        if (instrumentKey == null)
            return null;

        InstrumentSuspensionEntity entity = new InstrumentSuspensionEntity();
        entity.setInstrumentKey(instrumentKey);
        entity.setTradingDate(tradingDate);
        return entity;
    }

    // --- Utility Methods ---

    private String getString(Map<String, Object> record, String key) {
        Object value = record.get(key);
        return value != null ? value.toString() : null;
    }

    private Double getDouble(Map<String, Object> record, String key) {
        Object value = record.get(key);
        if (value == null)
            return null;
        if (value instanceof Number)
            return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer getInteger(Map<String, Object> record, String key, Integer defaultValue) {
        Object value = record.get(key);
        if (value == null)
            return defaultValue;
        if (value instanceof Number)
            return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Boolean getBoolean(Map<String, Object> record, String key) {
        Object value = record.get(key);
        if (value == null)
            return false;
        if (value instanceof Boolean)
            return (Boolean) value;
        return "true".equalsIgnoreCase(value.toString());
    }

    private LocalDate parseExpiry(Object value) {
        if (value == null)
            return null;
        if (value instanceof Number) {
            long timestamp = ((Number) value).longValue();
            return Instant.ofEpochMilli(timestamp).atZone(IST).toLocalDate();
        }
        return null;
    }

    /**
     * Exception for instrument loading failures.
     */
    public static class InstrumentLoadException extends RuntimeException {
        public InstrumentLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
