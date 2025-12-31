package com.vegatrader.upstox.api.rms.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for client_risk_state.
 * 
 * @since 4.1.0
 */
@Repository
public interface ClientRiskStateRepository extends JpaRepository<ClientRiskStateEntity, String> {

    @Modifying
    @Query("UPDATE ClientRiskStateEntity c SET " +
            "c.grossExposure = c.grossExposure + :exposure, " +
            "c.intradayTurnover = c.intradayTurnover + :turnover " +
            "WHERE c.clientId = :clientId")
    int addExposure(@Param("clientId") String clientId,
            @Param("exposure") double exposure,
            @Param("turnover") double turnover);

    @Modifying
    @Query("UPDATE ClientRiskStateEntity c SET c.currentMtm = :mtm WHERE c.clientId = :clientId")
    int updateMtm(@Param("clientId") String clientId, @Param("mtm") double mtm);

    @Modifying
    @Query("UPDATE ClientRiskStateEntity c SET c.openPositions = c.openPositions + :delta WHERE c.clientId = :clientId")
    int updatePositionCount(@Param("clientId") String clientId, @Param("delta") int delta);

    @Modifying
    @Query("UPDATE ClientRiskStateEntity c SET " +
            "c.grossExposure = 0, c.netExposure = 0, c.intradayTurnover = 0, " +
            "c.currentMtm = 0, c.openPositions = 0 " +
            "WHERE c.clientId = :clientId")
    int resetState(@Param("clientId") String clientId);

    @Modifying
    @Query("UPDATE ClientRiskStateEntity c SET " +
            "c.grossExposure = 0, c.netExposure = 0, c.intradayTurnover = 0, " +
            "c.currentMtm = 0, c.openPositions = 0")
    int resetAllStates();
}
