package com.vegatrader.upstox.api.rms.repository;

import com.vegatrader.upstox.api.rms.entity.QuantityCapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for symbol_quantity_caps.
 * 
 * @since 4.1.0
 */
@Repository
public interface QuantityCapRepository extends JpaRepository<QuantityCapEntity, String> {

    @Query("SELECT q FROM QuantityCapEntity q WHERE q.instrumentKey = :key " +
            "AND q.effectiveDate <= CURRENT_DATE AND (q.expiryDate IS NULL OR q.expiryDate >= CURRENT_DATE)")
    Optional<QuantityCapEntity> findActiveCapForKey(@Param("key") String instrumentKey);

    @Query("SELECT q FROM QuantityCapEntity q WHERE q.effectiveDate <= :date AND (q.expiryDate IS NULL OR q.expiryDate >= :date)")
    List<QuantityCapEntity> findAllActiveOnDate(@Param("date") LocalDate date);

    boolean existsByInstrumentKey(String instrumentKey);
}
