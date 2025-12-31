package com.vegatrader.upstox.api.instrument.repository;

import com.vegatrader.upstox.api.instrument.entity.InstrumentSuspensionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for instrument_suspension overlay table.
 * 
 * @since 4.0.0
 */
@Repository
public interface InstrumentSuspensionRepository extends JpaRepository<InstrumentSuspensionEntity, String> {

    /**
     * Find by trading date.
     */
    List<InstrumentSuspensionEntity> findByTradingDate(LocalDate tradingDate);

    /**
     * Check if instrument is suspended.
     */
    boolean existsByInstrumentKey(String instrumentKey);

    /**
     * Delete by trading date (for daily refresh).
     */
    @Modifying
    @Query("DELETE FROM InstrumentSuspensionEntity e WHERE e.tradingDate < :date")
    int deleteByTradingDateBefore(@Param("date") LocalDate date);
}
