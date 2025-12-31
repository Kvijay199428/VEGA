package com.vegatrader.upstox.api.admin.repository;

import com.vegatrader.upstox.api.admin.entity.BrokerRegistryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository for BrokerRegistryEntity.
 * 
 * @since 5.0.0
 */
@Repository
public interface BrokerRegistryRepository extends JpaRepository<BrokerRegistryEntity, Long> {

    /**
     * Find broker by code.
     */
    Optional<BrokerRegistryEntity> findByBrokerCode(String brokerCode);

    /**
     * Find all active brokers.
     */
    List<BrokerRegistryEntity> findByIsActiveTrueOrderByPriorityAsc();

    /**
     * Find brokers by exchange.
     */
    List<BrokerRegistryEntity> findByExchangeAndIsActiveTrueOrderByPriorityAsc(String exchange);

    /**
     * Find brokers by exchange and instrument type.
     */
    List<BrokerRegistryEntity> findByExchangeAndInstrumentTypeAndIsActiveTrueOrderByPriorityAsc(
            String exchange, String instrumentType);

    /**
     * Get highest priority broker for exchange.
     */
    @Query("SELECT b FROM BrokerRegistryEntity b WHERE b.exchange = :exchange AND b.isActive = true " +
            "ORDER BY b.priority ASC LIMIT 1")
    Optional<BrokerRegistryEntity> findTopBrokerByExchange(@Param("exchange") String exchange);

    /**
     * Update broker priority.
     */
    @Modifying
    @Transactional
    @Query("UPDATE BrokerRegistryEntity b SET b.priority = :priority, b.updatedBy = :updatedBy, " +
            "b.updatedAt = CURRENT_TIMESTAMP WHERE b.brokerCode = :brokerCode")
    int updatePriority(@Param("brokerCode") String brokerCode,
            @Param("priority") int priority,
            @Param("updatedBy") String updatedBy);

    /**
     * Activate/Deactivate broker.
     */
    @Modifying
    @Transactional
    @Query("UPDATE BrokerRegistryEntity b SET b.isActive = :active, b.updatedBy = :updatedBy, " +
            "b.updatedAt = CURRENT_TIMESTAMP WHERE b.brokerCode = :brokerCode")
    int updateActiveStatus(@Param("brokerCode") String brokerCode,
            @Param("active") boolean active,
            @Param("updatedBy") String updatedBy);

    /**
     * Count active brokers.
     */
    long countByIsActiveTrue();
}
