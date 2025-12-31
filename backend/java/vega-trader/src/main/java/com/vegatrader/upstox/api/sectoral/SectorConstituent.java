package com.vegatrader.upstox.api.sectoral;

import com.google.gson.annotations.SerializedName;

/**
 * Data transfer object representing a constituent stock in a sectoral index.
 * <p>
 * Contains information about a single stock that is part of a Nifty sectoral
 * index,
 * including its symbol, company name, industry, weight in the index, and other
 * metrics.
 * </p>
 * <p>
 * <b>Usage Example:</b>
 * 
 * <pre>{@code
 * SectorConstituent stock = new SectorConstituent();
 * stock.setSymbol("RELIANCE");
 * stock.setCompanyName("Reliance Industries Limited");
 * stock.setIndustry("Refineries");
 * stock.setWeight(9.82);
 * }</pre>
 * </p>
 *
 * @since 2.0.0
 */
public class SectorConstituent {

    @SerializedName("symbol")
    private String symbol;

    @SerializedName("company_name")
    private String companyName;

    @SerializedName("industry")
    private String industry;

    @SerializedName("series")
    private String series;

    @SerializedName("isin_code")
    private String isinCode;

    @SerializedName("weight")
    private Double weight;

    @SerializedName("market_cap")
    private Long marketCap;

    @SerializedName("instrument_key")
    private String instrumentKey;

    /**
     * Default constructor.
     */
    public SectorConstituent() {
    }

    /**
     * Creates a sector constituent with basic information.
     *
     * @param symbol      the stock symbol
     * @param companyName the company name
     * @param weight      the weight in the index (percentage)
     */
    public SectorConstituent(String symbol, String companyName, Double weight) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.weight = weight;
    }

    /**
     * Builder for creating sector constituents.
     *
     * @return a new SectorConstituentBuilder
     */
    public static SectorConstituentBuilder builder() {
        return new SectorConstituentBuilder();
    }

    // Getters and Setters

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

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getIsinCode() {
        return isinCode;
    }

    public void setIsinCode(String isinCode) {
        this.isinCode = isinCode;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Long getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(Long marketCap) {
        this.marketCap = marketCap;
    }

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    /**
     * Generates the instrument key for Upstox API.
     * <p>
     * Format: NSE_EQ|{ISIN_CODE}
     * </p>
     *
     * @return the instrument key
     */
    public String generateInstrumentKey() {
        if (isinCode != null && !isinCode.isEmpty()) {
            this.instrumentKey = "NSE_EQ|" + isinCode;
        }
        return instrumentKey;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - Weight: %.2f%%",
                symbol, companyName, weight != null ? weight : 0.0);
    }

    /**
     * Builder class for SectorConstituent.
     */
    public static class SectorConstituentBuilder {
        private String symbol;
        private String companyName;
        private String industry;
        private String series;
        private String isinCode;
        private Double weight;
        private Long marketCap;

        public SectorConstituentBuilder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public SectorConstituentBuilder companyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public SectorConstituentBuilder industry(String industry) {
            this.industry = industry;
            return this;
        }

        public SectorConstituentBuilder series(String series) {
            this.series = series;
            return this;
        }

        public SectorConstituentBuilder isinCode(String isinCode) {
            this.isinCode = isinCode;
            return this;
        }

        public SectorConstituentBuilder weight(Double weight) {
            this.weight = weight;
            return this;
        }

        public SectorConstituentBuilder marketCap(Long marketCap) {
            this.marketCap = marketCap;
            return this;
        }

        public SectorConstituent build() {
            SectorConstituent constituent = new SectorConstituent();
            constituent.symbol = this.symbol;
            constituent.companyName = this.companyName;
            constituent.industry = this.industry;
            constituent.series = this.series;
            constituent.isinCode = this.isinCode;
            constituent.weight = this.weight;
            constituent.marketCap = this.marketCap;
            constituent.generateInstrumentKey();
            return constituent;
        }
    }
}
