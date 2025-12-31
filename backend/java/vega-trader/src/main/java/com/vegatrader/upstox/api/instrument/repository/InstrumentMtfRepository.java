package com.vegatrader.upstox.api.instrument.repository;

import com.vegatrader.upstox.api.instrument.entity.InstrumentMtfEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for instrument_mtf overlay table.
 * 
 * @since 4.0.0
 */
@Repository
public interface InstrumentMtfRepository extends JpaRepository<InstrumentMtfEntity, String> {

    /**
     * Find by trading date.
     */
    List<InstrumentMtfEntity> findByTradingDate(LocalDate tradingDate);

    /**
     * Find MTF enabled instruments.
     */
    List<InstrumentMtfEntity> findByMtfEnabledTrue();

    /**
     * Check if MTF is enabled for instrument.
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM InstrumentMtfEntity e " +
            "WHERE e.instrumentKey = :key AND e.mtfEnabled = true")
    boolean isMtfEnabled(@Param("key") String instrumentKey);

    /**
     * Delete by trading date (for daily refresh).
     */
    @Modifying
    @Query("DELETE FROM InstrumentMtfEntity e WHERE e.tradingDate < :date")
    int deleteByTradingDateBefore(@Param("date") LocalDate date);
}
