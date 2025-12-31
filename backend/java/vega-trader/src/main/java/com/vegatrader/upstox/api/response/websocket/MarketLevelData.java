package com.vegatrader.upstox.api.response.websocket;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Market depth/level data for Market Data Feed V3.
 * 
 * <p>
 * Contains bid and ask quotes at multiple levels (5 or 30 depending on
 * subscription mode).
 * 
 * @since 3.0.0
 */
public class MarketLevelData {

    /**
     * List of bid/ask quotes representing market depth.
     * Full mode includes 5 levels, Full D30 mode includes 30 levels.
     */
    @SerializedName("bidAskQuote")
    private List<BidAskQuote> bidAskQuotes;

    public MarketLevelData() {
    }

    // Getters and Setters

    public List<BidAskQuote> getBidAskQuotes() {
        return bidAskQuotes;
    }

    public void setBidAskQuotes(List<BidAskQuote> bidAskQuotes) {
        this.bidAskQuotes = bidAskQuotes;
    }

    /**
     * Gets the number of market depth levels available.
     * 
     * @return the number of levels, or 0 if no quotes are available
     */
    public int getDepthLevels() {
        return bidAskQuotes != null ? bidAskQuotes.size() : 0;
    }

    /**
     * Gets the best bid price (highest bid).
     * 
     * @return the best bid price, or null if not available
     */
    public Double getBestBid() {
        if (bidAskQuotes != null && !bidAskQuotes.isEmpty()) {
            BidAskQuote first = bidAskQuotes.get(0);
            return first != null ? first.getBp() : null;
        }
        return null;
    }

    /**
     * Gets the best ask price (lowest ask).
     * 
     * @return the best ask price, or null if not available
     */
    public Double getBestAsk() {
        if (bidAskQuotes != null && !bidAskQuotes.isEmpty()) {
            BidAskQuote first = bidAskQuotes.get(0);
            return first != null ? first.getAp() : null;
        }
        return null;
    }

    /**
     * Calculates the total bid quantity across all levels.
     * 
     * @return the total bid quantity
     */
    public Long getTotalBidQuantity() {
        if (bidAskQuotes == null)
            return 0L;
        return bidAskQuotes.stream()
                .filter(q -> q.getBq() != null)
                .mapToLong(BidAskQuote::getBq)
                .sum();
    }

    /**
     * Calculates the total ask quantity across all levels.
     * 
     * @return the total ask quantity
     */
    public Long getTotalAskQuantity() {
        if (bidAskQuotes == null)
            return 0L;
        return bidAskQuotes.stream()
                .filter(q -> q.getAq() != null)
                .mapToLong(BidAskQuote::getAq)
                .sum();
    }

    @Override
    public String toString() {
        return String.format("MarketLevel{levels=%d, bestBid=%.2f, bestAsk=%.2f}",
                getDepthLevels(), getBestBid(), getBestAsk());
    }
}
