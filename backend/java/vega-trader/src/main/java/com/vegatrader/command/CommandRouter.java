package com.vegatrader.command;

import com.vegatrader.domain.enums.OrderType;
import com.vegatrader.domain.enums.ProductType;
import com.vegatrader.domain.enums.TransactionType;
import com.vegatrader.execution.ExecutionGateway;
import com.vegatrader.execution.dto.OrderRequest;
import com.vegatrader.execution.dto.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command Router.
 * Parses text commands and routes them to appropriate services.
 * Syntax: BUY <SYMBOL> <QTY> [PRICE]
 */
@Service
public class CommandRouter {

    private static final Logger logger = LoggerFactory.getLogger(CommandRouter.class);

    @Autowired
    private ExecutionGateway executionGateway;

    // BUY RELIANCE 100 2500.50
    // SELL INFY 50
    private static final Pattern ORDER_PATTERN = Pattern
            .compile("^(BUY|SELL)\\s+([A-Z0-9_]+)\\s+(\\d+)(?:\\s+([\\d.]+))?$", Pattern.CASE_INSENSITIVE);

    /**
     * Execute a text command.
     */
    public Map<String, Object> execute(String command) {
        logger.info("Processing command: {}", command);
        Map<String, Object> result = new HashMap<>();

        Matcher matcher = ORDER_PATTERN.matcher(command.trim());
        if (matcher.matches()) {
            return handleOrderCommand(matcher);
        }

        result.put("status", "ERROR");
        result.put("message", "Unknown command syntax");
        return result;
    }

    private Map<String, Object> handleOrderCommand(Matcher matcher) {
        try {
            String side = matcher.group(1).toUpperCase();
            String symbol = matcher.group(2).toUpperCase(); // This needs resolution to InstrumentKey!
            int qty = Integer.parseInt(matcher.group(3));
            Double price = matcher.group(4) != null ? Double.parseDouble(matcher.group(4)) : null;

            // Resolve Symbol -> InstrumentKey (Mock for now, normally use
            // InstrumentService)
            // Assumption: User types "RELIANCE", we need "NSE_EQ|RELIANCE"
            String instrumentKey = resolveInstrumentKey(symbol);

            OrderRequest request = OrderRequest.builder()
                    .instrumentKey(instrumentKey)
                    .transactionType(TransactionType.valueOf(side))
                    .orderType(price != null ? OrderType.LIMIT : OrderType.MARKET)
                    .productType(ProductType.INTRA) // Default
                    .quantity(qty)
                    .price(price != null ? price : 0.0)
                    .tag("CMD_LINE")
                    .build();

            OrderResponse response = executionGateway.placeOrder(request);

            Map<String, Object> result = new HashMap<>();
            result.put("status", response.getStatus());
            result.put("message", response.getMessage());
            result.put("orderId", response.getOrderId());
            return result;

        } catch (Exception e) {
            logger.error("Command execution failed", e);
            return Map.of("status", "ERROR", "message", e.getMessage());
        }
    }

    private String resolveInstrumentKey(String symbol) {
        // TODO: Real lookup
        if (!symbol.contains("|")) {
            return "NSE_EQ|" + symbol;
        }
        return symbol;
    }
}
