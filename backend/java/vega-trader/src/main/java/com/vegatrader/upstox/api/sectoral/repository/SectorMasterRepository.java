package com.vegatrader.upstox.api.sectoral.repository;

import com.vegatrader.upstox.api.sectoral.entity.SectorMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for sector_master table.
 * 
 * @since 4.3.0
 */
@Repository
public interface SectorMasterRepository extends JpaRepository<SectorMasterEntity, String> {

    List<SectorMasterEntity> findByActiveTrueOrderByDisplayOrderAsc();

    List<SectorMasterEntity> findByCategoryAndActiveTrueOrderByDisplayOrderAsc(String category);

    @Query("SELECT s FROM SectorMasterEntity s WHERE s.category = 'SECTORAL' AND s.active = true ORDER BY s.displayOrder")
    List<SectorMasterEntity> findAllSectoral();

    @Query("SELECT s FROM SectorMasterEntity s WHERE s.category = 'THEMATIC' AND s.active = true ORDER BY s.displayOrder")
    List<SectorMasterEntity> findAllThematic();

    @Query("SELECT s FROM SectorMasterEntity s WHERE s.category = 'BROAD' AND s.active = true ORDER BY s.displayOrder")
    List<SectorMasterEntity> findAllBroad();
}
