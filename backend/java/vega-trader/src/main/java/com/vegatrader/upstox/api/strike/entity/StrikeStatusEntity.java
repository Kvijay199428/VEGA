package com.vegatrader.upstox.api.strike.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity for strike_status table.
 * 
 * @since 4.5.0
 */
@Entity
@Table(name = "strike_status")
@IdClass(StrikeStatusId.class)
public class StrikeStatusEntity {

    @Id
    @Column(name = "exchange", length = 10)
    private String exchange;

    @Id
    @Column(name = "underlying", length = 50)
    private String underlying;

    @Id
    @Column(name = "strike_price")
    private Double strikePrice;

    @Id
    @Column(name = "option_type", length = 5)
    private String optionType; // CE / PE

    @Column(name = "enabled")
    private Boolean enabled = true;

    @Column(name = "open_interest")
    private Integer openInterest = 0;

    @Column(name = "disabled_reason")
    private String disabledReason;

    @Column(name = "disabled_at")
    private LocalDateTime disabledAt;

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

    public Double getStrikePrice() {
        return strikePrice;
    }

    public void setStrikePrice(Double strikePrice) {
        this.strikePrice = strikePrice;
    }

    public String getOptionType() {
        return optionType;
    }

    public void setOptionType(String optionType) {
        this.optionType = optionType;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getOpenInterest() {
        return openInterest;
    }

    public void setOpenInterest(Integer openInterest) {
        this.openInterest = openInterest;
    }

    public String getDisabledReason() {
        return disabledReason;
    }

    public void setDisabledReason(String disabledReason) {
        this.disabledReason = disabledReason;
    }

    public LocalDateTime getDisabledAt() {
        return disabledAt;
    }

    public void setDisabledAt(LocalDateTime disabledAt) {
        this.disabledAt = disabledAt;
    }

    // --- Utility ---

    public boolean isCall() {
        return "CE".equalsIgnoreCase(optionType);
    }

    public boolean isPut() {
        return "PE".equalsIgnoreCase(optionType);
    }
}
