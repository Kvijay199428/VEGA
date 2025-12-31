package com.vegatrader.upstox.api.instrument.search;

import com.vegatrader.upstox.api.instrument.entity.InstrumentMasterEntity;
import com.vegatrader.upstox.api.instrument.repository.InstrumentMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for fast autocomplete with caching.
 * 
 * <p>
 * Uses Caffeine cache (or Redis if configured) with 10-minute TTL.
 * 
 * @since 4.0.0
 */
@Service
public class InstrumentAutocompleteService {

    private static final Logger logger = LoggerFactory.getLogger(InstrumentAutocompleteService.class);
    private static final int MAX_RESULTS = 15;
    private static final int MIN_QUERY_LENGTH = 2;

    private final InstrumentMasterRepository masterRepository;

    public InstrumentAutocompleteService(InstrumentMasterRepository masterRepository) {
        this.masterRepository = masterRepository;
    }

    /**
     * Performs autocomplete search with caching.
     * 
     * <p>
     * Cache key format: autocomplete::<query>
     * 
     * @param query search query (min 2 characters)
     * @return list of matching results
     */
    @Cacheable(value = "autocomplete", key = "#query.toLowerCase()", unless = "#result.isEmpty()")
    public List<AutocompleteResult> autocomplete(String query) {
        if (query == null || query.length() < MIN_QUERY_LENGTH) {
            return Collections.emptyList();
        }

        logger.debug("Autocomplete search (cache miss): {}", query);

        List<InstrumentMasterEntity> results = masterRepository.searchBySymbolPrefix(query);

        return results.stream()
                .limit(MAX_RESULTS)
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    /**
     * Autocomplete within a specific segment.
     * 
     * @param query   search query
     * @param segment market segment filter
     * @return filtered results
     */
    @Cacheable(value = "autocomplete_segment", key = "#segment + ':' + #query.toLowerCase()", unless = "#result.isEmpty()")
    public List<AutocompleteResult> autocompleteInSegment(String query, String segment) {
        if (query == null || query.length() < MIN_QUERY_LENGTH) {
            return Collections.emptyList();
        }

        logger.debug("Autocomplete search in segment {} (cache miss): {}", segment, query);

        List<InstrumentMasterEntity> results = masterRepository.searchBySymbolPrefix(query);

        return results.stream()
                .filter(i -> segment.equals(i.getSegment()))
                .limit(MAX_RESULTS)
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    private AutocompleteResult toResult(InstrumentMasterEntity entity) {
        AutocompleteResult result = new AutocompleteResult();
        result.setInstrumentKey(entity.getInstrumentKey());
        result.setSymbol(entity.getTradingSymbol());
        result.setName(entity.getName() != null ? entity.getName() : entity.getShortName());
        result.setSegment(entity.getSegment());
        result.setInstrumentType(entity.getInstrumentType());
        return result;
    }

    /**
     * Lightweight autocomplete result DTO.
     */
    public static class AutocompleteResult {
        private String instrumentKey;
        private String symbol;
        private String name;
        private String segment;
        private String instrumentType;

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

        public String getInstrumentType() {
            return instrumentType;
        }

        public void setInstrumentType(String instrumentType) {
            this.instrumentType = instrumentType;
        }
    }
}
