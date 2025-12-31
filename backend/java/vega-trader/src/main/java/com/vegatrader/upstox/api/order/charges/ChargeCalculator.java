package com.vegatrader.upstox.api.order.charges;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Charge Calculator for all brokerage and statutory charges.
 * Per order-mgmt/b2.md section 5.
 * 
 * Calculates:
 * - Brokerage
 * - STT (Securities Transaction Tax)
 * - Exchange fees
 * - GST
 * - SEBI charges
 * - Stamp duty
 * 
 * @since 4.9.0
 */
@Service
public class ChargeCalculator {

    private static final Logger logger = LoggerFactory.getLogger(ChargeCalculator.class);

    // Brokerage rates (configurable via AdminSettings)
    private static final BigDecimal BROKERAGE_RATE_EQUITY = new BigDecimal("0.0003"); // 0.03%
    private static final BigDecimal BROKERAGE_RATE_FO = new BigDecimal("0.0003");
    private static final BigDecimal MAX_BROKERAGE_PER_ORDER = new BigDecimal("20.00");

    // STT rates per SEBI
    private static final BigDecimal STT_EQUITY_DELIVERY_BUY = new BigDecimal("0.001"); // 0.1%
    private static final BigDecimal STT_EQUITY_DELIVERY_SELL = new BigDecimal("0.001");
    private static final BigDecimal STT_EQUITY_INTRADAY = new BigDecimal("0.00025"); // Sell only
    private static final BigDecimal STT_OPTIONS_SELL = new BigDecimal("0.0005"); // 0.05% on premium
    private static final BigDecimal STT_FUTURES = new BigDecimal("0.0001"); // Sell only

    // Exchange fees
    private static final BigDecimal NSE_TXN_CHARGE_EQ = new BigDecimal("0.0000325");
    private static final BigDecimal NSE_TXN_CHARGE_FO = new BigDecimal("0.00002");
    private static final BigDecimal BSE_TXN_CHARGE_EQ = new BigDecimal("0.000030");

    // GST (18% on brokerage + exchange fees)
    private static final BigDecimal GST_RATE = new BigDecimal("0.18");

    // SEBI charges (per crore)
    private static final BigDecimal SEBI_CHARGE_RATE = new BigDecimal("0.0000001"); // 10 per crore

    // Stamp duty (varies by state, using Maharashtra rates)
    private static final BigDecimal STAMP_DUTY_EQ = new BigDecimal("0.00015"); // 0.015%
    private static final BigDecimal STAMP_DUTY_FO = new BigDecimal("0.00003"); // 0.003%

    /**
     * Calculate charges for an order.
     */
    public ChargeBreakdown calculate(ChargeRequest request) {
        logger.debug("Calculating charges for: {} {} {} @ {}",
                request.side(), request.quantity(), request.instrument(), request.price());

        BigDecimal turnover = request.price().multiply(BigDecimal.valueOf(request.quantity()));

        // 1. Brokerage
        BigDecimal brokerage = calculateBrokerage(turnover, request.segment());

        // 2. STT
        BigDecimal stt = calculateSTT(turnover, request);

        // 3. Exchange fees
        BigDecimal exchangeFees = calculateExchangeFees(turnover, request.exchange(), request.segment());

        // 4. GST (on brokerage + exchange fees)
        BigDecimal taxableAmount = brokerage.add(exchangeFees);
        BigDecimal gst = taxableAmount.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);

        // 5. SEBI charges
        BigDecimal sebiCharges = turnover.multiply(SEBI_CHARGE_RATE).setScale(2, RoundingMode.HALF_UP);

        // 6. Stamp duty (only on buy)
        BigDecimal stampDuty = BigDecimal.ZERO;
        if ("BUY".equalsIgnoreCase(request.side())) {
            stampDuty = calculateStampDuty(turnover, request.segment());
        }

        // Total
        BigDecimal totalCharges = brokerage.add(stt).add(exchangeFees)
                .add(gst).add(sebiCharges).add(stampDuty);

        ChargeBreakdown breakdown = new ChargeBreakdown(
                brokerage.setScale(2, RoundingMode.HALF_UP),
                stt.setScale(2, RoundingMode.HALF_UP),
                exchangeFees.setScale(2, RoundingMode.HALF_UP),
                gst,
                sebiCharges,
                stampDuty.setScale(2, RoundingMode.HALF_UP),
                totalCharges.setScale(2, RoundingMode.HALF_UP),
                turnover.setScale(2, RoundingMode.HALF_UP));

        logger.debug("Charges calculated: {}", breakdown);
        return breakdown;
    }

    private BigDecimal calculateBrokerage(BigDecimal turnover, String segment) {
        BigDecimal rate = segment.contains("FO") ? BROKERAGE_RATE_FO : BROKERAGE_RATE_EQUITY;
        BigDecimal brokerage = turnover.multiply(rate);
        return brokerage.min(MAX_BROKERAGE_PER_ORDER);
    }

    private BigDecimal calculateSTT(BigDecimal turnover, ChargeRequest request) {
        String segment = request.segment();
        String side = request.side();
        String product = request.product();

        if (segment.contains("FO")) {
            // Options
            if (segment.contains("OPT") || request.instrument().contains("CE") ||
                    request.instrument().contains("PE")) {
                return "SELL".equalsIgnoreCase(side) ? turnover.multiply(STT_OPTIONS_SELL) : BigDecimal.ZERO;
            }
            // Futures
            return "SELL".equalsIgnoreCase(side) ? turnover.multiply(STT_FUTURES) : BigDecimal.ZERO;
        }

        // Equity
        if ("I".equals(product)) {
            // Intraday - STT only on sell
            return "SELL".equalsIgnoreCase(side) ? turnover.multiply(STT_EQUITY_INTRADAY) : BigDecimal.ZERO;
        } else {
            // Delivery - STT on both buy and sell
            return "BUY".equalsIgnoreCase(side) ? turnover.multiply(STT_EQUITY_DELIVERY_BUY)
                    : turnover.multiply(STT_EQUITY_DELIVERY_SELL);
        }
    }

    private BigDecimal calculateExchangeFees(BigDecimal turnover, String exchange, String segment) {
        BigDecimal rate;
        if (exchange.startsWith("NSE")) {
            rate = segment.contains("FO") ? NSE_TXN_CHARGE_FO : NSE_TXN_CHARGE_EQ;
        } else if (exchange.startsWith("BSE")) {
            rate = BSE_TXN_CHARGE_EQ;
        } else {
            rate = NSE_TXN_CHARGE_EQ; // Default
        }
        return turnover.multiply(rate);
    }

    private BigDecimal calculateStampDuty(BigDecimal turnover, String segment) {
        BigDecimal rate = segment.contains("FO") ? STAMP_DUTY_FO : STAMP_DUTY_EQ;
        return turnover.multiply(rate);
    }

    /**
     * Charge calculation request.
     */
    public record ChargeRequest(
            String instrument,
            String exchange,
            String segment,
            String side,
            String product,
            int quantity,
            BigDecimal price) {
    }
}
