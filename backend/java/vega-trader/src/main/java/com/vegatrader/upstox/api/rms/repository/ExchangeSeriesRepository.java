package com.vegatrader.upstox.api.rms.repository;

import com.vegatrader.upstox.api.rms.entity.ExchangeSeriesEntity;
import com.vegatrader.upstox.api.rms.entity.ExchangeSeriesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for exchange_series.
 * 
 * @since 4.1.0
 */
@Repository
public interface ExchangeSeriesRepository extends JpaRepository<ExchangeSeriesEntity, ExchangeSeriesId> {

    Optional<ExchangeSeriesEntity> findByExchangeAndSeriesCode(String exchange, String seriesCode);

    List<ExchangeSeriesEntity> findByExchange(String exchange);

    List<ExchangeSeriesEntity> findByTradeForTradeTrue();

    List<ExchangeSeriesEntity> findBySurveillanceTrue();

    @Query("SELECT e FROM ExchangeSeriesEntity e WHERE e.exchange = :exchange AND e.tradeForTrade = true")
    List<ExchangeSeriesEntity> findT2TSeriesByExchange(@Param("exchange") String exchange);

    @Query("SELECT e FROM ExchangeSeriesEntity e WHERE e.misAllowed = true")
    List<ExchangeSeriesEntity> findMisAllowedSeries();

    @Query("SELECT e FROM ExchangeSeriesEntity e WHERE e.mtfAllowed = true")
    List<ExchangeSeriesEntity> findMtfAllowedSeries();

    boolean existsByExchangeAndSeriesCode(String exchange, String seriesCode);
}
