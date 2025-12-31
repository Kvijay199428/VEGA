package com.vegatrader.upstox.api.order.settlement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

/**
 * Settlement Service for T+0/T+1 tracking.
 * Per order-mgmt/b2.md section 5.
 * 
 * Settlement cycles:
 * - Equity: T+1 (from Feb 2025)
 * - FO: T+0 (same day)
 * - Commodity: T+1
 * 
 * @since 4.9.0
 */
@Service
public class SettlementService {

    private static final Logger logger = LoggerFactory.getLogger(SettlementService.class);

    // Indian market holidays (sample - should be loaded from DB)
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.of(2025, 1, 26), // Republic Day
            LocalDate.of(2025, 3, 14), // Holi
            LocalDate.of(2025, 4, 10), // Mahavir Jayanti
            LocalDate.of(2025, 4, 14), // Dr. Ambedkar Jayanti
            LocalDate.of(2025, 4, 18), // Good Friday
            LocalDate.of(2025, 5, 1), // May Day
            LocalDate.of(2025, 8, 15), // Independence Day
            LocalDate.of(2025, 10, 2), // Gandhi Jayanti
            LocalDate.of(2025, 10, 21), // Diwali Laxmi Pujan
            LocalDate.of(2025, 11, 5), // Diwali Balipratipada
            LocalDate.of(2025, 12, 25) // Christmas
    );

    /**
     * Get settlement date for a trade.
     */
    public LocalDate getSettlementDate(String segment, LocalDate tradeDate) {
        int settlementDays = getSettlementCycle(segment);

        LocalDate settlementDate = tradeDate;
        int businessDaysAdded = 0;

        while (businessDaysAdded < settlementDays) {
            settlementDate = settlementDate.plusDays(1);
            if (isBusinessDay(settlementDate)) {
                businessDaysAdded++;
            }
        }

        logger.debug("Settlement for {} trade on {}: {} (T+{})",
                segment, tradeDate, settlementDate, settlementDays);

        return settlementDate;
    }

    /**
     * Get settlement cycle for segment.
     */
    public int getSettlementCycle(String segment) {
        return switch (segment.toUpperCase()) {
            case "NSE_EQ", "BSE_EQ", "EQ" -> 1; // T+1 for equity
            case "NSE_FO", "BSE_FO", "FO" -> 0; // T+0 for F&O
            case "MCX", "CDS", "COM" -> 1; // T+1 for commodity
            case "MF" -> 2; // T+2 for mutual funds
            default -> 1;
        };
    }

    /**
     * Check if date is a business day.
     */
    public boolean isBusinessDay(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY &&
                dow != DayOfWeek.SUNDAY &&
                !HOLIDAYS.contains(date);
    }

    /**
     * Get next business day.
     */
    public LocalDate getNextBusinessDay(LocalDate date) {
        LocalDate next = date.plusDays(1);
        while (!isBusinessDay(next)) {
            next = next.plusDays(1);
        }
        return next;
    }

    /**
     * Get previous business day.
     */
    public LocalDate getPreviousBusinessDay(LocalDate date) {
        LocalDate prev = date.minusDays(1);
        while (!isBusinessDay(prev)) {
            prev = prev.minusDays(1);
        }
        return prev;
    }

    /**
     * Calculate pending settlements for user.
     */
    public List<PendingSettlement> getPendingSettlements(
            List<TradeSettlement> trades,
            LocalDate asOfDate) {

        List<PendingSettlement> pending = new ArrayList<>();

        for (TradeSettlement trade : trades) {
            LocalDate settlementDate = getSettlementDate(trade.segment(), trade.tradeDate());

            if (settlementDate.isAfter(asOfDate)) {
                pending.add(new PendingSettlement(
                        trade.tradeId(),
                        trade.symbol(),
                        trade.segment(),
                        trade.tradeDate(),
                        settlementDate,
                        trade.quantity(),
                        trade.value(),
                        calculateDaysToSettlement(asOfDate, settlementDate)));
            }
        }

        return pending;
    }

    /**
     * Calculate business days between dates.
     */
    public int calculateDaysToSettlement(LocalDate from, LocalDate to) {
        if (from.isAfter(to))
            return 0;

        int days = 0;
        LocalDate current = from;
        while (current.isBefore(to)) {
            current = current.plusDays(1);
            if (isBusinessDay(current)) {
                days++;
            }
        }
        return days;
    }

    /**
     * Get settlement calendar for month.
     */
    public List<SettlementDay> getSettlementCalendar(int year, int month) {
        List<SettlementDay> calendar = new ArrayList<>();
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1);

        LocalDate current = start;
        while (current.isBefore(end)) {
            calendar.add(new SettlementDay(
                    current,
                    isBusinessDay(current),
                    HOLIDAYS.contains(current),
                    current.getDayOfWeek()));
            current = current.plusDays(1);
        }

        return calendar;
    }

    // ==================== Records ====================

    public record TradeSettlement(
            String tradeId,
            String symbol,
            String segment,
            LocalDate tradeDate,
            int quantity,
            java.math.BigDecimal value) {
    }

    public record PendingSettlement(
            String tradeId,
            String symbol,
            String segment,
            LocalDate tradeDate,
            LocalDate settlementDate,
            int quantity,
            java.math.BigDecimal value,
            int daysRemaining) {
    }

    public record SettlementDay(
            LocalDate date,
            boolean isBusinessDay,
            boolean isHoliday,
            DayOfWeek dayOfWeek) {
    }
}
