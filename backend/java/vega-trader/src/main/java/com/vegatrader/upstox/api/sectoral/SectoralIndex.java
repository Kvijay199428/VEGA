package com.vegatrader.upstox.api.sectoral;

/**
 * Enumeration of all 21 NSE Sectoral Indices.
 * <p>
 * Each sector provides access to its constituent stocks through NSE's public
 * CSV files.
 * The CSV files contain information about all stocks in that sector including
 * symbols,
 * company names, weights, and other relevant data.
 * </p>
 * <p>
 * <b>Sector Groups:</b>
 * <ul>
 * <li><b>Banking & Finance:</b> BANK, FINANCIAL_SERVICES, FIN_SERVICES_25_50,
 * PRIVATE_BANK, PSU_BANK, MIDSMALL_FINANCIAL</li>
 * <li><b>Technology & IT:</b> IT, MIDSMALL_IT_TELECOM</li>
 * <li><b>Healthcare & Pharma:</b> HEALTHCARE, PHARMA, NIFTY500_HEALTHCARE,
 * MIDSMALL_HEALTHCARE</li>
 * <li><b>Consumer & FMCG:</b> FMCG, CONSUMER_DURABLES</li>
 * <li><b>Cyclicals & Commodities:</b> AUTO, METAL, OIL_GAS, REALTY</li>
 * <li><b>Others:</b> CHEMICALS, MEDIA</li>
 * </ul>
 * </p>
 * <p>
 * <b>Usage Example:</b>
 * 
 * <pre>{@code
 * // Get sector URL
 * String bankUrl = SectoralIndex.BANK.getFullUrl();
 * 
 * // Get display name
 * String name = SectoralIndex.BANK.getDisplayName();
 * 
 * // Get sector key
 * String key = SectoralIndex.BANK.getSectorKey();
 * }</pre>
 * </p>
 *
 * @since 2.0.0
 */
public enum SectoralIndex {

    // Banking & Finance Sectors
    BANK("nifty_bank", "Nifty Bank Index", "ind_niftybanklist.csv"),
    FINANCIAL_SERVICES("nifty_financial", "Nifty Financial Services", "ind_niftyfinancelist.csv"),
    FIN_SERVICES_25_50("nifty_fin_25_50", "Nifty Fin Services 25/50", "ind_niftyfinancialservices25-50list.csv"),
    FINANCIAL_SERVICES_EX_BANK("nifty_financial_ex_bank", "Nifty Financial Services Ex-Bank",
            "ind_niftyfinancialservicesexbank_list.csv"),
    PRIVATE_BANK("nifty_private_bank", "Nifty Private Bank", "ind_nifty_privatebanklist.csv"),
    PSU_BANK("nifty_psu_bank", "Nifty PSU Bank", "ind_niftypsubanklist.csv"),
    MIDSMALL_FINANCIAL("nifty_midsmall_financial", "Nifty MidSmall Financial Services",
            "ind_niftymidsmallfinancailservice_list.csv"),

    // Technology & IT
    IT("nifty_it", "Nifty IT", "ind_niftyitlist.csv"),
    MIDSMALL_IT_TELECOM("nifty_midsmall_it_telecom", "Nifty MidSmall IT & Telecom",
            "ind_niftymidsmallitAndtelecom_list.csv"),

    // Healthcare & Pharma
    HEALTHCARE("nifty_healthcare", "Nifty Healthcare", "ind_niftyhealthcarelist.csv"),
    PHARMA("nifty_pharma", "Nifty Pharma", "ind_niftypharmalist.csv"),
    NIFTY500_HEALTHCARE("nifty500_healthcare", "Nifty500 Healthcare", "ind_nifty500Healthcare_list.csv"),
    MIDSMALL_HEALTHCARE("nifty_midsmall_healthcare", "Nifty MidSmall Healthcare",
            "ind_niftymidsmallhealthcare_list.csv"),

