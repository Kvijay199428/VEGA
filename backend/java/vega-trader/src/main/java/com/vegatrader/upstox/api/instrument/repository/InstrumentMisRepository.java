package com.vegatrader.upstox.api.instrument.repository;

import com.vegatrader.upstox.api.instrument.entity.InstrumentMisEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for instrument_mis overlay table.
 * 
 * @since 4.0.0
 */
@Repository
public interface InstrumentMisRepository extends JpaRepository<InstrumentMisEntity, String> {

    /**
     * Find by trading date.
     */
    List<InstrumentMisEntity> findByTradingDate(LocalDate tradingDate);

    /**
     * Check if instrument is MIS allowed.
     */
    boolean existsByInstrumentKey(String instrumentKey);

    /**
     * Delete by trading date (for daily refresh).
     */
    @Modifying
    @Query("DELETE FROM InstrumentMisEntity e WHERE e.tradingDate < :date")
    int deleteByTradingDateBefore(@Param("date") LocalDate date);
}
