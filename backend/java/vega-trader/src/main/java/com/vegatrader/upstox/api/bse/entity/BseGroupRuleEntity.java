package com.vegatrader.upstox.api.bse.entity;

import jakarta.persistence.*;

/**
 * JPA Entity for bse_group_rule table.
 * Per a1.md section 3.2.
 * 
 * @since 4.5.0
 */
@Entity
@Table(name = "bse_group_rule")
public class BseGroupRuleEntity {

    @Id
    @Column(name = "group_code", length = 5)
    private String groupCode;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "cnc_only")
    private Boolean cncOnly = false;

    @Column(name = "trade_for_trade")
    private Boolean tradeForTrade = false;

    @Column(name = "quantity_cap")
    private Integer quantityCap;

    @Column(name = "margin_multiplier")
    private Double marginMultiplier = 1.0;

    @Column(name = "active")
    private Boolean active = true;

    // --- Getters and Setters ---

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Boolean getCncOnly() {
        return cncOnly;
    }

    public void setCncOnly(Boolean cncOnly) {
        this.cncOnly = cncOnly;
    }

    public Boolean getTradeForTrade() {
        return tradeForTrade;
    }

    public void setTradeForTrade(Boolean tradeForTrade) {
        this.tradeForTrade = tradeForTrade;
    }

    public Integer getQuantityCap() {
        return quantityCap;
    }

    public void setQuantityCap(Integer quantityCap) {
        this.quantityCap = quantityCap;
    }

    public Double getMarginMultiplier() {
        return marginMultiplier;
    }

    public void setMarginMultiplier(Double marginMultiplier) {
        this.marginMultiplier = marginMultiplier;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    // --- RMS Enforcement per a1.md section 3.3 ---

    public boolean isTradeForTrade() {
        return Boolean.TRUE.equals(tradeForTrade);
    }

    public boolean isCncOnly() {
        return Boolean.TRUE.equals(cncOnly);
    }

    public boolean isBlocked() {
        return !Boolean.TRUE.equals(active);
    }
}
