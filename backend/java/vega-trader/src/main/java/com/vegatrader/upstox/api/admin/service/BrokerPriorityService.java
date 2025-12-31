package com.vegatrader.upstox.api.admin.service;

import com.vegatrader.upstox.api.admin.entity.BrokerRegistryEntity;
import com.vegatrader.upstox.api.admin.model.BrokerPriorityRequest;
import com.vegatrader.upstox.api.admin.repository.BrokerRegistryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Broker Priority Service.
 * Manages broker routing priority.
 * Per a1.md Section 9.
 * 
 * @since 5.0.0
 */
@Service
public class BrokerPriorityService {

    private static final Logger logger = LoggerFactory.getLogger(BrokerPriorityService.class);

    private final BrokerRegistryRepository brokerRepository;

    @Autowired
    public BrokerPriorityService(BrokerRegistryRepository brokerRepository) {
        this.brokerRepository = brokerRepository;
    }

    /**
     * Update broker priority.
     */
    @Transactional
    public boolean updatePriority(BrokerPriorityRequest request, String adminUser) {
        logger.info("Updating broker priority for {} / {}: {}",
                request.instrumentType(), request.exchange(), request.priority());

        // Update each broker in priority order
        List<String> priorities = request.priority();
        for (int i = 0; i < priorities.size(); i++) {
            String brokerCode = priorities.get(i);
            int updatedRows = brokerRepository.updatePriority(brokerCode, i + 1, adminUser);
            if (updatedRows == 0) {
                logger.warn("Broker not found: {}", brokerCode);
            } else {
                logger.info("Updated {} priority to {}", brokerCode, i + 1);
            }
        }
        return true;
    }

    /**
     * Get all active brokers.
     */
    public List<BrokerRegistryEntity> getAllActiveBrokers() {
        return brokerRepository.findByIsActiveTrueOrderByPriorityAsc();
    }

    /**
     * Get brokers by exchange.
     */
    public List<BrokerRegistryEntity> getBrokersByExchange(String exchange) {
        return brokerRepository.findByExchangeAndIsActiveTrueOrderByPriorityAsc(exchange);
    }

    /**
     * Get brokers by exchange and instrument type.
     */
    public List<BrokerRegistryEntity> getBrokersByExchangeAndType(String exchange, String instrumentType) {
        return brokerRepository.findByExchangeAndInstrumentTypeAndIsActiveTrueOrderByPriorityAsc(
                exchange, instrumentType);
    }

    /**
     * Get top priority broker for exchange.
     */
    public Optional<BrokerRegistryEntity> getTopBroker(String exchange) {
        return brokerRepository.findTopBrokerByExchange(exchange);
    }

    /**
     * Get broker by code.
     */
    public Optional<BrokerRegistryEntity> getBroker(String brokerCode) {
        return brokerRepository.findByBrokerCode(brokerCode);
    }

    /**
     * Activate a broker.
     */
    @Transactional
    public boolean activateBroker(String brokerCode, String adminUser) {
        logger.info("Activating broker: {}", brokerCode);
        int updated = brokerRepository.updateActiveStatus(brokerCode, true, adminUser);
        return updated > 0;
    }

    /**
     * Deactivate a broker.
     */
    @Transactional
    public boolean deactivateBroker(String brokerCode, String adminUser) {
        logger.info("Deactivating broker: {}", brokerCode);
        int updated = brokerRepository.updateActiveStatus(brokerCode, false, adminUser);
        return updated > 0;
    }

    /**
     * Register a new broker.
     */
    @Transactional
    public BrokerRegistryEntity registerBroker(String brokerCode, String brokerName,
            String exchange, int priority) {
        logger.info("Registering broker: {} for {}", brokerCode, exchange);

        if (brokerRepository.findByBrokerCode(brokerCode).isPresent()) {
            throw new IllegalArgumentException("Broker already exists: " + brokerCode);
        }

        BrokerRegistryEntity entity = BrokerRegistryEntity.create(brokerCode, brokerName, exchange, priority);
        return brokerRepository.save(entity);
    }

    /**
     * Count active brokers.
     */
    public long countActiveBrokers() {
        return brokerRepository.countByIsActiveTrue();
    }
}
