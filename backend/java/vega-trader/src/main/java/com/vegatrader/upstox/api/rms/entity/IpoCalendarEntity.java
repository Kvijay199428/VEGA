package com.vegatrader.upstox.api.rms.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity for ipo_calendar table.
 * Tracks listing dates for Day-0 restrictions.
 * 
 * @since 4.1.0
 */
@Entity
@Table(name = "ipo_calendar")
@IdClass(IpoCalendarId.class)
public class IpoCalendarEntity {

    @Id
    @Column(name = "symbol", length = 32)
    private String symbol;

    @Id
    @Column(name = "exchange", length = 8)
    private String exchange;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "listing_date", nullable = false)
    private LocalDate listingDate;

    @Column(name = "issue_price")
    private Double issuePrice;

    @Column(name = "lot_size")
    private Integer lotSize;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public IpoCalendarEntity() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public LocalDate getListingDate() {
        return listingDate;
    }

    public void setListingDate(LocalDate listingDate) {
        this.listingDate = listingDate;
    }

    public Double getIssuePrice() {
        return issuePrice;
    }

    public void setIssuePrice(Double issuePrice) {
        this.issuePrice = issuePrice;
    }

    public Integer getLotSize() {
        return lotSize;
    }

    public void setLotSize(Integer lotSize) {
        this.lotSize = lotSize;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // --- Utility Methods ---

    public boolean isListingDay() {
        return LocalDate.now().equals(listingDate);
    }

    public boolean isPastListing() {
        return LocalDate.now().isAfter(listingDate);
    }
}
