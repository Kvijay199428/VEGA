package com.vegatrader.upstox.api.rms.eligibility;

import com.vegatrader.upstox.api.instrument.entity.InstrumentMasterEntity;
import com.vegatrader.upstox.api.instrument.repository.InstrumentMasterRepository;
import com.vegatrader.upstox.api.rms.entity.*;
import com.vegatrader.upstox.api.rms.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Resolves product eligibility for an instrument.
 * 
 * <p>
 * Decision flow:
 * <ol>
 * <li>Check if instrument exists</li>
 * <li>Check PCA/Surveillance watchlist</li>
 * <li>Check exchange series (T2T)</li>
 * <li>Check IPO Day-0</li>
 * <li>Check equity security type (SME, PCA, RELIST)</li>
 * <li>Determine margin % from series</li>
 * </ol>
 * 
 * @since 4.1.0
 */
@Service
public class EligibilityResolver {

    private static final Logger logger = LoggerFactory.getLogger(EligibilityResolver.class);

    private final InstrumentMasterRepository instrumentRepo;
    private final RegulatoryWatchlistRepository watchlistRepo;
    private final ExchangeSeriesRepository seriesRepo;
    private final IpoCalendarRepository ipoRepo;
    private final IntradayMarginRepository marginRepo;

    public EligibilityResolver(
            InstrumentMasterRepository instrumentRepo,
            RegulatoryWatchlistRepository watchlistRepo,
            ExchangeSeriesRepository seriesRepo,
            IpoCalendarRepository ipoRepo,
            IntradayMarginRepository marginRepo) {
        this.instrumentRepo = instrumentRepo;
        this.watchlistRepo = watchlistRepo;
        this.seriesRepo = seriesRepo;
        this.ipoRepo = ipoRepo;
        this.marginRepo = marginRepo;
    }

    /**
     * Resolves eligibility for an instrument key.
     */
    public ProductEligibility resolve(String instrumentKey) {
        logger.debug("Resolving eligibility for: {}", instrumentKey);

        // 1. Check instrument exists
        Optional<InstrumentMasterEntity> instrumentOpt = instrumentRepo.findById(instrumentKey);
        if (instrumentOpt.isEmpty()) {
            return ProductEligibility.blocked("INSTRUMENT_NOT_FOUND");
        }

        InstrumentMasterEntity instrument = instrumentOpt.get();
        String symbol = instrument.getTradingSymbol();
        String exchange = instrument.getExchange();
        String series = instrument.getExchangeSeries();
        String securityType = instrument.getEquitySecurityType();

        // 2. Check regulatory watchlist (PCA)
        if (watchlistRepo.isPcaSymbol(symbol)) {
            return ProductEligibility.cncOnly("PCA");
        }

        // 3. Check surveillance
        if (watchlistRepo.isSurveillanceSymbol(symbol)) {
            return ProductEligibility.cncOnly("SURVEILLANCE");
        }

        // 4. Check exchange series (T2T)
        Optional<ExchangeSeriesEntity> seriesOpt = seriesRepo.findByExchangeAndSeriesCode(exchange, series);
        if (seriesOpt.isPresent()) {
            ExchangeSeriesEntity seriesEntity = seriesOpt.get();

            if (Boolean.TRUE.equals(seriesEntity.getTradeForTrade())) {
                return ProductEligibility.cncOnly("T2T");
            }

            if (Boolean.TRUE.equals(seriesEntity.getSurveillance())) {
                return ProductEligibility.cncOnly("SURVEILLANCE_SERIES");
            }
        }

        // 5. Check IPO Day-0
        if (ipoRepo.isListingDay(symbol, exchange)) {
            return ProductEligibility.cncOnly("IPO_DAY0");
        }

        // 6. Check equity security type
        EquitySecurityType type = EquitySecurityType.fromCode(securityType);
        if (!type.isMisAllowed() || !type.isMtfAllowed()) {
            return ProductEligibility.withMargin(
                    type.isMisAllowed(),
                    type.isMtfAllowed(),
                    type.isCncAllowed(),
                    type.name(),
                    100.0,
                    1.0);
        }

        // 7. Get margin from series
        double marginPct = 20.0;
        double leverage = 5.0;
        Optional<IntradayMarginEntity> marginOpt = marginRepo.findByExchangeAndSeriesCode(exchange, series);
        if (marginOpt.isPresent()) {
            marginPct = marginOpt.get().getIntradayMarginPct();
            leverage = marginOpt.get().getIntradayLeverage();
        }

        // Normal eligibility
        boolean misAllowed = seriesOpt.map(s -> Boolean.TRUE.equals(s.getMisAllowed())).orElse(true);
        boolean mtfAllowed = seriesOpt.map(s -> Boolean.TRUE.equals(s.getMtfAllowed())).orElse(true);

        return ProductEligibility.withMargin(misAllowed, mtfAllowed, true, "NORMAL", marginPct, leverage);
    }
}
