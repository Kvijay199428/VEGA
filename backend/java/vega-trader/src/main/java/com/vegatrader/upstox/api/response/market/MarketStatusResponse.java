package com.vegatrader.upstox.api.response.market;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response DTO for market status.
 *
 * @since 2.0.0
 */
public class MarketStatusResponse {

    @SerializedName("exchanges")
    private List<ExchangeStatus> exchanges;

    public MarketStatusResponse() {
    }

    public List<ExchangeStatus> getExchanges() {
        return exchanges;
    }

    public void setExchanges(List<ExchangeStatus> exchanges) {
        this.exchanges = exchanges;
    }

    public boolean isAnyExchangeOpen() {
        if (exchanges == null)
            return false;
        return exchanges.stream().anyMatch(ExchangeStatus::isOpen);
    }

    public ExchangeStatus getExchangeStatus(String exchangeName) {
        if (exchanges == null)
            return null;
        return exchanges.stream()
                .filter(e -> exchangeName.equalsIgnoreCase(e.getExchange()))
                .findFirst()
                .orElse(null);
    }

    public static class ExchangeStatus {
        @SerializedName("exchange")
        private String exchange;

        @SerializedName("status")
        private String status;

        @SerializedName("market_type")
        private String marketType;

        @SerializedName("open_time")
        private String openTime;

        @SerializedName("close_time")
        private String closeTime;

        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMarketType() {
            return marketType;
        }

        public void setMarketType(String marketType) {
            this.marketType = marketType;
        }

        public String getOpenTime() {
            return openTime;
        }

        public void setOpenTime(String openTime) {
            this.openTime = openTime;
        }

        public String getCloseTime() {
            return closeTime;
        }

        public void setCloseTime(String closeTime) {
            this.closeTime = closeTime;
        }

        public boolean isOpen() {
            return "OPEN".equalsIgnoreCase(status);
        }

        public boolean isClosed() {
            return "CLOSED".equalsIgnoreCase(status);
        }

        public boolean isPreOpen() {
            return "PRE_OPEN".equalsIgnoreCase(status);
        }

        @Override
        public String toString() {
            return String.format("%s: %s (%s - %s)",
                    exchange, status, openTime, closeTime);
        }
    }
}
