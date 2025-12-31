package com.vegatrader.upstox.api.rms.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Client risk management service.
 * 
 * <p>
 * Integrates with OMS order flow for pre-trade validation.
 * 
 * @since 4.1.0
 */
@Service
public class ClientRiskService {

    private static final Logger logger = LoggerFactory.getLogger(ClientRiskService.class);

    private final ClientRiskLimitRepository limitRepo;
    private final ClientRiskStateRepository stateRepo;
    private final ClientRiskEvaluator evaluator;

    public ClientRiskService(
            ClientRiskLimitRepository limitRepo,
            ClientRiskStateRepository stateRepo,
            ClientRiskEvaluator evaluator) {
        this.limitRepo = limitRepo;
        this.stateRepo = stateRepo;
        this.evaluator = evaluator;
    }

    /**
     * Validates an order against client limits.
     */
    public void validateOrder(String clientId, double orderValue, boolean isNewPosition) {
        ClientRiskLimit limit = getLimit(clientId);
        ClientRiskState state = getState(clientId);

        double projectedGross = state.grossExposure() + Math.abs(orderValue);
        double projectedNet = state.netExposure() + orderValue;
        int projectedPositions = isNewPosition ? state.openPositions() + 1 : state.openPositions();

        evaluator.validate(limit, state, Math.abs(orderValue), projectedGross, projectedNet, projectedPositions);
        logger.debug("Order validated for client {}: value={}", clientId, orderValue);
    }

    /**
     * Gets client risk limits (or default).
     */
    public ClientRiskLimit getLimit(String clientId) {
        return limitRepo.findById(clientId)
                .map(ClientRiskLimitEntity::toRecord)
                .orElseGet(() -> {
                    // Try default limits
                    return limitRepo.findById("DEFAULT")
                            .map(ClientRiskLimitEntity::toRecord)
                            .orElse(ClientRiskLimit.defaultLimits(clientId));
                });
    }

    /**
     * Gets client risk state (or zero).
     */
    public ClientRiskState getState(String clientId) {
        return stateRepo.findById(clientId)
                .map(ClientRiskStateEntity::toRecord)
                .orElse(ClientRiskState.zero(clientId));
    }

    /**
     * Kill-switch: Disable client trading immediately.
     */
    @Transactional
    public void disableClient(String clientId) {
        limitRepo.disableClient(clientId);
        logger.warn("Client {} trading DISABLED", clientId);
    }

    /**
     * Enable client trading.
     */
    @Transactional
    public void enableClient(String clientId) {
        limitRepo.enableClient(clientId);
        logger.info("Client {} trading ENABLED", clientId);
    }

    /**
     * Emergency kill-switch: Disable all clients.
     */
    @Transactional
    public void disableAllClients() {
        int count = limitRepo.disableAllClients();
        logger.error("EMERGENCY: All {} clients DISABLED", count);
    }

    /**
     * Updates client MTM (called on tick/position update).
     */
    @Transactional
    public void updateMtm(String clientId, double mtm) {
        stateRepo.updateMtm(clientId, mtm);
    }

    /**
     * Resets client state (called at BOD).
     */
    @Transactional
    public void resetClientState(String clientId) {
        stateRepo.resetState(clientId);
        logger.info("Client {} state reset", clientId);
    }

    /**
     * Resets all client states (BOD job).
     */
    @Transactional
    public void resetAllClientStates() {
        int count = stateRepo.resetAllStates();
        logger.info("All {} client states reset", count);
    }

    /**
     * Check if client can trade.
     */
    public boolean canTrade(String clientId) {
        ClientRiskLimit limit = getLimit(clientId);
        ClientRiskState state = getState(clientId);
        return evaluator.canTrade(limit, state);
    }
}
