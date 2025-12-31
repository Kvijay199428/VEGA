package com.vegatrader.upstox.api.strike.service;

import com.vegatrader.upstox.api.strike.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for strike_status table.
 */
@Repository
public interface StrikeStatusRepository extends JpaRepository<StrikeStatusEntity, StrikeStatusId> {

    List<StrikeStatusEntity> findByEnabledTrue();

    List<StrikeStatusEntity> findByEnabledFalse();

    List<StrikeStatusEntity> findByExchangeAndUnderlyingAndEnabledTrue(String exchange, String underlying);

    List<StrikeStatusEntity> findByExchangeAndUnderlyingAndOptionTypeOrderByStrikePriceAsc(
            String exchange, String underlying, String optionType);
}
