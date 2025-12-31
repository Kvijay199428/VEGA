package com.vegatrader.upstox.api.rms.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for client_risk_limits.
 * 
 * @since 4.1.0
 */
@Repository
public interface ClientRiskLimitRepository extends JpaRepository<ClientRiskLimitEntity, String> {

    @Query("SELECT c FROM ClientRiskLimitEntity c WHERE c.tradingEnabled = true")
    List<ClientRiskLimitEntity> findAllEnabled();

    @Query("SELECT c FROM ClientRiskLimitEntity c WHERE c.tradingEnabled = false")
    List<ClientRiskLimitEntity> findAllDisabled();

    @Modifying
    @Query("UPDATE ClientRiskLimitEntity c SET c.tradingEnabled = false WHERE c.clientId = :clientId")
    int disableClient(@Param("clientId") String clientId);

    @Modifying
    @Query("UPDATE ClientRiskLimitEntity c SET c.tradingEnabled = true WHERE c.clientId = :clientId")
    int enableClient(@Param("clientId") String clientId);

    @Modifying
    @Query("UPDATE ClientRiskLimitEntity c SET c.tradingEnabled = false")
    int disableAllClients();

    boolean existsByClientId(String clientId);
}
