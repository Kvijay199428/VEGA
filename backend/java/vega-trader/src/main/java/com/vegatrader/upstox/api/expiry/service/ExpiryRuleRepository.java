package com.vegatrader.upstox.api.expiry.service;

import com.vegatrader.upstox.api.expiry.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for exchange_expiry_rule table.
 * 
 * @since 4.5.0
 */
@Repository
public interface ExpiryRuleRepository extends JpaRepository<ExchangeExpiryRuleEntity, ExchangeExpiryRuleId> {

    Optional<ExchangeExpiryRuleEntity> findByExchangeAndInstrumentTypeAndCycleType(
            String exchange, String instrumentType, String cycleType);

    List<ExchangeExpiryRuleEntity> findByExchangeAndActiveTrue(String exchange);

    List<ExchangeExpiryRuleEntity> findByInstrumentTypeAndActiveTrue(String instrumentType);

    List<ExchangeExpiryRuleEntity> findByActiveTrueOrderByExchangeAscInstrumentTypeAsc();
}
