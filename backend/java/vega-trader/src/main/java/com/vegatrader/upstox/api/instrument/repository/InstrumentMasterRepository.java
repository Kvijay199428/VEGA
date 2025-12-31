package com.vegatrader.upstox.api.instrument.repository;

import com.vegatrader.upstox.api.instrument.entity.InstrumentMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for instrument_master table.
 * Provides search, autocomplete, and lookup operations.
 * 
 * @since 4.0.0
 */
@Repository
public interface InstrumentMasterRepository extends JpaRepository<InstrumentMasterEntity, String> {

    /**
     * Find by trading symbol, segment, and instrument type.
     */
    List<InstrumentMasterEntity> findByTradingSymbolIgnoreCaseAndSegmentAndInstrumentType(
            String tradingSymbol, String segment, String instrumentType);

    /**
     * Find by segment and instrument type.
     */
    List<InstrumentMasterEntity> findBySegmentAndInstrumentType(String segment, String instrumentType);

    /**
     * Find by underlying key (for derivatives).
     */
    List<InstrumentMasterEntity> findByUnderlyingKey(String underlyingKey);

    /**
     * Find by ISIN.
     */
    Optional<InstrumentMasterEntity> findByIsin(String isin);

    /**
     * Find active instruments by trading date.
     */
    List<InstrumentMasterEntity> findByTradingDateAndIsActiveTrue(LocalDate tradingDate);

    /**
     * Search instruments by symbol pattern (autocomplete).
     */
    @Query("SELECT i FROM InstrumentMasterEntity i " +
            "WHERE LOWER(i.tradingSymbol) LIKE LOWER(CONCAT(:pattern, '%')) " +
            "AND i.isActive = true " +
            "ORDER BY " +
            "CASE WHEN LOWER(i.tradingSymbol) = LOWER(:pattern) THEN 0 ELSE 1 END, " +
            "CASE WHEN i.instrumentType = 'EQ' THEN 0 " +
            "     WHEN i.instrumentType = 'FUT' THEN 1 " +
            "     ELSE 2 END, " +
            "i.tradingSymbol")
    List<InstrumentMasterEntity> searchBySymbolPrefix(@Param("pattern") String pattern);

    /**
     * Full search with segment and type.
     */
    @Query("SELECT i FROM InstrumentMasterEntity i " +
            "WHERE LOWER(i.tradingSymbol) LIKE LOWER(CONCAT('%', :symbol, '%')) " +
            "AND i.segment = :segment " +
            "AND i.instrumentType = :type " +
            "AND i.isActive = true")
    List<InstrumentMasterEntity> searchBySymbolSegmentType(
            @Param("symbol") String symbol,
            @Param("segment") String segment,
            @Param("type") String type);

    /**
     * Find options by underlying and expiry.
     */
    @Query("SELECT i FROM InstrumentMasterEntity i " +
            "WHERE i.underlyingKey = :underlyingKey " +
            "AND i.expiry = :expiry " +
            "AND i.instrumentType IN ('CE', 'PE') " +
            "AND i.isActive = true " +
            "ORDER BY i.strikePrice")
    List<InstrumentMasterEntity> findOptionsByUnderlyingAndExpiry(
            @Param("underlyingKey") String underlyingKey,
            @Param("expiry") LocalDate expiry);

    /**
     * Find futures by underlying.
     */
    @Query("SELECT i FROM InstrumentMasterEntity i " +
            "WHERE i.underlyingKey = :underlyingKey " +
            "AND i.instrumentType = 'FUT' " +
            "AND i.isActive = true " +
            "ORDER BY i.expiry")
    List<InstrumentMasterEntity> findFuturesByUnderlying(@Param("underlyingKey") String underlyingKey);

    /**
     * Get distinct expiry dates for underlying.
     */
    @Query("SELECT DISTINCT i.expiry FROM InstrumentMasterEntity i " +
            "WHERE i.underlyingKey = :underlyingKey " +
            "AND i.expiry IS NOT NULL " +
            "AND i.isActive = true " +
            "ORDER BY i.expiry")
    List<LocalDate> findExpiryDatesByUnderlying(@Param("underlyingKey") String underlyingKey);

    /**
     * Count instruments by segment and type.
     */
    long countBySegmentAndInstrumentType(String segment, String instrumentType);

    /**
     * Delete by trading date (for daily refresh).
     */
    @Modifying
    @Query("DELETE FROM InstrumentMasterEntity i WHERE i.tradingDate < :date")
    int deleteByTradingDateBefore(@Param("date") LocalDate date);

    /**
     * Mark all instruments as inactive for a trading date.
     */
    @Modifying
    @Query("UPDATE InstrumentMasterEntity i SET i.isActive = false WHERE i.tradingDate = :date")
    int deactivateByTradingDate(@Param("date") LocalDate date);
}
