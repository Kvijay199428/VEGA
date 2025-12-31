package com.vegatrader.upstox.api.rms.repository;

import com.vegatrader.upstox.api.rms.entity.FoContractLifecycleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for fo_contract_lifecycle.
 * 
 * @since 4.1.0
 */
@Repository
public interface FoContractLifecycleRepository extends JpaRepository<FoContractLifecycleEntity, String> {

    List<FoContractLifecycleEntity> findByUnderlyingKey(String underlyingKey);

    List<FoContractLifecycleEntity> findByUnderlyingKeyAndIsActiveTrue(String underlyingKey);

    @Query("SELECT f FROM FoContractLifecycleEntity f WHERE f.expiryDate < CURRENT_DATE AND f.isActive = true")
    List<FoContractLifecycleEntity> findExpiredActive();

    @Modifying
    @Query("UPDATE FoContractLifecycleEntity f SET f.isActive = false WHERE f.expiryDate < CURRENT_DATE")
    int deactivateExpired();

    @Query("SELECT f FROM FoContractLifecycleEntity f WHERE f.underlyingKey = :underlyingKey " +
            "AND f.expiryDate > CURRENT_DATE AND f.isActive = true ORDER BY f.expiryDate ASC")
    List<FoContractLifecycleEntity> findNextActiveContracts(@Param("underlyingKey") String underlyingKey);

    @Query("SELECT f FROM FoContractLifecycleEntity f WHERE f.underlyingKey = " +
            "(SELECT f2.underlyingKey FROM FoContractLifecycleEntity f2 WHERE f2.instrumentKey = :key) " +
            "AND f.expiryDate > CURRENT_DATE AND f.isActive = true ORDER BY f.expiryDate ASC")
    Optional<FoContractLifecycleEntity> findNextContractForKey(@Param("key") String instrumentKey);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM FoContractLifecycleEntity f " +
            "WHERE f.instrumentKey = :key AND f.isActive = true AND f.expiryDate >= CURRENT_DATE")
    boolean isActiveContract(@Param("key") String instrumentKey);

    @Query("SELECT f FROM FoContractLifecycleEntity f WHERE f.expiryDate = :date")
    List<FoContractLifecycleEntity> findByExpiryDate(@Param("date") LocalDate expiryDate);
}
