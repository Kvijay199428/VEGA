package com.vegatrader.upstox.api.rms.repository;

import com.vegatrader.upstox.api.rms.entity.RegulatoryWatchlistEntity;
import com.vegatrader.upstox.api.rms.entity.RegulatoryWatchlistId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for regulatory_watchlist.
 * 
 * @since 4.1.0
 */
@Repository
public interface RegulatoryWatchlistRepository extends JpaRepository<RegulatoryWatchlistEntity, RegulatoryWatchlistId> {

    List<RegulatoryWatchlistEntity> findByExchangeAndSymbol(String exchange, String symbol);

    List<RegulatoryWatchlistEntity> findByWatchType(String watchType);

    List<RegulatoryWatchlistEntity> findBySymbol(String symbol);

    @Query("SELECT w FROM RegulatoryWatchlistEntity w WHERE w.symbol = :symbol AND w.watchType = 'PCA' " +
            "AND w.effectiveDate <= :date AND (w.expiryDate IS NULL OR w.expiryDate >= :date)")
    List<RegulatoryWatchlistEntity> findActivePcaForSymbol(@Param("symbol") String symbol,
            @Param("date") LocalDate date);

    @Query("SELECT w FROM RegulatoryWatchlistEntity w WHERE w.watchType = 'PCA' " +
            "AND w.effectiveDate <= :date AND (w.expiryDate IS NULL OR w.expiryDate >= :date)")
    List<RegulatoryWatchlistEntity> findAllActivePca(@Param("date") LocalDate date);

    @Query("SELECT w FROM RegulatoryWatchlistEntity w WHERE w.watchType IN ('SURVEILLANCE', 'ASM', 'GSM') " +
            "AND w.effectiveDate <= :date AND (w.expiryDate IS NULL OR w.expiryDate >= :date)")
    List<RegulatoryWatchlistEntity> findAllActiveSurveillance(@Param("date") LocalDate date);

    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM RegulatoryWatchlistEntity w " +
            "WHERE w.symbol = :symbol AND w.watchType = 'PCA' " +
            "AND w.effectiveDate <= CURRENT_DATE AND (w.expiryDate IS NULL OR w.expiryDate >= CURRENT_DATE)")
    boolean isPcaSymbol(@Param("symbol") String symbol);

    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM RegulatoryWatchlistEntity w " +
            "WHERE w.symbol = :symbol AND w.watchType IN ('SURVEILLANCE', 'ASM', 'GSM') " +
            "AND w.effectiveDate <= CURRENT_DATE AND (w.expiryDate IS NULL OR w.expiryDate >= CURRENT_DATE)")
    boolean isSurveillanceSymbol(@Param("symbol") String symbol);
}
