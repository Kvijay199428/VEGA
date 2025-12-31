package com.vegatrader.upstox.api.order.pnl;

import com.vegatrader.upstox.api.order.entity.TradeEntity;
import com.vegatrader.upstox.api.order.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * P&L Engine for trade analytics.
 * Per order-mgmt/b2.md section 5.
 * 
 * Calculates:
 * - Realized P&L (on trade)
 * - Unrealized P&L (mark-to-market)
 * - Net P&L (post charges)
 * 
 * @since 4.9.0
 */
@Service
public class PnLService {

    private static final Logger logger = LoggerFactory.getLogger(PnLService.class);

    private final TradeRepository tradeRepository;

    public PnLService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    /**
     * Calculate realized P&L for a trade.
     */
    public PnLResult calculateRealizedPnL(TradeEntity trade, BigDecimal costBasis) {
        BigDecimal tradeValue = trade.getPrice().multiply(BigDecimal.valueOf(trade.getQuantity()));

        BigDecimal grossPnL;
        if ("SELL".equalsIgnoreCase(trade.getTransactionType())) {
            // SELL: profit = sell value - cost basis
            grossPnL = tradeValue.subtract(costBasis);
        } else {
            // BUY: no realized P&L, just updates cost basis
            grossPnL = BigDecimal.ZERO;
        }

        BigDecimal charges = trade.getTotalCharges() != null ? trade.getTotalCharges() : BigDecimal.ZERO;
        BigDecimal netPnL = grossPnL.subtract(charges);

        logger.info("Realized PnL computed for trade {}: Gross={}, Net={}",
                trade.getTradeId(), grossPnL, netPnL);

        return new PnLResult(
                trade.getTradeId(),
                trade.getOrderId(),
                trade.getTradingSymbol(),
                grossPnL.setScale(2, RoundingMode.HALF_UP),
                charges.setScale(2, RoundingMode.HALF_UP),
                netPnL.setScale(2, RoundingMode.HALF_UP),
                "REALIZED");
    }

    /**
     * Calculate unrealized P&L (mark-to-market).
     */
    public PnLResult calculateUnrealizedPnL(
            String symbol,
            int quantity,
            BigDecimal avgBuyPrice,
            BigDecimal currentPrice) {

        BigDecimal costBasis = avgBuyPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal currentValue = currentPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal grossPnL = currentValue.subtract(costBasis);

        logger.debug("Unrealized PnL for {}: {}", symbol, grossPnL);

        // No charges for unrealized (yet)
        return new PnLResult(
                null,
                null,
                symbol,
                grossPnL.setScale(2, RoundingMode.HALF_UP),
                BigDecimal.ZERO,
                grossPnL.setScale(2, RoundingMode.HALF_UP),
                "UNREALIZED");
    }

    /**
     * Calculate daily P&L for user.
     */
    public DailyPnLSummary calculateDailyPnL(String userId, LocalDate date) {
        logger.info("Calculating daily PnL for user {} on {}", userId, date);
        List<TradeEntity> trades = tradeRepository.findByUserIdAndTradeDate(userId, date);

        BigDecimal totalGrossPnL = BigDecimal.ZERO;
        BigDecimal totalCharges = BigDecimal.ZERO;
        int tradeCount = 0;
        int winningTrades = 0;
        int losingTrades = 0;

        // Group by symbol to calculate P&L
        Map<String, List<TradeEntity>> bySymbol = new HashMap<>();
        for (TradeEntity trade : trades) {
            bySymbol.computeIfAbsent(trade.getTradingSymbol(), k -> new ArrayList<>()).add(trade);
            tradeCount++;

            BigDecimal charges = trade.getTotalCharges() != null ? trade.getTotalCharges() : BigDecimal.ZERO;
            totalCharges = totalCharges.add(charges);
        }

        // Calculate P&L per symbol
        List<SymbolPnL> symbolPnLs = new ArrayList<>();
        for (Map.Entry<String, List<TradeEntity>> entry : bySymbol.entrySet()) {
            SymbolPnL symbolPnL = calculateSymbolPnL(entry.getKey(), entry.getValue());
            symbolPnLs.add(symbolPnL);
            totalGrossPnL = totalGrossPnL.add(symbolPnL.grossPnL());

            if (symbolPnL.netPnL().compareTo(BigDecimal.ZERO) > 0) {
                winningTrades++;
            } else if (symbolPnL.netPnL().compareTo(BigDecimal.ZERO) < 0) {
                losingTrades++;
            }
        }

        BigDecimal netPnL = totalGrossPnL.subtract(totalCharges);
        double winRate = tradeCount > 0 ? (double) winningTrades / tradeCount * 100 : 0;

        return new DailyPnLSummary(
                userId,
                date,
                totalGrossPnL.setScale(2, RoundingMode.HALF_UP),
                totalCharges.setScale(2, RoundingMode.HALF_UP),
                netPnL.setScale(2, RoundingMode.HALF_UP),
                tradeCount,
                winningTrades,
                losingTrades,
                winRate,
                symbolPnLs);
    }

    /**
     * Calculate P&L for a symbol's trades.
     */
    private SymbolPnL calculateSymbolPnL(String symbol, List<TradeEntity> trades) {
        // Sort by timestamp
        trades.sort(Comparator.comparing(TradeEntity::getCreatedAt));

        BigDecimal buyValue = BigDecimal.ZERO;
        int buyQty = 0;
        BigDecimal sellValue = BigDecimal.ZERO;
        int sellQty = 0;
        BigDecimal charges = BigDecimal.ZERO;

        for (TradeEntity trade : trades) {
            BigDecimal tradeValue = trade.getPrice().multiply(BigDecimal.valueOf(trade.getQuantity()));
            charges = charges.add(trade.getTotalCharges() != null ? trade.getTotalCharges() : BigDecimal.ZERO);

            if ("BUY".equalsIgnoreCase(trade.getTransactionType())) {
                buyValue = buyValue.add(tradeValue);
                buyQty += trade.getQuantity();
            } else {
                sellValue = sellValue.add(tradeValue);
                sellQty += trade.getQuantity();
            }
        }

        // P&L = Sell value - Buy value
        BigDecimal grossPnL = sellValue.subtract(buyValue);
        BigDecimal netPnL = grossPnL.subtract(charges);

        return new SymbolPnL(
                symbol,
                buyQty,
                sellQty,
                buyValue.setScale(2, RoundingMode.HALF_UP),
                sellValue.setScale(2, RoundingMode.HALF_UP),
                grossPnL.setScale(2, RoundingMode.HALF_UP),
                charges.setScale(2, RoundingMode.HALF_UP),
                netPnL.setScale(2, RoundingMode.HALF_UP));
    }

    // ==================== Result Records ====================

    public record PnLResult(
            String tradeId,
            String orderId,
            String symbol,
            BigDecimal grossPnL,
            BigDecimal charges,
            BigDecimal netPnL,
            String type // REALIZED, UNREALIZED
    ) {
    }

    public record DailyPnLSummary(
            String userId,
            LocalDate date,
            BigDecimal totalGrossPnL,
            BigDecimal totalCharges,
            BigDecimal totalNetPnL,
            int tradeCount,
            int winningTrades,
            int losingTrades,
            double winRate,
            List<SymbolPnL> bySymbol) {
    }

    public record SymbolPnL(
            String symbol,
            int buyQuantity,
            int sellQuantity,
            BigDecimal buyValue,
            BigDecimal sellValue,
            BigDecimal grossPnL,
            BigDecimal charges,
            BigDecimal netPnL) {
    }
}
