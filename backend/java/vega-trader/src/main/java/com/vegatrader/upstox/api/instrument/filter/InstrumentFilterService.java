package com.vegatrader.upstox.api.instrument.filter;

import com.vegatrader.upstox.api.response.instrument.InstrumentResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for filtering instruments based on criteria.
 * 
 * @since 3.0.0
 */
@Component
public class InstrumentFilterService {

    /**
     * Filters instruments based on criteria.
     * 
     * @param instruments list of instruments
     * @param criteria    filter criteria
     * @return filtered list
     */
    public List<InstrumentResponse> filter(List<InstrumentResponse> instruments, InstrumentFilterCriteria criteria) {
        if (instruments == null || instruments.isEmpty()) {
            return List.of();
        }

        if (criteria == null) {
            return instruments;
        }

        List<InstrumentResponse> filtered = instruments.stream()
                .filter(criteria::matches)
                .collect(Collectors.toList());

        // Apply limit if specified
        if (criteria.getLimit() != null && criteria.getLimit() > 0) {
            return filtered.stream()
                    .limit(criteria.getLimit())
                    .collect(Collectors.toList());
        }

        return filtered;
    }

    /**
     * Extracts instrument keys from filtered instruments.
     * 
     * @param instruments list of instruments
     * @param criteria    filter criteria
     * @return list of instrument keys
     */
    public List<String> extractInstrumentKeys(List<InstrumentResponse> instruments, InstrumentFilterCriteria criteria) {
        List<InstrumentResponse> filtered = filter(instruments, criteria);
        return filtered.stream()
                .map(InstrumentResponse::getInstrumentKey)
                .collect(Collectors.toList());
    }

    /**
     * Counts instruments matching criteria.
     * 
     * @param instruments list of instruments
     * @param criteria    filter criteria
     * @return count of matching instruments
     */
    public long count(List<InstrumentResponse> instruments, InstrumentFilterCriteria criteria) {
        return filter(instruments, criteria).size();
    }

    /**
     * Quick filter for Reliance Equity example.
     * 
     * @param instruments list of instruments
     * @return filtered Reliance equity instruments
     */
    public List<InstrumentResponse> filterRelianceEquity(List<InstrumentResponse> instruments) {
        InstrumentFilterCriteria criteria = InstrumentFilterCriteria.builder()
                .segment("NSE_EQ")
                .instrumentType("EQ")
                .tradingSymbolPattern("RELIANCE")
                .build();

        return filter(instruments, criteria);
    }

    /**
     * Quick filter for Nifty options.
     * 
     * @param instruments list of instruments
     * @param expiry      expiry date
     * @return filtered Nifty option instruments
     */
    public List<InstrumentResponse> filterNiftyOptions(List<InstrumentResponse> instruments, String expiry) {
        InstrumentFilterCriteria criteria = InstrumentFilterCriteria.builder()
                .segment("NSE_FO")
                .instrumentType("OPTION")
                .tradingSymbolPattern("NIFTY")
                .expiryDate(expiry)
                .build();

        return filter(instruments, criteria);
    }
}
