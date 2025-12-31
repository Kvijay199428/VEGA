package com.vegatrader.upstox.api.response.websocket;

import com.google.gson.annotations.SerializedName;

/**
 * Container for different feed data types in Market Data Feed V3.
 * 
 * <p>
 * Depending on the subscription mode, different fields will be populated:
 * <ul>
 * <li>LTPC mode: only ltpc field</li>
 * <li>OPTION_GREEKS mode: only optionGreeks field</li>
 * <li>FULL mode: ltpc, optionGreeks, marketOhlc, marketLevel (5 levels)</li>
 * <li>FULL_D30 mode: ltpc, optionGreeks, marketOhlc, marketLevel (30
 * levels)</li>
 * </ul>
 * 
 * @since 3.0.0
 */
public class FeedData {

    /**
     * LTPC (Last Traded Price and Close Price) data.
     * Available in: LTPC, FULL, FULL_D30 modes.
     */
    @SerializedName("ltpc")
    private LTPCData ltpc;

    /**
     * Option Greeks data.
     * Available in: OPTION_GREEKS, FULL, FULL_D30 modes.
     */
    @SerializedName("optionGreeks")
    private OptionGreeksData optionGreeks;

    /**
     * Market OHLC data.
     * Available in: FULL, FULL_D30 modes.
     */
    @SerializedName("marketOhlc")
    private MarketOHLCData marketOhlc;

    /**
     * Market depth/level data.
     * Available in: FULL (5 levels), FULL_D30 (30 levels) modes.
     */
    @SerializedName("marketLevel")
    private MarketLevelData marketLevel;

    public FeedData() {
    }

    // Getters and Setters

    public LTPCData getLtpc() {
        return ltpc;
    }

    public void setLtpc(LTPCData ltpc) {
        this.ltpc = ltpc;
    }

    public OptionGreeksData getOptionGreeks() {
        return optionGreeks;
    }

    public void setOptionGreeks(OptionGreeksData optionGreeks) {
        this.optionGreeks = optionGreeks;
    }

    public MarketOHLCData getMarketOhlc() {
        return marketOhlc;
    }

    public void setMarketOhlc(MarketOHLCData marketOhlc) {
        this.marketOhlc = marketOhlc;
    }

    public MarketLevelData getMarketLevel() {
        return marketLevel;
    }

    public void setMarketLevel(MarketLevelData marketLevel) {
        this.marketLevel = marketLevel;
    }

    /**
     * Checks if LTPC data is available.
     * 
     * @return true if ltpc is not null
     */
    public boolean hasLtpc() {
        return ltpc != null;
    }

    /**
     * Checks if Option Greeks data is available.
     * 
     * @return true if optionGreeks is not null
     */
    public boolean hasOptionGreeks() {
        return optionGreeks != null;
    }

    /**
     * Checks if Market OHLC data is available.
     * 
     * @return true if marketOhlc is not null
     */
    public boolean hasMarketOhlc() {
        return marketOhlc != null;
    }

    /**
     * Checks if Market Level data is available.
     * 
     * @return true if marketLevel is not null
     */
    public boolean hasMarketLevel() {
        return marketLevel != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("FeedData{");
        if (hasLtpc())
            sb.append("ltpc=").append(ltpc).append(", ");
        if (hasOptionGreeks())
            sb.append("greeks=").append(optionGreeks).append(", ");
        if (hasMarketOhlc())
            sb.append("ohlc=").append(marketOhlc).append(", ");
        if (hasMarketLevel())
            sb.append("depth=").append(marketLevel.getDepthLevels()).append(" levels");
        sb.append("}");
        return sb.toString();
    }
}
