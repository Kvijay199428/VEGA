package com.vegatrader.upstox.api.response.websocket;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * Market information data for Market Data Feed V3.
 * 
 * <p>
 * Contains the status of various market segments (NSE_EQ, BSE_EQ, NSE_FO,
 * etc.).
 * This is the first message sent when connecting to the WebSocket feed.
 * 
 * @since 3.0.0
 */
public class MarketInfoData {

    /**
     * Map of segment names to their current status.
     * 
     * <p>
     * Example segments:
     * <ul>
     * <li>NSE_EQ - NSE Equity</li>
     * <li>BSE_EQ - BSE Equity</li>
     * <li>NSE_FO - NSE Futures & Options</li>
     * <li>BSE_FO - BSE Futures & Options</li>
     * <li>MCX_FO - MCX Futures & Options</li>
     * <li>NSE_INDEX - NSE Indices</li>
     * <li>BSE_INDEX - BSE Indices</li>
     * <li>MCX_INDEX - MCX Indices</li>
     * <li>NSE_COM - NSE Commodities</li>
     * <li>NCD_FO - NCD Futures & Options</li>
     * <li>BCD_FO - BCD Futures & Options</li>
     * </ul>
     */
    @SerializedName("segmentStatus")
    private Map<String, String> segmentStatus;

    public MarketInfoData() {
    }

    // Getters and Setters

    public Map<String, String> getSegmentStatus() {
        return segmentStatus;
    }

    public void setSegmentStatus(Map<String, String> segmentStatus) {
        this.segmentStatus = segmentStatus;
    }

    /**
     * Gets the status of a specific market segment.
     * 
     * @param segment the segment name (e.g., "NSE_EQ", "BSE_FO")
     * @return the status string, or null if segment not found
     */
    public String getStatus(String segment) {
        return segmentStatus != null ? segmentStatus.get(segment) : null;
    }

    /**
     * Gets the status of a specific market segment as an enum.
     * 
     * @param segment the segment name
     * @return the MarketSegmentStatus enum, or null if segment not found
     */
    public MarketSegmentStatus getStatusEnum(String segment) {
        String status = getStatus(segment);
        if (status != null) {
            try {
                return MarketSegmentStatus.fromValue(status);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Checks if a specific market segment is currently open.
     * 
     * @param segment the segment name
     * @return true if the segment is in NORMAL_OPEN status
     */
    public boolean isSegmentOpen(String segment) {
        MarketSegmentStatus status = getStatusEnum(segment);
        return status != null && status.isOpen();
    }

    /**
     * Checks if NSE Equity market is open.
     * 
     * @return true if NSE_EQ is in NORMAL_OPEN status
     */
    public boolean isNseEquityOpen() {
        return isSegmentOpen("NSE_EQ");
    }

    /**
     * Checks if BSE Equity market is open.
     * 
     * @return true if BSE_EQ is in NORMAL_OPEN status
     */
    public boolean isBseEquityOpen() {
        return isSegmentOpen("BSE_EQ");
    }

    /**
     * Checks if NSE F&O market is open.
     * 
     * @return true if NSE_FO is in NORMAL_OPEN status
     */
    public boolean isNseFoOpen() {
        return isSegmentOpen("NSE_FO");
    }

    @Override
    public String toString() {
        if (segmentStatus == null || segmentStatus.isEmpty()) {
            return "MarketInfo{no segments}";
        }
        long openCount = segmentStatus.values().stream()
                .filter("NORMAL_OPEN"::equals)
                .count();
        return String.format("MarketInfo{segments=%d, open=%d}",
                segmentStatus.size(), openCount);
    }
}
