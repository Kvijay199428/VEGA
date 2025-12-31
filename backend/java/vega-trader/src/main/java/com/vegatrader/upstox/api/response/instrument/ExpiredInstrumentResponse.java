package com.vegatrader.upstox.api.response.instrument;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response DTO for expired instruments data.
 *
 * @since 2.0.0
 */
public class ExpiredInstrumentResponse {

    @SerializedName("expiries")
    private List<String> expiries;

    @SerializedName("contracts")
    private List<ExpiredContract> contracts;

    public ExpiredInstrumentResponse() {
    }

    public List<String> getExpiries() {
        return expiries;
    }

    public void setExpiries(List<String> expiries) {
        this.expiries = expiries;
    }

    public List<ExpiredContract> getContracts() {
        return contracts;
    }

    public void setContracts(List<ExpiredContract> contracts) {
        this.contracts = contracts;
    }

    public int getExpiryCount() {
        return expiries != null ? expiries.size() : 0;
    }

    public int getContractCount() {
        return contracts != null ? contracts.size() : 0;
    }

    public static class ExpiredContract {
        @SerializedName("instrument_key")
        private String instrumentKey;

        @SerializedName("strike_price")
        private Double strikePrice;

        @SerializedName("option_type")
        private String optionType;

        @SerializedName("expiry_date")
        private String expiryDate;

        @SerializedName("lot_size")
        private Integer lotSize;

        @SerializedName("tradingsymbol")
        private String tradingSymbol;

        public String getInstrumentKey() {
            return instrumentKey;
        }

        public void setInstrumentKey(String instrumentKey) {
            this.instrumentKey = instrumentKey;
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

        public String getExpiryDate() {
            return expiryDate;
        }

        public void setExpiryDate(String expiryDate) {
            this.expiryDate = expiryDate;
        }

        public Integer getLotSize() {
            return lotSize;
        }

        public void setLotSize(Integer lotSize) {
            this.lotSize = lotSize;
        }

        public String getTradingSymbol() {
            return tradingSymbol;
        }

        public void setTradingSymbol(String tradingSymbol) {
            this.tradingSymbol = tradingSymbol;
        }

        public boolean isCallOption() {
            return "CE".equalsIgnoreCase(optionType);
        }

        public boolean isPutOption() {
            return "PE".equalsIgnoreCase(optionType);
        }

        @Override
        public String toString() {
            return String.format("%s %s %.0f %s", tradingSymbol, expiryDate, strikePrice, optionType);
        }
    }

    @Override
    public String toString() {
        return String.format("ExpiredInstruments{expiries=%d, contracts=%d}",
                getExpiryCount(), getContractCount());
    }
}
