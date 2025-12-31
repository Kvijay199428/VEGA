package com.vegatrader.upstox.api.broker;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for brokers table.
 * 
 * @since 4.2.0
 */
@Repository
public interface BrokerRepository extends JpaRepository<BrokerEntity, String> {

    List<BrokerEntity> findByEnabledTrueOrderByPriorityAsc();

    @Query("SELECT b FROM BrokerEntity b WHERE b.enabled = true ORDER BY b.priority ASC")
    List<BrokerEntity> findAllEnabled();

    boolean existsByBrokerIdAndEnabledTrue(String brokerId);
}
