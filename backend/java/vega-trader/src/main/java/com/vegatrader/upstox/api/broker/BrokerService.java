package com.vegatrader.upstox.api.broker;

import com.vegatrader.upstox.api.broker.adapter.BrokerAdapter;
import com.vegatrader.upstox.api.broker.adapter.UpstoxBrokerAdapter;
import com.vegatrader.upstox.api.broker.engine.MultiBrokerEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * Broker service for managing broker connections.
 * 
 * @since 4.2.0
 */
@Service
public class BrokerService {

    private static final Logger logger = LoggerFactory.getLogger(BrokerService.class);

    private final BrokerRepository brokerRepo;
    private final MultiBrokerEngine engine;
    private final UpstoxBrokerAdapter upstoxAdapter;

    public BrokerService(
            BrokerRepository brokerRepo,
            MultiBrokerEngine engine,
            UpstoxBrokerAdapter upstoxAdapter) {
        this.brokerRepo = brokerRepo;
        this.engine = engine;
        this.upstoxAdapter = upstoxAdapter;
    }

    @PostConstruct
    public void init() {
        // Register enabled brokers
        List<BrokerEntity> enabledBrokers = brokerRepo.findAllEnabled();

        for (BrokerEntity broker : enabledBrokers) {
            switch (broker.getBrokerId()) {
                case "UPSTOX" -> engine.registerAdapter("UPSTOX", upstoxAdapter);
                // Add other broker adapters as needed
            }
        }

        logger.info("Registered {} broker adapters", engine.getAvailableBrokers().size());
    }

    /**
     * Gets all enabled brokers.
     */
    public List<Broker> getEnabledBrokers() {
        return brokerRepo.findAllEnabled().stream()
                .map(BrokerEntity::toRecord)
                .toList();
    }

    /**
     * Check if broker is available.
     */
    public boolean isBrokerAvailable(String brokerId) {
        return engine.isBrokerConnected(brokerId);
    }

    /**
     * Gets the multi-broker engine.
     */
    public MultiBrokerEngine getEngine() {
        return engine;
    }
}
