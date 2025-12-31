package com.vegatrader.upstox.api.sectoral.repository;

import com.vegatrader.upstox.api.sectoral.entity.IndexMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for index_master table.
 * 
 * @since 4.3.0
 */
@Repository
public interface IndexMasterRepository extends JpaRepository<IndexMasterEntity, String> {

    List<IndexMasterEntity> findByActiveTrueOrderByIndexName();

    List<IndexMasterEntity> findBySectorCodeAndActiveTrue(String sectorCode);

    @Query("SELECT i FROM IndexMasterEntity i WHERE i.active = true AND (i.lastUpdated IS NULL OR i.lastUpdated < :today)")
    List<IndexMasterEntity> findIndicesNeedingRefresh(@Param("today") LocalDate today);

    @Modifying
    @Query("UPDATE IndexMasterEntity i SET i.lastUpdated = :date, i.constituentCount = :count WHERE i.indexCode = :indexCode")
    int updateLastRefreshed(@Param("indexCode") String indexCode, @Param("date") LocalDate date,
            @Param("count") int count);
}
