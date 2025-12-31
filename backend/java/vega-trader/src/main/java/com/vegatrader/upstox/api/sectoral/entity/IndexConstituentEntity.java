package com.vegatrader.upstox.api.sectoral.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity for index_constituent table.
 * 
 * @since 4.3.0
 */
@Entity
@Table(name = "index_constituent")
@IdClass(IndexConstituentId.class)
public class IndexConstituentEntity {

    @Id
    @Column(name = "index_code", length = 64)
    private String indexCode;

    @Id
    @Column(name = "instrument_key", length = 64)
    private String instrumentKey;

    @Column(name = "symbol", nullable = false, length = 32)
    private String symbol;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "series", length = 8)
    private String series = "EQ";

    @Column(name = "isin", length = 16)
    private String isin;

    @Column(name = "industry")
    private String industry;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "free_float_mcap")
    private Double freeFloatMcap;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "index_code", insertable = false, updatable = false)
    private IndexMasterEntity index;

    public IndexConstituentEntity() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public String getIndexCode() {
        return indexCode;
    }

    public void setIndexCode(String indexCode) {
        this.indexCode = indexCode;
    }

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getFreeFloatMcap() {
        return freeFloatMcap;
    }

    public void setFreeFloatMcap(Double freeFloatMcap) {
        this.freeFloatMcap = freeFloatMcap;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public IndexMasterEntity getIndex() {
        return index;
    }
}
