package com.vegatrader.upstox.api.sectoral.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity for index_master table.
 * 
 * @since 4.3.0
 */
@Entity
@Table(name = "index_master")
public class IndexMasterEntity {

    @Id
    @Column(name = "index_code", length = 64)
    private String indexCode;

    @Column(name = "index_name", nullable = false)
    private String indexName;

    @Column(name = "sector_code", length = 32)
    private String sectorCode;

    @Column(name = "exchange", nullable = false, length = 8)
    private String exchange = "NSE";

    @Column(name = "source_url", nullable = false)
    private String sourceUrl;

    @Column(name = "csv_format", length = 32)
    private String csvFormat = "NIFTY_INDICES";

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "last_updated")
    private LocalDate lastUpdated;

    @Column(name = "constituent_count")
    private Integer constituentCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_code", insertable = false, updatable = false)
    private SectorMasterEntity sector;

    public IndexMasterEntity() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public String getIndexCode() {
        return indexCode;
    }

    public void setIndexCode(String indexCode) {
        this.indexCode = indexCode;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getSectorCode() {
        return sectorCode;
    }

    public void setSectorCode(String sectorCode) {
        this.sectorCode = sectorCode;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getCsvFormat() {
        return csvFormat;
    }

    public void setCsvFormat(String csvFormat) {
        this.csvFormat = csvFormat;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDate getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDate lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Integer getConstituentCount() {
        return constituentCount;
    }

    public void setConstituentCount(Integer constituentCount) {
        this.constituentCount = constituentCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public SectorMasterEntity getSector() {
        return sector;
    }

    // --- Utility Methods ---

    public boolean needsRefresh() {
        return lastUpdated == null || lastUpdated.isBefore(LocalDate.now());
    }
}
