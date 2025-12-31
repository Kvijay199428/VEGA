package com.vegatrader.upstox.api.sectoral.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity for sector_master table.
 * 
 * @since 4.3.0
 */
@Entity
@Table(name = "sector_master")
public class SectorMasterEntity {

    @Id
    @Column(name = "sector_code", length = 32)
    private String sectorCode;

    @Column(name = "sector_name", nullable = false)
    private String sectorName;

    @Column(name = "category", nullable = false, length = 16)
    private String category = "SECTORAL"; // SECTORAL / THEMATIC / BROAD

    @Column(name = "description")
    private String description;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "display_order")
    private Integer displayOrder = 100;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public SectorMasterEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public String getSectorCode() {
        return sectorCode;
    }

    public void setSectorCode(String sectorCode) {
        this.sectorCode = sectorCode;
    }

    public String getSectorName() {
        return sectorName;
    }

    public void setSectorName(String sectorName) {
        this.sectorName = sectorName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // --- Utility Methods ---

    public boolean isSectoral() {
        return "SECTORAL".equalsIgnoreCase(category);
    }

    public boolean isThematic() {
        return "THEMATIC".equalsIgnoreCase(category);
    }

    public boolean isBroad() {
        return "BROAD".equalsIgnoreCase(category);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
