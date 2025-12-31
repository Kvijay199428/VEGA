package com.vegatrader.upstox.api.rms.repository;

import com.vegatrader.upstox.api.rms.entity.PriceBandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for price_band.
 * 
 * @since 4.1.0
 */
@Repository
public interface PriceBandRepository extends JpaRepository<PriceBandEntity, String> {

    @Query("SELECT p FROM PriceBandEntity p WHERE p.instrumentKey = :key AND p.effectiveDate = CURRENT_DATE")
    Optional<PriceBandEntity> findTodayBandForKey(@Param("key") String instrumentKey);

    @Query("SELECT p FROM PriceBandEntity p WHERE p.effectiveDate = :date")
    List<PriceBandEntity> findAllByDate(@Param("date") LocalDate date);

    @Query("SELECT p FROM PriceBandEntity p WHERE p.instrumentKey = :key ORDER BY p.effectiveDate DESC")
    Optional<PriceBandEntity> findLatestForKey(@Param("key") String instrumentKey);

    @Modifying
    @Query("DELETE FROM PriceBandEntity p WHERE p.effectiveDate < :date")
    int deleteOlderThan(@Param("date") LocalDate date);

    boolean existsByInstrumentKey(String instrumentKey);
}
