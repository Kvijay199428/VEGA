package com.vegatrader.upstox.api.bse.repository;

import com.vegatrader.upstox.api.bse.entity.BseGroupRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for bse_group_rule table.
 */
@Repository
public interface BseGroupRuleRepository extends JpaRepository<BseGroupRuleEntity, String> {

    List<BseGroupRuleEntity> findByActiveTrue();

    @Query("SELECT b FROM BseGroupRuleEntity b WHERE b.tradeForTrade = true AND b.active = true")
    List<BseGroupRuleEntity> findTradeForTradeGroups();

    @Query("SELECT b FROM BseGroupRuleEntity b WHERE b.cncOnly = true AND b.active = true")
    List<BseGroupRuleEntity> findCncOnlyGroups();
}
