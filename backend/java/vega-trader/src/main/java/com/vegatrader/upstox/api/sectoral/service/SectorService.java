package com.vegatrader.upstox.api.sectoral.service;

import com.vegatrader.upstox.api.sectoral.entity.*;
import com.vegatrader.upstox.api.sectoral.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Sector service for discovery and search.
 * 
 * @since 4.3.0
 */
@Service
public class SectorService {

    private final SectorMasterRepository sectorRepo;
    private final IndexMasterRepository indexRepo;
    private final IndexConstituentRepository constituentRepo;
    private final SectorRiskLimitRepository riskRepo;

    public SectorService(
            SectorMasterRepository sectorRepo,
            IndexMasterRepository indexRepo,
            IndexConstituentRepository constituentRepo,
            SectorRiskLimitRepository riskRepo) {
        this.sectorRepo = sectorRepo;
        this.indexRepo = indexRepo;
        this.constituentRepo = constituentRepo;
        this.riskRepo = riskRepo;
    }

    // === Sector Queries ===

    public List<SectorMasterEntity> getAllSectors() {
        return sectorRepo.findByActiveTrueOrderByDisplayOrderAsc();
    }

    public List<SectorMasterEntity> getSectoralSectors() {
        return sectorRepo.findAllSectoral();
    }

    public List<SectorMasterEntity> getThematicSectors() {
        return sectorRepo.findAllThematic();
    }

    public List<SectorMasterEntity> getBroadSectors() {
        return sectorRepo.findAllBroad();
    }

    // === Index Queries ===

    public List<IndexMasterEntity> getAllIndices() {
        return indexRepo.findByActiveTrueOrderByIndexName();
    }

    public List<IndexMasterEntity> getIndicesBySector(String sectorCode) {
        return indexRepo.findBySectorCodeAndActiveTrue(sectorCode);
    }

    // === Constituent Queries ===

    public List<IndexConstituentEntity> getConstituents(String indexCode) {
        return constituentRepo.findByIndexCode(indexCode);
    }

    public List<String> getInstrumentKeysByIndex(String indexCode) {
        return constituentRepo.findInstrumentKeysByIndex(indexCode);
    }

    public List<IndexConstituentEntity> getConstituentsBySector(String sectorCode) {
        return constituentRepo.findBySector(sectorCode);
    }

    public List<String> getInstrumentKeysBySector(String sectorCode) {
        return constituentRepo.findBySector(sectorCode).stream()
                .map(IndexConstituentEntity::getInstrumentKey)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<String> getDistinctIndustries() {
        return constituentRepo.findDistinctIndustries();
    }

    public List<IndexConstituentEntity> findByIndustry(String industry) {
        return constituentRepo.findByIndustry(industry);
    }

    // === Symbol to Sector Mapping ===

    public List<String> getSectorsForSymbol(String symbol) {
        return constituentRepo.findBySymbol(symbol).stream()
                .map(c -> c.getIndex())
                .filter(i -> i != null)
                .map(IndexMasterEntity::getSectorCode)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<String> getIndicesForSymbol(String symbol) {
        return constituentRepo.findBySymbol(symbol).stream()
                .map(IndexConstituentEntity::getIndexCode)
                .distinct()
                .collect(Collectors.toList());
    }

    // === Risk Queries ===

    public boolean isSectorBlocked(String sectorCode) {
        return riskRepo.findById(sectorCode)
                .map(SectorRiskLimitEntity::isBlocked)
                .orElse(false);
    }

    public List<SectorRiskLimitEntity> getBlockedSectors() {
        return riskRepo.findBlockedSectors();
    }
}
