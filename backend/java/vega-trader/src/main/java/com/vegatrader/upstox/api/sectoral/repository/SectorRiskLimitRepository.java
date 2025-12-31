package com.vegatrader.upstox.api.sectoral.repository;

import com.vegatrader.upstox.api.sectoral.entity.SectorRiskLimitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for sector_risk_limit table.
 * 
 * @since 4.3.0
 */
@Repository
public interface SectorRiskLimitRepository extends JpaRepository<SectorRiskLimitEntity, String> {

    @Query("SELECT s FROM SectorRiskLimitEntity s WHERE s.tradingBlocked = true")
    List<SectorRiskLimitEntity> findBlockedSectors();

    @Modifying
    @Query("UPDATE SectorRiskLimitEntity s SET s.tradingBlocked = true, s.blockReason = :reason WHERE s.sectorCode = :sectorCode")
    int blockSector(@Param("sectorCode") String sectorCode, @Param("reason") String reason);

    @Modifying
    @Query("UPDATE SectorRiskLimitEntity s SET s.tradingBlocked = false, s.blockReason = null WHERE s.sectorCode = :sectorCode")
    int unblockSector(@Param("sectorCode") String sectorCode);
}
