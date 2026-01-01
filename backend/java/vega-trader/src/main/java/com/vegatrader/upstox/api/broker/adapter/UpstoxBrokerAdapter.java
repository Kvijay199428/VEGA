package com.vegatrader.upstox.api.broker.adapter;

import com.vegatrader.upstox.api.broker.BrokerSymbolMappingEntity;
import com.vegatrader.upstox.api.broker.model.*;
import com.vegatrader.upstox.api.broker.BrokerSymbolMappingRepository;
import com.vegatrader.upstox.api.broker.service.BrokerInstrumentResolver;
import com.vegatrader.upstox.api.broker.service.MultiBrokerResolver;
import com.vegatrader.upstox.api.broker.service.MultiBrokerResolver.BrokerInstrument;
import com.vegatrader.upstox.api.broker.service.UpstoxOptionContractService;
import com.vegatrader.upstox.api.broker.service.UpstoxOptionContractService.UpstoxOptionContract;
import com.vegatrader.upstox.auth.config.AuthConstants;
import com.vegatrader.upstox.auth.entity.UpstoxTokenEntity;
import com.vegatrader.upstox.auth.service.TokenStorageService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Upstox broker adapter implementation.
 * 
 * <p>
 * Integrates with Upstox REST and WebSocket APIs.
 * Implements instrument resolution via /option/contract API.
 * 
 * @since 4.2.0
 */
@Component("upstoxBrokerAdapterStub")
public class UpstoxBrokerAdapter implements BrokerAdapter, BrokerInstrumentResolver {

    private static final Logger logger = LoggerFactory.getLogger(UpstoxBrokerAdapter.class);
    private static final String BROKER_ID = "UPSTOX";

    @Autowired
    private UpstoxOptionContractService contractService;

    @Autowired
    private BrokerSymbolMappingRepository mappingRepo;

    @Autowired
    private TokenStorageService tokenStorageService;

    @Autowired
    private MultiBrokerResolver multiBrokerResolver;

    private volatile boolean connected = false;

    @PostConstruct
    public void init() {
        // Self-register
        multiBrokerResolver.registerResolver(BROKER_ID, this);
    }

    // --- BrokerInstrumentResolver Implementation ---

    @Override
    public BrokerInstrument resolveOption(String underlyingKey, LocalDate expiry, BigDecimal strike,
            String optionType) {
        // 1. Construct cache key: Underlying|Expiry|Strike|Type
        // Example: NSE_INDEX|Nifty 50|2024-02-15|21000|CE
        String cacheKey = String.format("%s|%s|%s|%s",
                underlyingKey, expiry, strike.stripTrailingZeros().toPlainString(), optionType);

        // 2. Check Cache
        Optional<BrokerSymbolMappingEntity> cached = mappingRepo.findByBrokerAndInstrument(BROKER_ID, cacheKey);
        if (cached.isPresent()) {
            logger.debug("Cache hit for {}", cacheKey);
            return new BrokerInstrument(
                    cached.get().getBrokerSymbol(),
                    cached.get().getBrokerToken(),
                    cached.get().getBrokerSymbol(),
                    1,
                    BigDecimal.ZERO,
                    0,
                    false);
        }

        // 3. Fetch from API if not cached
        logger.info("Cache miss for {}, fetching from Upstox API", cacheKey);

        Optional<String> tokenOpt = tokenStorageService.getToken(AuthConstants.TOKEN_CATEGORY_PRIMARY)
                .map(UpstoxTokenEntity::getAccessToken);

        if (tokenOpt.isEmpty()) {
            throw new IllegalStateException("No valid PRIMARY token available for resolution");
        }

        List<UpstoxOptionContract> contracts = contractService.fetchOptionContracts(underlyingKey, tokenOpt.get());

        // 4. Filter
        UpstoxOptionContract match = contracts.stream()
                .filter(c -> isMatch(c, expiry, strike, optionType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No contract found for " + cacheKey));

        // 5. Store in Cache
        BrokerSymbolMappingEntity entity = new BrokerSymbolMappingEntity();
        entity.setBrokerId(BROKER_ID);
        entity.setInstrumentKey(cacheKey);
        entity.setBrokerSymbol(match.getInstrumentKey());
        entity.setBrokerToken(match.getExchangeToken());
        entity.setTradeable(true);
        mappingRepo.save(entity);

        return new BrokerInstrument(
                match.getInstrumentKey(),
                match.getExchangeToken(),
                match.getTradingSymbol(),
                match.getLotSize(),
                BigDecimal.ZERO,
                match.getFreezeQuantity(),
                false);
    }

    private boolean isMatch(UpstoxOptionContract c, LocalDate expiry, BigDecimal strike, String type) {
        if (!c.getExpiry().equals(expiry.toString()))
            return false;
        if (!c.getInstrumentType().equalsIgnoreCase(type))
            return false;
        return BigDecimal.valueOf(c.getStrikePrice()).compareTo(strike) == 0;
    }

    @Override
    public String getBrokerCode() {
        return BROKER_ID;
    }

    @Override
    public boolean isHealthy() {
        return connected;
    }

    // --- BrokerAdapter Implementation (Stubs) ---

    @Override
    public String getBrokerId() {
        return BROKER_ID;
    }

    @Override
    public BrokerOrderResponse placeOrder(OrderRequest request) {
        logger.info("Placing order on Upstox: {} {} qty={} price={}",
                request.transactionType(), request.brokerSymbol(), request.qty(), request.price());
        return BrokerOrderResponse.success("ORD_" + System.currentTimeMillis(), "UPSTOX_" + System.currentTimeMillis());
    }

    @Override
    public BrokerOrderResponse modifyOrder(String orderId, OrderRequest request) {
        return BrokerOrderResponse.success(orderId, orderId);
    }

    @Override
    public void cancelOrder(String orderId) {
        logger.info("Cancelling order {} on Upstox", orderId);
    }

    @Override
    public BrokerOrderStatus getOrderStatus(String orderId) {
        return new BrokerOrderStatus(
                orderId, orderId, "", "PENDING", "", "", "", 0, 0, 0, 0, 0, null, LocalDateTime.now());
    }

    @Override
    public List<BrokerOrderStatus> getOrders() {
        return new ArrayList<>();
    }

    @Override
    public List<Position> getPositions() {
        return new ArrayList<>();
    }

    @Override
    public List<Holding> getHoldings() {
        return new ArrayList<>();
    }

    @Override
    public void subscribeMarketData(List<String> instrumentKeys) {
        logger.info("Subscribing to market data: {}", instrumentKeys);
    }

    @Override
    public void unsubscribeMarketData(List<String> instrumentKeys) {
        logger.info("Unsubscribing from market data: {}", instrumentKeys);
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void reconnect() {
        connected = true;
    }
}
