package com.vegatrader.upstox.api.instrument.search;

import com.vegatrader.upstox.api.instrument.entity.InstrumentMasterEntity;
import com.vegatrader.upstox.api.instrument.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for searching and resolving instruments.
 * 
 * @since 4.0.0
 */
@Service
public class InstrumentSearchService {

    private static final Logger logger = LoggerFactory.getLogger(InstrumentSearchService.class);
    private static final int MAX_AUTOCOMPLETE_RESULTS = 15;

    private final InstrumentMasterRepository masterRepository;
    private final InstrumentMisRepository misRepository;
    private final InstrumentMtfRepository mtfRepository;
    private final InstrumentSuspensionRepository suspensionRepository;

    public InstrumentSearchService(
            InstrumentMasterRepository masterRepository,
            InstrumentMisRepository misRepository,
            InstrumentMtfRepository mtfRepository,
            InstrumentSuspensionRepository suspensionRepository) {
        this.masterRepository = masterRepository;
        this.misRepository = misRepository;
        this.mtfRepository = mtfRepository;
        this.suspensionRepository = suspensionRepository;
    }

    /**
     * Resolves symbol to instrument key.
     * 
     * @param symbol         trading symbol
     * @param segment        market segment
     * @param instrumentType instrument type (EQ, FUT, CE, PE, INDEX)
     * @return instrument key or empty if not found
     */
    public Optional<String> resolveInstrumentKey(String symbol, String segment, String instrumentType) {
        List<InstrumentMasterEntity> results = masterRepository
                .findByTradingSymbolIgnoreCaseAndSegmentAndInstrumentType(symbol, segment, instrumentType);

        if (results.isEmpty()) {
            logger.debug("No instrument found for {}/{}/{}", symbol, segment, instrumentType);
            return Optional.empty();
        }

        return Optional.of(results.get(0).getInstrumentKey());
    }

    /**
     * Autocomplete search by symbol prefix.
     * 
     * @param query search query
     * @return list of matching instruments
     */
    public List<InstrumentSearchResult> autocomplete(String query) {
        if (query == null || query.length() < 2) {
            return Collections.emptyList();
        }

        List<InstrumentMasterEntity> results = masterRepository.searchBySymbolPrefix(query);

        return results.stream()
                .limit(MAX_AUTOCOMPLETE_RESULTS)
                .map(this::toSearchResult)
                .collect(Collectors.toList());
    }

    /**
     * Full search with segment and type filters.
     */
    public List<InstrumentSearchResult> search(String symbol, String segment, String instrumentType) {
        List<InstrumentMasterEntity> results = masterRepository
                .searchBySymbolSegmentType(symbol, segment, instrumentType);

        return results.stream()
                .map(this::toSearchResult)
                .collect(Collectors.toList());
    }

    /**
     * Finds instrument by key with overlay data.
     */
    public Optional<InstrumentSearchResult> findByKey(String instrumentKey) {
        return masterRepository.findById(instrumentKey)
                .map(this::toSearchResultWithOverlays);
    }

    /**
     * Gets options chain for underlying.
     */
    public List<InstrumentMasterEntity> getOptionsChain(String underlyingKey, LocalDate expiry) {
        return masterRepository.findOptionsByUnderlyingAndExpiry(underlyingKey, expiry);
    }

    /**
     * Gets available expiry dates for underlying.
     */
    public List<LocalDate> getExpiryDates(String underlyingKey) {
        return masterRepository.findExpiryDatesByUnderlying(underlyingKey);
    }

    // --- Mapping Methods ---

    private InstrumentSearchResult toSearchResult(InstrumentMasterEntity entity) {
        InstrumentSearchResult result = new InstrumentSearchResult();
        result.setInstrumentKey(entity.getInstrumentKey());
        result.setSymbol(entity.getTradingSymbol());
        result.setName(entity.getName());
        result.setSegment(entity.getSegment());
        result.setExchange(entity.getExchange());
        result.setInstrumentType(entity.getInstrumentType());
        result.setExpiry(entity.getExpiry());
        result.setStrikePrice(entity.getStrikePrice());
        result.setLotSize(entity.getLotSize());
        return result;
    }

    private InstrumentSearchResult toSearchResultWithOverlays(InstrumentMasterEntity entity) {
        InstrumentSearchResult result = toSearchResult(entity);

        // Add overlay data
        result.setMisAllowed(misRepository.existsByInstrumentKey(entity.getInstrumentKey()));
        result.setMtfEnabled(mtfRepository.isMtfEnabled(entity.getInstrumentKey()));
        result.setSuspended(suspensionRepository.existsByInstrumentKey(entity.getInstrumentKey()));

        return result;
    }

    /**
     * Search result DTO with overlay data.
     */
    public static class InstrumentSearchResult {
        private String instrumentKey;
        private String symbol;
        private String name;
        private String segment;
        private String exchange;
        private String instrumentType;
        private LocalDate expiry;
        private Double strikePrice;
        private Integer lotSize;
        private Boolean misAllowed;
        private Boolean mtfEnabled;
        private Boolean suspended;

        // --- Getters and Setters ---

        public String getInstrumentKey() {
            return instrumentKey;
        }

        public void setInstrumentKey(String instrumentKey) {
            this.instrumentKey = instrumentKey;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSegment() {
            return segment;
        }

        public void setSegment(String segment) {
            this.segment = segment;
        }

        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public String getInstrumentType() {
            return instrumentType;
        }

        public void setInstrumentType(String instrumentType) {
            this.instrumentType = instrumentType;
        }

        public LocalDate getExpiry() {
            return expiry;
        }

        public void setExpiry(LocalDate expiry) {
            this.expiry = expiry;
        }

        public Double getStrikePrice() {
            return strikePrice;
        }

        public void setStrikePrice(Double strikePrice) {
            this.strikePrice = strikePrice;
        }

        public Integer getLotSize() {
            return lotSize;
        }

        public void setLotSize(Integer lotSize) {
            this.lotSize = lotSize;
        }

        public Boolean getMisAllowed() {
            return misAllowed;
        }

        public void setMisAllowed(Boolean misAllowed) {
            this.misAllowed = misAllowed;
        }

        public Boolean getMtfEnabled() {
            return mtfEnabled;
        }

        public void setMtfEnabled(Boolean mtfEnabled) {
            this.mtfEnabled = mtfEnabled;
        }

        public Boolean getSuspended() {
            return suspended;
        }

        public void setSuspended(Boolean suspended) {
            this.suspended = suspended;
        }
    }
}
