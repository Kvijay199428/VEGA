package com.vegatrader.upstox.api.sectoral.repository;

import com.vegatrader.upstox.api.sectoral.entity.IndexConstituentEntity;
import com.vegatrader.upstox.api.sectoral.entity.IndexConstituentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for index_constituent table.
 * 
 * @since 4.3.0
 */
@Repository
public interface IndexConstituentRepository extends JpaRepository<IndexConstituentEntity, IndexConstituentId> {

    List<IndexConstituentEntity> findByIndexCode(String indexCode);

    List<IndexConstituentEntity> findBySymbol(String symbol);

    List<IndexConstituentEntity> findByInstrumentKey(String instrumentKey);

    @Query("SELECT DISTINCT c.instrumentKey FROM IndexConstituentEntity c WHERE c.indexCode = :indexCode")
    List<String> findInstrumentKeysByIndex(@Param("indexCode") String indexCode);

    @Query("SELECT c FROM IndexConstituentEntity c WHERE c.industry = :industry")
    List<IndexConstituentEntity> findByIndustry(@Param("industry") String industry);

    @Query("SELECT DISTINCT c.industry FROM IndexConstituentEntity c WHERE c.industry IS NOT NULL ORDER BY c.industry")
    List<String> findDistinctIndustries();

    @Query("SELECT c FROM IndexConstituentEntity c " +
            "JOIN IndexMasterEntity i ON c.indexCode = i.indexCode " +
            "WHERE i.sectorCode = :sectorCode")
    List<IndexConstituentEntity> findBySector(@Param("sectorCode") String sectorCode);

    @Modifying
    @Query("DELETE FROM IndexConstituentEntity c WHERE c.indexCode = :indexCode")
    int deleteByIndexCode(@Param("indexCode") String indexCode);

    long countByIndexCode(String indexCode);
}