    // Consumer & FMCG
    FMCG("nifty_fmcg", "Nifty FMCG", "ind_niftyfmcglist.csv"),
    CONSUMER_DURABLES("nifty_consumer_durables", "Nifty Consumer Durables", "ind_niftyconsumerdurableslist.csv"),

    // Cyclicals & Commodities
    AUTO("nifty_auto", "Nifty Auto", "ind_niftyautolist.csv"),
    METAL("nifty_metal", "Nifty Metal", "ind_niftymetallist.csv"),
    OIL_GAS("nifty_oil_gas", "Nifty Oil & Gas", "ind_niftyoilgaslist.csv"),
    REALTY("nifty_realty", "Nifty Realty", "ind_niftyrealtylist.csv"),

    // Others
    CHEMICALS("nifty_chemicals", "Nifty Chemicals", "ind_niftyChemicals_list.csv"),
    MEDIA("nifty_media", "Nifty Media", "ind_niftymedialist.csv"),

    // Energy
    ENERGY("nifty_energy", "Nifty Energy", "ind_niftyenergylist.csv");

    private static final String BASE_URL = "https://www.niftyindices.com/IndexConstituent/";

    private final String sectorKey;
    private final String displayName;
    private final String csvFilename;

    /**
     * Constructor for sectoral index enum.
     *
     * @param sectorKey   unique identifier for the sector
     * @param displayName human-readable sector name
     * @param csvFilename CSV file name on NSE website
     */
    SectoralIndex(String sectorKey, String displayName, String csvFilename) {
        this.sectorKey = sectorKey;
        this.displayName = displayName;
        this.csvFilename = csvFilename;
    }

    /**
     * Gets the sector key (unique identifier).
     *
     * @return the sector key
     */
    public String getSectorKey() {
        return sectorKey;
    }

    /**
     * Gets the display name.
     *
     * @return the human-readable sector name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the CSV filename.
     *
     * @return the CSV filename
     */
    public String getCsvFilename() {
        return csvFilename;
    }

    /**
     * Gets the full URL to download the sector's CSV file.
     *
     * @return the complete URL
     */
    public String getFullUrl() {
        return BASE_URL + csvFilename;
    }

    /**
     * Gets the base URL for all NSE sectoral indices.
     *
     * @return the base URL
     */
    public static String getBaseUrl() {
        return BASE_URL;
    }

    /**
     * Finds a sectoral index by its sector key.
     *
     * @param sectorKey the sector key to search for
     * @return the matching SectoralIndex, or null if not found
     */
    public static SectoralIndex fromSectorKey(String sectorKey) {
        for (SectoralIndex sector : values()) {
            if (sector.sectorKey.equalsIgnoreCase(sectorKey)) {
                return sector;
            }
        }
        return null;
    }

    /**
     * Gets all sectors in a specific group.
     *
     * @param group the sector group ("BANKING", "IT", "HEALTHCARE", etc.)
     * @return array of sectors in that group
     */
    public static SectoralIndex[] getSectorsByGroup(String group) {
        switch (group.toUpperCase()) {
            case "BANKING":
            case "FINANCE":
                return new SectoralIndex[] { BANK, FINANCIAL_SERVICES, FIN_SERVICES_25_50,
                        PRIVATE_BANK, PSU_BANK, MIDSMALL_FINANCIAL };
            case "IT":
            case "TECHNOLOGY":
                return new SectoralIndex[] { IT, MIDSMALL_IT_TELECOM };
            case "HEALTHCARE":
            case "PHARMA":
                return new SectoralIndex[] { HEALTHCARE, PHARMA, NIFTY500_HEALTHCARE, MIDSMALL_HEALTHCARE };
            case "CONSUMER":
            case "FMCG":
                return new SectoralIndex[] { FMCG, CONSUMER_DURABLES };
            case "CYCLICALS":
            case "COMMODITIES":
                return new SectoralIndex[] { AUTO, METAL, OIL_GAS, REALTY };
            default:
                return new SectoralIndex[0];
        }
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", displayName, sectorKey);
    }
}
