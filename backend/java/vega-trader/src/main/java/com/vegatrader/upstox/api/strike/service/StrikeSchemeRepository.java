package com.vegatrader.upstox.api.strike.service;

import com.vegatrader.upstox.api.strike.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository for strike_scheme_rule table.
 */
@Repository
public interface StrikeSchemeRepository extends JpaRepository<StrikeSchemeEntity, StrikeSchemeId> {

    Optional<StrikeSchemeEntity> findByExchangeAndUnderlying(String exchange, String underlying);

    List<StrikeSchemeEntity> findByExchange(String exchange);

    List<StrikeSchemeEntity> findByActiveTrueOrderByExchangeAscUnderlyingAsc();
}
