package com.vegatrader.upstox.api.strike.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity for strike_scheme_rule table.
 * 
 * @since 4.5.0
 */
@Entity
@Table(name = "strike_scheme_rule")
@IdClass(StrikeSchemeId.class)
public class StrikeSchemeEntity {

    @Id
    @Column(name = "exchange", length = 10)
    private String exchange;

    @Id
    @Column(name = "underlying", length = 50)
    private String underlying;

    @Column(name = "strike_interval", nullable = false)
    private Integer strikeInterval;

    @Column(name = "min_strike")
    private Double minStrike;

    @Column(name = "max_strike")
    private Double maxStrike;

    @Column(name = "review_frequency", nullable = false, length = 20)
    private String reviewFrequency; // DAILY / WEEKLY

    @Column(name = "last_reviewed")
    private LocalDateTime lastReviewed;

    @Column(name = "active")
    private Boolean active = true;

    // --- Getters and Setters ---

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getUnderlying() {
        return underlying;
    }

    public void setUnderlying(String underlying) {
        this.underlying = underlying;
    }

    public Integer getStrikeInterval() {
        return strikeInterval;
    }

    public void setStrikeInterval(Integer strikeInterval) {
        this.strikeInterval = strikeInterval;
    }

    public Double getMinStrike() {
        return minStrike;
    }

    public void setMinStrike(Double minStrike) {
        this.minStrike = minStrike;
    }

    public Double getMaxStrike() {
        return maxStrike;
    }

    public void setMaxStrike(Double maxStrike) {
        this.maxStrike = maxStrike;
    }

    public String getReviewFrequency() {
        return reviewFrequency;
    }

    public void setReviewFrequency(String reviewFrequency) {
        this.reviewFrequency = reviewFrequency;
    }

    public LocalDateTime getLastReviewed() {
        return lastReviewed;
    }

    public void setLastReviewed(LocalDateTime lastReviewed) {
        this.lastReviewed = lastReviewed;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
