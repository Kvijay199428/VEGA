package com.vegatrader.upstox.api.admin.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Disabled Strike Entity.
 * Tracks strikes that are disabled for trading.
 * 
 * @since 5.0.0
 */
@Entity
@Table(name = "disabled_strikes")
public class DisabledStrikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "underlying_key", nullable = false, length = 64)
    private String underlyingKey; // e.g., NSE_INDEX|Nifty 50

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "strike_price", nullable = false)
    private Double strikePrice;

    @Column(name = "option_type", nullable = false, length = 2)
    private String optionType; // CE, PE

    @Column(name = "disabled_at", nullable = false)
    private Instant disabledAt;

    @Column(name = "disabled_by", nullable = false, length = 64)
    private String disabledBy;

    @Column(name = "reason", length = 512)
    private String reason;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "enabled_at")
    private Instant enabledAt;

    @Column(name = "enabled_by", length = 64)
    private String enabledBy;

    @Column(name = "enable_reason", length = 512)
    private String enableReason;

    // Default constructor
    public DisabledStrikeEntity() {
        this.disabledAt = Instant.now();
        this.active = true;
    }

    // Factory for disabling a strike
    public static DisabledStrikeEntity disable(String underlyingKey, LocalDate expiry,
            double strike, String optionType, String disabledBy, String reason) {
        DisabledStrikeEntity entity = new DisabledStrikeEntity();
        entity.underlyingKey = underlyingKey;
        entity.expiryDate = expiry;
        entity.strikePrice = strike;
        entity.optionType = optionType;
        entity.disabledBy = disabledBy;
        entity.reason = reason;
        return entity;
    }

    // Enable the strike
    public void enable(String enabledBy, String enableReason) {
        this.active = false;
        this.enabledAt = Instant.now();
        this.enabledBy = enabledBy;
        this.enableReason = enableReason;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getUnderlyingKey() {
        return underlyingKey;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public Double getStrikePrice() {
        return strikePrice;
    }

    public String getOptionType() {
        return optionType;
    }

    public Instant getDisabledAt() {
        return disabledAt;
    }

    public String getDisabledBy() {
        return disabledBy;
    }

    public String getReason() {
        return reason;
    }

    public Boolean getActive() {
        return active;
    }

    public Instant getEnabledAt() {
        return enabledAt;
    }

    public String getEnabledBy() {
        return enabledBy;
    }

    public String getEnableReason() {
        return enableReason;
    }

    // Setters
    public void setUnderlyingKey(String underlyingKey) {
        this.underlyingKey = underlyingKey;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setStrikePrice(Double strikePrice) {
        this.strikePrice = strikePrice;
    }

    public void setOptionType(String optionType) {
        this.optionType = optionType;
    }

    public void setDisabledAt(Instant disabledAt) {
        this.disabledAt = disabledAt;
    }

    public void setDisabledBy(String disabledBy) {
        this.disabledBy = disabledBy;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
