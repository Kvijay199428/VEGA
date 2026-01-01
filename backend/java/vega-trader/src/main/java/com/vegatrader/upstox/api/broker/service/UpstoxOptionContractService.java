package com.vegatrader.upstox.api.broker.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vegatrader.upstox.auth.config.AuthConstants;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Service to interact with Upstox /option/contract API.
 * As per architecture a1.md.
 */
@Service
public class UpstoxOptionContractService {

    private static final Logger logger = LoggerFactory.getLogger(UpstoxOptionContractService.class);
    private final OkHttpClient client;
    private final ObjectMapper mapper;

    public UpstoxOptionContractService(ObjectMapper mapper) {
        this.client = new OkHttpClient();
        this.mapper = mapper;
    }

    /**
     * Fetch option contracts for a given underlying instrument key.
     * 
     * @param instrumentKey The underlying instrument key (e.g., NSE_INDEX|Nifty 50)
     * @param accessToken   Valid OAUTH access token
     * @return List of option contracts
     */
    public List<UpstoxOptionContract> fetchOptionContracts(String instrumentKey, String accessToken) {
        String url = AuthConstants.API_BASE_URL + "/option/contract?instrument_key=" + instrumentKey;

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("Upstox /option/contract failed: POST {} - Code: {}", url, response.code());
                return Collections.emptyList();
            }
            if (response.body() == null)
                return Collections.emptyList();

            UpstoxOptionResponse wrapper = mapper.readValue(response.body().string(), UpstoxOptionResponse.class);
            if (!"success".equalsIgnoreCase(wrapper.getStatus())) {
                logger.error("Upstox API returned error status: {}", wrapper.getStatus());
                return Collections.emptyList();
            }
            if (wrapper.getData() == null) {
                return Collections.emptyList();
            }
            return wrapper.getData();

        } catch (IOException e) {
            logger.error("Error fetching option contracts for {}: {}", instrumentKey, e.getMessage());
            return Collections.emptyList();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpstoxOptionResponse {
        private String status;
        private List<UpstoxOptionContract> data;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public List<UpstoxOptionContract> getData() {
            return data;
        }

        public void setData(List<UpstoxOptionContract> data) {
            this.data = data;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpstoxOptionContract {
        @JsonProperty("instrument_key")
        private String instrumentKey;

        @JsonProperty("exchange_token")
        private String exchangeToken;

        @JsonProperty("trading_symbol")
        private String tradingSymbol;

        @JsonProperty("expiry")
        private String expiry; // Format: 2024-02-15

        @JsonProperty("strike_price")
        private Double strikePrice;

        @JsonProperty("instrument_type")
        private String instrumentType; // CE/PE

        @JsonProperty("lot_size")
        private Integer lotSize;

        @JsonProperty("freeze_quantity")
        private Integer freezeQuantity;

        // Getters
        public String getInstrumentKey() {
            return instrumentKey;
        }

        public String getExchangeToken() {
            return exchangeToken;
        }

        public String getTradingSymbol() {
            return tradingSymbol;
        }

        public String getExpiry() {
            return expiry;
        }

        public Double getStrikePrice() {
            return strikePrice;
        }

        public String getInstrumentType() {
            return instrumentType;
        }

        public Integer getLotSize() {
            return lotSize;
        }

        public Integer getFreezeQuantity() {
            return freezeQuantity;
        }

        // Setters are handled by Jackson reflection or can be added if needed
        public void setInstrumentKey(String k) {
            this.instrumentKey = k;
        }

        public void setExchangeToken(String t) {
            this.exchangeToken = t;
        }

        public void setTradingSymbol(String s) {
            this.tradingSymbol = s;
        }

        public void setExpiry(String e) {
            this.expiry = e;
        }

        public void setStrikePrice(Double p) {
            this.strikePrice = p;
        }

        public void setInstrumentType(String t) {
            this.instrumentType = t;
        }

        public void setLotSize(Integer s) {
            this.lotSize = s;
        }

        public void setFreezeQuantity(Integer q) {
            this.freezeQuantity = q;
        }
    }
}
