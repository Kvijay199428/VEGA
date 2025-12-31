package com.vegatrader.upstox.api.instrument.repository;

import com.vegatrader.upstox.api.instrument.entity.ProductRiskProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for product_risk_profile table.
 * 
 * @since 4.0.0
 */
@Repository
public interface ProductRiskProfileRepository extends JpaRepository<ProductRiskProfileEntity, String> {

    // Basic CRUD is sufficient for this lookup table
}
