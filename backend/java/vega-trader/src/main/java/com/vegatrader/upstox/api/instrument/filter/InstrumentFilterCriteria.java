package com.vegatrader.upstox.api.instrument.filter;

import com.vegatrader.upstox.api.response.instrument.InstrumentResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Criteria for filtering instruments.
 * 
 * <p>
 * Supports filtering by:
 * <ul>
 * <li>Segment (NSE_EQ, NSE_FO, BSE_EQ, etc.)</li>
 * <li>Instrument Type (EQ, OPTION, FUTURE, etc.)</li>
 * <li>Exchange (NSE, BSE, etc.)</li>
 * <li>Trading Symbol (partial match)</li>
 * <li>Name (partial match)</li>
 * <li>Option Type (CE, PE)</li>
 * <li>Expiry Date</li>
 * </ul>
 * 
 * @since 3.0.0
 */
public class InstrumentFilterCriteria {

    private List<String> segments;
    private List<String> instrumentTypes;
    private List<String> exchanges;
    private String tradingSymbolPattern;
    private String namePattern;
    private List<String> optionTypes;
    private String expiryDate;
    private boolean excludeExpired;
    private Integer limit;

    public InstrumentFilterCriteria() {
        this.segments = new ArrayList<>();
        this.instrumentTypes = new ArrayList<>();
        this.exchanges = new ArrayList<>();
        this.optionTypes = new ArrayList<>();
        this.excludeExpired = true;
    }

    /**
     * Builder for InstrumentFilterCriteria.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Checks if an instrument matches all criteria.
     * 
     * @param instrument the instrument to check
     * @return true if matches all criteria
     */
    public boolean matches(InstrumentResponse instrument) {
        // Segment filter
        if (!segments.isEmpty() && !matchesSegment(instrument.getSegment())) {
            return false;
        }

        // Instrument type filter
        if (!instrumentTypes.isEmpty() && !matchesInstrumentType(instrument.getInstrumentType())) {
            return false;
        }

        // Exchange filter
        if (!exchanges.isEmpty() && !matchesExchange(instrument.getExchange())) {
            return false;
        }

        // Trading symbol pattern
        if (tradingSymbolPattern != null && !matchesTradingSymbol(instrument.getTradingSymbol())) {
            return false;
        }

        // Name pattern
        if (namePattern != null && !matchesName(instrument.getName())) {
            return false;
        }

        // Option type filter
        if (!optionTypes.isEmpty() && !matchesOptionType(instrument.getOptionType())) {
            return false;
        }

        // Expiry date filter
        if (expiryDate != null && !matchesExpiry(instrument.getExpiry())) {
            return false;
        }

        // Exclude expired filter
        if (excludeExpired && instrument.isExpired()) {
            return false;
        }

        return true;
    }

    private boolean matchesSegment(String segment) {
        if (segment == null)
            return false;
        return segments.stream().anyMatch(s -> s.equalsIgnoreCase(segment));
    }

    private boolean matchesInstrumentType(String type) {
        if (type == null)
            return false;
        return instrumentTypes.stream().anyMatch(t -> t.equalsIgnoreCase(type));
    }

    private boolean matchesExchange(String exchange) {
        if (exchange == null)
            return false;
        return exchanges.stream().anyMatch(e -> e.equalsIgnoreCase(exchange));
    }

    private boolean matchesTradingSymbol(String symbol) {
        if (symbol == null)
            return false;
        return symbol.toUpperCase().contains(tradingSymbolPattern.toUpperCase());
    }

    private boolean matchesName(String name) {
        if (name == null)
            return false;
        return name.toUpperCase().contains(namePattern.toUpperCase());
    }

    private boolean matchesOptionType(String optionType) {
        if (optionType == null)
            return false;
        return optionTypes.stream().anyMatch(ot -> ot.equalsIgnoreCase(optionType));
    }

    private boolean matchesExpiry(String expiry) {
        if (expiry == null)
            return false;
        return expiry.equals(expiryDate);
    }

    // Getters
    public List<String> getSegments() {
        return segments;
    }

    public List<String> getInstrumentTypes() {
        return instrumentTypes;
    }

    public List<String> getExchanges() {
        return exchanges;
    }

    public String getTradingSymbolPattern() {
        return tradingSymbolPattern;
    }

    public String getNamePattern() {
        return namePattern;
    }

    public List<String> getOptionTypes() {
        return optionTypes;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public boolean isExcludeExpired() {
        return excludeExpired;
    }

    public Integer getLimit() {
        return limit;
    }

    // Builder class
    public static class Builder {
        private final InstrumentFilterCriteria criteria;

        public Builder() {
            this.criteria = new InstrumentFilterCriteria();
        }

        public Builder segment(String... segments) {
            criteria.segments = List.of(segments);
            return this;
        }

        public Builder instrumentType(String... types) {
            criteria.instrumentTypes = List.of(types);
            return this;
        }

        public Builder exchange(String... exchanges) {
            criteria.exchanges = List.of(exchanges);
            return this;
        }

        public Builder tradingSymbolPattern(String pattern) {
            criteria.tradingSymbolPattern = pattern;
            return this;
        }

        public Builder namePattern(String pattern) {
            criteria.namePattern = pattern;
            return this;
        }

        public Builder optionType(String... types) {
            criteria.optionTypes = List.of(types);
            return this;
        }

        public Builder expiryDate(String date) {
            criteria.expiryDate = date;
            return this;
        }

        public Builder excludeExpired(boolean exclude) {
            criteria.excludeExpired = exclude;
            return this;
        }

        public Builder limit(int limit) {
            criteria.limit = limit;
            return this;
        }

        public InstrumentFilterCriteria build() {
            return criteria;
        }
    }

    @Override
    public String toString() {
        return String.format("FilterCriteria{segments=%s, types=%s, exchanges=%s, limit=%d}",
                segments, instrumentTypes, exchanges, limit != null ? limit : -1);
    }
}
