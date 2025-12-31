package com.vegatrader.upstox.api.instrument.loader;

/**
 * Enum defining Upstox instrument file sources with download URLs.
 * 
 * <p>
 * Files are GZIP compressed JSON files that are refreshed daily at ~6 AM IST.
 * 
 * @since 4.0.0
 */
public enum InstrumentFileSource {

    /**
     * Complete instrument master (all exchanges combined).
     */
    COMPLETE("complete", "https://assets.upstox.com/market-quote/instruments/exchange/complete.json.gz", "BOD"),

    /**
     * NSE instruments only.
     */
    NSE("nse", "https://assets.upstox.com/market-quote/instruments/exchange/NSE.json.gz", "BOD"),

    /**
     * BSE instruments only.
     */
    BSE("bse", "https://assets.upstox.com/market-quote/instruments/exchange/BSE.json.gz", "BOD"),

    /**
     * MCX commodities.
     */
    MCX("mcx", "https://assets.upstox.com/market-quote/instruments/exchange/MCX.json.gz", "BOD"),

    /**
     * Suspended instruments (not tradable).
     */
    SUSPENDED("suspended", "https://assets.upstox.com/market-quote/instruments/exchange/suspended-instrument.json.gz",
            "SUSPENSION"),

    /**
     * Margin Trading Facility instruments.
     */
    MTF("mtf", "https://assets.upstox.com/market-quote/instruments/exchange/MTF.json.gz", "MTF"),

    /**
     * NSE Margin Intraday Square-off instruments.
     */
    NSE_MIS("nse_mis", "https://assets.upstox.com/market-quote/instruments/exchange/NSE_MIS.json.gz", "MIS"),

    /**
     * BSE Margin Intraday Square-off instruments.
     */
    BSE_MIS("bse_mis", "https://assets.upstox.com/market-quote/instruments/exchange/BSE_MIS.json.gz", "MIS");

    private final String key;
    private final String url;
    private final String category;

    InstrumentFileSource(String key, String url, String category) {
        this.key = key;
        this.url = url;
        this.category = category;
    }

    /**
     * Gets the source key.
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the download URL.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets the category (BOD, MIS, MTF, SUSPENSION).
     */
    public String getCategory() {
        return category;
    }

    /**
     * Checks if this is a BOD (Beginning of Day) master file.
     */
    public boolean isBod() {
        return "BOD".equals(category);
    }

    /**
     * Checks if this is an MIS overlay file.
     */
    public boolean isMis() {
        return "MIS".equals(category);
    }

    /**
     * Checks if this is an MTF overlay file.
     */
    public boolean isMtf() {
        return "MTF".equals(category);
    }

    /**
     * Checks if this is a suspension overlay file.
     */
    public boolean isSuspension() {
        return "SUSPENSION".equals(category);
    }

    /**
     * Finds source by key.
     */
    public static InstrumentFileSource fromKey(String key) {
        for (InstrumentFileSource source : values()) {
            if (source.key.equalsIgnoreCase(key)) {
                return source;
            }
        }
        throw new IllegalArgumentException("Unknown instrument file source: " + key);
    }

    @Override
    public String toString() {
        return String.format("InstrumentFileSource{key='%s', category='%s'}", key, category);
    }
}
