package com.vegatrader.upstox.api.broker;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for broker_symbol_mapping table.
 * 
 * @since 4.2.0
 */
@Repository
public interface BrokerSymbolMappingRepository extends JpaRepository<BrokerSymbolMappingEntity, BrokerSymbolMappingId> {

    @Query("SELECT m FROM BrokerSymbolMappingEntity m WHERE m.brokerId = :brokerId AND m.instrumentKey = :instrumentKey")
    Optional<BrokerSymbolMappingEntity> findByBrokerAndInstrument(
            @Param("brokerId") String brokerId,
            @Param("instrumentKey") String instrumentKey);

    List<BrokerSymbolMappingEntity> findByBrokerId(String brokerId);

    @Query("SELECT m.brokerSymbol FROM BrokerSymbolMappingEntity m WHERE m.brokerId = :brokerId AND m.instrumentKey = :instrumentKey")
    Optional<String> findBrokerSymbol(@Param("brokerId") String brokerId, @Param("instrumentKey") String instrumentKey);
}
