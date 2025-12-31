package com.vegatrader.upstox.api.expiry.service;

import com.vegatrader.upstox.api.expiry.entity.*;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.*;

/**
 * Expiry Calendar Service per a1.md section 1.4.
 * Resolves expiry dates for F&O instruments.
 * 
 * @since 4.5.0
 */
@Service
public class ExpiryCalendarService {

    private final ExpiryRuleRepository ruleRepository;

    public ExpiryCalendarService(ExpiryRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    /**
     * Resolve expiry date for a contract.
     * 
     * @param exchange       NSE / BSE
     * @param instrumentType FUTIDX / OPTIDX / FUTSTK / OPTSTK
     * @param cycleType      WEEKLY / MONTHLY / QUARTERLY
     * @param contractMonth  Year-month of contract
     * @return Resolved expiry date
     */
    public LocalDate resolveExpiry(
            String exchange,
            String instrumentType,
            String cycleType,
            YearMonth contractMonth) {

        ExchangeExpiryRuleEntity rule = ruleRepository
                .findByExchangeAndInstrumentTypeAndCycleType(exchange, instrumentType, cycleType)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No expiry rule for " + exchange + "/" + instrumentType + "/" + cycleType));

        LocalDate candidate = calculateExpiry(rule, contractMonth);
        return adjustForHoliday(candidate, rule);
    }

    /**
     * Get next expiry from today.
     */
    public LocalDate getNextExpiry(String exchange, String instrumentType, String cycleType) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate expiry = resolveExpiry(exchange, instrumentType, cycleType, currentMonth);

        if (expiry.isBefore(LocalDate.now()) || expiry.isEqual(LocalDate.now())) {
            expiry = resolveExpiry(exchange, instrumentType, cycleType, currentMonth.plusMonths(1));
        }

        return expiry;
    }

    /**
     * Get all expiries for next N months.
     */
    public java.util.List<LocalDate> getUpcomingExpiries(
            String exchange, String instrumentType, String cycleType, int months) {

        java.util.List<LocalDate> expiries = new java.util.ArrayList<>();
        YearMonth current = YearMonth.now();

        for (int i = 0; i <= months; i++) {
            try {
                LocalDate expiry = resolveExpiry(exchange, instrumentType, cycleType, current.plusMonths(i));
                if (!expiry.isBefore(LocalDate.now())) {
                    expiries.add(expiry);
                }
            } catch (Exception e) {
                // Skip if rule doesn't exist
            }
        }

        return expiries;
    }

    // === Private Helpers ===

    private LocalDate calculateExpiry(ExchangeExpiryRuleEntity rule, YearMonth contractMonth) {
        DayOfWeek expiryDayOfWeek = parseDayOfWeek(rule.getExpiryDay());

        if (rule.isWeekly()) {
            // Weekly: find the occurrence of expiry day in the week containing today
            LocalDate today = LocalDate.now();
            return today.with(TemporalAdjusters.nextOrSame(expiryDayOfWeek));
        } else {
            // Monthly/Quarterly: last occurrence of expiry day in the month
            LocalDate lastDayOfMonth = contractMonth.atEndOfMonth();
            return lastDayOfMonth.with(TemporalAdjusters.lastInMonth(expiryDayOfWeek));
        }
    }

    private LocalDate adjustForHoliday(LocalDate candidate, ExchangeExpiryRuleEntity rule) {
        // TODO: Integrate with trading calendar for holiday check
        // For now, return candidate as-is
        // In production: check if candidate is holiday, apply fallback

        if (rule.usePreviousTradingDay()) {
            // Would move to previous trading day if holiday
            return candidate;
        } else {
            // Would move to next trading day
            return candidate;
        }
    }

    private DayOfWeek parseDayOfWeek(String day) {
        return switch (day.toUpperCase()) {
            case "MONDAY" -> DayOfWeek.MONDAY;
            case "TUESDAY" -> DayOfWeek.TUESDAY;
            case "WEDNESDAY" -> DayOfWeek.WEDNESDAY;
            case "THURSDAY" -> DayOfWeek.THURSDAY;
            case "FRIDAY" -> DayOfWeek.FRIDAY;
            default -> throw new IllegalArgumentException("Invalid day: " + day);
        };
    }
}
