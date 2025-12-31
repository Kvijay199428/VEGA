package com.vegatrader.upstox.api.order.slicing;

import com.vegatrader.upstox.api.order.broker.BrokerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Auto Slicing Service for large order handling.
 * Per order-mgmt/a2.md section on auto slicing.
 * 
 * Splits orders exceeding exchange freeze quantity into smaller chunks.
 * Correlation IDs get suffixes: correlation_id_1, correlation_id_2, etc.
 * 
 * @since 4.9.0
 */
@Service
public class AutoSlicingService {

    private static final Logger logger = LoggerFactory.getLogger(AutoSlicingService.class);

    // Exchange freeze quantities (per SEBI)
    private static final Map<String, Integer> FREEZE_QUANTITIES = Map.of(
            "NSE_EQ", 50000,
            "BSE_EQ", 50000,
            "NSE_FO_NIFTY", 1800,
            "NSE_FO_BANKNIFTY", 900,
            "NSE_FO_FINNIFTY", 1800,
            "NSE_FO_DEFAULT", 1800,
            "MCX", 500,
            "CDS", 10000);

    /**
     * Check if order needs slicing.
     */
    public boolean needsSlicing(BrokerAdapter.OrderRequest order, String segment) {
        int freezeQty = getFreezeQuantity(segment, order.instrumentToken());
        return order.quantity() > freezeQty;
    }

    /**
     * Slice order into smaller chunks.
     */
    public List<BrokerAdapter.OrderRequest> sliceOrder(
            BrokerAdapter.OrderRequest order,
            String segment) {

        int freezeQty = getFreezeQuantity(segment, order.instrumentToken());

        if (order.quantity() <= freezeQty) {
            return List.of(order); // No slicing needed
        }

        List<BrokerAdapter.OrderRequest> slices = new ArrayList<>();
        int remaining = order.quantity();
        int sliceIndex = 1;

        while (remaining > 0) {
            int sliceQty = Math.min(remaining, freezeQty);
            String sliceCorrelationId = order.correlationId() + "_" + sliceIndex;

            slices.add(new BrokerAdapter.OrderRequest(
                    sliceCorrelationId,
                    order.instrumentToken(),
                    order.side(),
                    order.orderType(),
                    order.product(),
                    sliceQty,
                    order.price(),
                    order.triggerPrice(),
                    order.validity(),
                    Math.min(order.disclosedQuantity(), sliceQty),
                    order.tag(),
                    order.isAmo(),
                    false // Slices don't need to be sliced again
            ));

            remaining -= sliceQty;
            sliceIndex++;
        }

        logger.info("Order {} sliced into {} parts (original qty: {}, freeze: {})",
                order.correlationId(), slices.size(), order.quantity(), freezeQty);

        return slices;
    }

    /**
     * Slice multiple orders.
     */
    public SliceResult sliceOrders(List<BrokerAdapter.OrderRequest> orders, String segment) {
        List<BrokerAdapter.OrderRequest> allSlices = new ArrayList<>();
        Map<String, List<String>> sliceMap = new HashMap<>(); // original -> slice IDs

        for (BrokerAdapter.OrderRequest order : orders) {
            if (needsSlicing(order, segment)) {
                List<BrokerAdapter.OrderRequest> slices = sliceOrder(order, segment);
                allSlices.addAll(slices);
                sliceMap.put(order.correlationId(),
                        slices.stream().map(BrokerAdapter.OrderRequest::correlationId).toList());
            } else {
                allSlices.add(order);
                sliceMap.put(order.correlationId(), List.of(order.correlationId()));
            }
        }

        int totalSlices = allSlices.size();
        int slicedOrders = (int) orders.stream()
                .filter(o -> needsSlicing(o, segment)).count();

        return new SliceResult(
                allSlices,
                sliceMap,
                orders.size(),
                slicedOrders,
                totalSlices);
    }

    /**
     * Get freeze quantity for segment/instrument.
     */
    public int getFreezeQuantity(String segment, String instrumentToken) {
        // Check for specific instrument overrides
        if (segment.contains("FO")) {
            if (instrumentToken.contains("NIFTY") && !instrumentToken.contains("BANKNIFTY")
                    && !instrumentToken.contains("FINNIFTY")) {
                return FREEZE_QUANTITIES.get("NSE_FO_NIFTY");
            }
            if (instrumentToken.contains("BANKNIFTY")) {
                return FREEZE_QUANTITIES.get("NSE_FO_BANKNIFTY");
            }
            if (instrumentToken.contains("FINNIFTY")) {
                return FREEZE_QUANTITIES.get("NSE_FO_FINNIFTY");
            }
            return FREEZE_QUANTITIES.get("NSE_FO_DEFAULT");
        }

        return FREEZE_QUANTITIES.getOrDefault(segment, 50000);
    }

    /**
     * Rebuild original order results from slices.
     */
    public Map<String, AggregatedResult> aggregateSliceResults(
            Map<String, List<String>> sliceMap,
            List<BrokerAdapter.OrderResult> sliceResults) {

        Map<String, BrokerAdapter.OrderResult> resultsByCorrelation = new HashMap<>();
        for (BrokerAdapter.OrderResult result : sliceResults) {
            resultsByCorrelation.put(result.correlationId(), result);
        }

        Map<String, AggregatedResult> aggregated = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry : sliceMap.entrySet()) {
            String originalId = entry.getKey();
            List<String> sliceIds = entry.getValue();

            int successCount = 0;
            int errorCount = 0;
            List<String> successOrderIds = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            long totalLatency = 0;

            for (String sliceId : sliceIds) {
                BrokerAdapter.OrderResult result = resultsByCorrelation.get(sliceId);
                if (result != null) {
                    if (result.success()) {
                        successCount++;
                        successOrderIds.add(result.orderId());
                    } else {
                        errorCount++;
                        errors.add(result.errorMessage());
                    }
                    totalLatency += result.latencyMs();
                }
            }

            String status = errorCount == 0 ? "success" : (successCount == 0 ? "error" : "partial_success");

            aggregated.put(originalId, new AggregatedResult(
                    originalId,
                    status,
                    sliceIds.size(),
                    successCount,
                    errorCount,
                    successOrderIds,
                    errors,
                    totalLatency));
        }

        return aggregated;
    }

    // ==================== Records ====================

    public record SliceResult(
            List<BrokerAdapter.OrderRequest> slicedOrders,
            Map<String, List<String>> sliceMap,
            int originalOrderCount,
            int ordersSliced,
            int totalSlices) {
    }

    public record AggregatedResult(
            String originalCorrelationId,
            String status,
            int sliceCount,
            int successCount,
            int errorCount,
            List<String> successOrderIds,
            List<String> errors,
            long totalLatencyMs) {
    }
}
