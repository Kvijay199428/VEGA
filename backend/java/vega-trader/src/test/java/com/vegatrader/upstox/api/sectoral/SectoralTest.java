package com.vegatrader.upstox.api.sectoral;

import com.vegatrader.upstox.api.sectoral.entity.*;
import com.vegatrader.upstox.api.sectoral.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Sectoral Indexing module.
 * Tests entities, sector service, and index loader logic.
 * 
 * Per testing-guide/a1.md sections 2-3
 */
class SectoralTest {

    // === 2.2 Instrument Key Determinism Tests ===

    @Test
    @DisplayName("SectorMasterEntity: category checks")
    void sectorMasterCategoryChecks() {
        SectorMasterEntity sectoral = new SectorMasterEntity();
        sectoral.setSectorCode("IT");
        sectoral.setSectorName("Information Technology");
        sectoral.setCategory("SECTORAL");

        assertTrue(sectoral.isSectoral());
        assertFalse(sectoral.isThematic());
        assertFalse(sectoral.isBroad());
    }

    @Test
    @DisplayName("SectorMasterEntity: thematic category")
    void sectorMasterThematicCategory() {
        SectorMasterEntity thematic = new SectorMasterEntity();
        thematic.setCategory("THEMATIC");

        assertTrue(thematic.isThematic());
        assertFalse(thematic.isSectoral());
    }

    @Test
    @DisplayName("SectorMasterEntity: broad category")
    void sectorMasterBroadCategory() {
        SectorMasterEntity broad = new SectorMasterEntity();
        broad.setCategory("BROAD");

        assertTrue(broad.isBroad());
        assertFalse(broad.isSectoral());
    }

    // === 3.1 Index Master Tests ===

    @Test
    @DisplayName("IndexMasterEntity: needs refresh when null")
    void indexMasterNeedsRefreshWhenNull() {
        IndexMasterEntity index = new IndexMasterEntity();
        index.setIndexCode("NIFTY_IT");
        index.setLastUpdated(null);

        assertTrue(index.needsRefresh());
    }

    @Test
    @DisplayName("IndexMasterEntity: needs refresh when old")
    void indexMasterNeedsRefreshWhenOld() {
        IndexMasterEntity index = new IndexMasterEntity();
        index.setIndexCode("NIFTY_IT");
        index.setLastUpdated(LocalDate.now().minusDays(1));

        assertTrue(index.needsRefresh());
    }

    @Test
    @DisplayName("IndexMasterEntity: no refresh when current")
    void indexMasterNoRefreshWhenCurrent() {
        IndexMasterEntity index = new IndexMasterEntity();
        index.setIndexCode("NIFTY_IT");
        index.setLastUpdated(LocalDate.now());

        assertFalse(index.needsRefresh());
    }

    // === 3.2 Index Constituent Tests ===

    @Test
    @DisplayName("IndexConstituentEntity: instrument key format")
    void indexConstituentInstrumentKeyFormat() {
        IndexConstituentEntity constituent = new IndexConstituentEntity();
        constituent.setIndexCode("NIFTY_IT");
        constituent.setSymbol("INFY");
        constituent.setInstrumentKey("NSE_EQ|INFY");

        assertEquals("NSE_EQ|INFY", constituent.getInstrumentKey());
        assertTrue(constituent.getInstrumentKey().startsWith("NSE_EQ|"));
    }

    @Test
    @DisplayName("IndexConstituentId: equality check")
    void indexConstituentIdEquality() {
        IndexConstituentId id1 = new IndexConstituentId("NIFTY_IT", "NSE_EQ|INFY");
        IndexConstituentId id2 = new IndexConstituentId("NIFTY_IT", "NSE_EQ|INFY");
        IndexConstituentId id3 = new IndexConstituentId("NIFTY_BANK", "NSE_EQ|INFY");

        assertEquals(id1, id2);
        assertNotEquals(id1, id3);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    // === 3.3 Sector Risk Limit Tests ===

    @Test
    @DisplayName("SectorRiskLimitEntity: isBlocked check")
    void sectorRiskLimitIsBlocked() {
        SectorRiskLimitEntity blocked = new SectorRiskLimitEntity();
        blocked.setSectorCode("REALTY");
        blocked.setTradingBlocked(true);
        blocked.setBlockReason("Surveillance");

        SectorRiskLimitEntity active = new SectorRiskLimitEntity();
        active.setSectorCode("IT");
        active.setTradingBlocked(false);

        assertTrue(blocked.isBlocked());
        assertFalse(active.isBlocked());
    }

    @Test
    @DisplayName("SectorRiskLimitEntity: exposure limits")
    void sectorRiskLimitExposure() {
        SectorRiskLimitEntity limit = new SectorRiskLimitEntity();
        limit.setSectorCode("IT");
        limit.setMaxExposurePct(40.0);
        limit.setMaxOpenPositions(50);

        assertEquals(40.0, limit.getMaxExposurePct());
        assertEquals(50, limit.getMaxOpenPositions());
    }

    // === 5.1 Search Mode Tests (verified via entity) ===

    @Test
    @DisplayName("IndexConstituentEntity: industry field for search")
    void indexConstituentIndustryForSearch() {
        IndexConstituentEntity constituent = new IndexConstituentEntity();
        constituent.setIndustry("Software & Services");
        constituent.setCompanyName("Infosys Limited");

        assertNotNull(constituent.getIndustry());
        assertNotNull(constituent.getCompanyName());
    }

    @Test
    @DisplayName("IndexConstituentEntity: weight for ranking")
    void indexConstituentWeightForRanking() {
        IndexConstituentEntity constituent = new IndexConstituentEntity();
        constituent.setWeight(15.5);

        assertEquals(15.5, constituent.getWeight());
    }
}
