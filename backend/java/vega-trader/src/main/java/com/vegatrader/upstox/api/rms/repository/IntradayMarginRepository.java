package com.vegatrader.upstox.api.rms.repository;

import com.vegatrader.upstox.api.rms.entity.IntradayMarginEntity;
import com.vegatrader.upstox.api.rms.entity.ExchangeSeriesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for intraday_margin_by_series.
 * 
 * @since 4.1.0
 */
@Repository
public interface IntradayMarginRepository extends JpaRepository<IntradayMarginEntity, ExchangeSeriesId> {

    Optional<IntradayMarginEntity> findByExchangeAndSeriesCode(String exchange, String seriesCode);

    @Query("SELECT m FROM IntradayMarginEntity m WHERE m.exchange = :exchange AND m.seriesCode = :series")
    Optional<IntradayMarginEntity> findMarginBySeries(@Param("exchange") String exchange,
            @Param("series") String seriesCode);

    @Query("SELECT m.intradayMarginPct FROM IntradayMarginEntity m WHERE m.exchange = :exchange AND m.seriesCode = :series")
    Double findMarginPct(@Param("exchange") String exchange, @Param("series") String seriesCode);

    @Query("SELECT m.intradayLeverage FROM IntradayMarginEntity m WHERE m.exchange = :exchange AND m.seriesCode = :series")
    Double findLeverage(@Param("exchange") String exchange, @Param("series") String seriesCode);
}
