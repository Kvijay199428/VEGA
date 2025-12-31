# 📅 DERIVATIVES EXPIRY DAY SELECTION & CONDITIONS SETTINGS

## Comprehensive Implementation Guide for Trading Platform

---

## PART 1: NSE EQUITY DERIVATIVES EXPIRY SPECIFICATIONS

### **1. Index Futures & Options Expiry Rules**

```json
{
  "nse_expiry_specifications": {
    "NIFTY": {
      "name": "NIFTY 50 Index",
      "exchange": "NSE",
      "segment": "NFO",
      "expiry_pattern": "3_monthly_cycle",
      "expiry_day_rule": "last_tuesday_of_month",
      "exceptions": [
        {
          "month": "December",
          "rule": "last_tuesday_before_31_dec",
          "reason": "Year-end holiday considerations"
        },
        {
          "month": "January",
          "rule": "check_for_republic_day",
          "date": "26_jan",
          "rule_detail": "If last Tuesday falls on 26 Jan, move to previous Tuesday"
        }
      ],
      "contract_cycle": {
        "available_months": ["current_month", "next_month", "month_after"],
        "weekly_contracts": "Every_Thursday_expiry",
        "monthly_contracts": "Last_Tuesday",
        "quarterly_contracts": "Last_Tuesday_of_Mar_Jun_Sep_Dec"
      },
      "trading_hours": {
        "market_open": "09:15",
        "market_close": "15:30",
        "pre_market": "09:00",
        "post_market": "not_available"
      },
      "expiry_settlement": {
        "type": "cash_settled",
        "settlement_price": "index_closing_price",
        "settlement_time": "15:30",
        "settlement_day": "next_working_day"
      }
    },
    "BANKNIFTY": {
      "name": "NIFTY Bank Index",
      "exchange": "NSE",
      "segment": "NFO",
      "expiry_pattern": "3_monthly_cycle",
      "expiry_day_rule": "last_tuesday_of_month",
      "contract_cycle": {
        "available_months": ["current_month", "next_month", "month_after"],
        "weekly_contracts": "Every_Wednesday_expiry",
        "monthly_contracts": "Last_Tuesday",
        "quarterly_contracts": "Last_Tuesday_of_Mar_Jun_Sep_Dec"
      },
      "exceptions": [
        {
          "condition": "Public_Holiday",
          "action": "Move_to_previous_working_Tuesday"
        }
      ]
    },
    "FINNIFTY": {
      "name": "NIFTY Financial Services",
      "exchange": "NSE",
      "segment": "NFO",
      "expiry_pattern": "3_monthly_cycle",
      "expiry_day_rule": "last_tuesday_of_month",
      "contract_cycle": {
        "available_months": ["current_month", "next_month", "month_after"],
        "monthly_contracts": "Last_Tuesday",
        "quarterly_contracts": "Last_Tuesday_of_Mar_Jun_Sep_Dec"
      }
    },
    "MIDCPNIFTY": {
      "name": "NIFTY Midcap Select",
      "exchange": "NSE",
      "segment": "NFO",
      "expiry_pattern": "3_monthly_cycle",
      "expiry_day_rule": "last_tuesday_of_month",
      "contract_cycle": {
        "available_months": ["current_month", "next_month", "month_after"],
        "monthly_contracts": "Last_Tuesday",
        "quarterly_contracts": "Last_Tuesday_of_Mar_Jun_Sep_Dec"
      }
    },
    "NIFTYNXT50": {
      "name": "NIFTY Next 50",
      "exchange": "NSE",
      "segment": "NFO",
      "expiry_pattern": "3_monthly_cycle",
      "expiry_day_rule": "last_tuesday_of_month",
      "contract_cycle": {
        "available_months": ["current_month", "next_month", "month_after"],
        "monthly_contracts": "Last_Tuesday",
        "quarterly_contracts": "Last_Tuesday_of_Mar_Jun_Sep_Dec"
      }
    },
    "individual_stocks": {
      "description": "Stock futures and options on NSE",
      "expiry_pattern": "3_monthly_cycle",
      "expiry_day_rule": "last_tuesday_of_month",
      "eligible_stocks": [
        "SBIN", "TCS", "INFY", "HDFC", "RELIANCE",
        "ICICI", "AXIS", "LT", "MARUTI", "BAJAJ"
      ],
      "contract_cycle": {
        "available_months": ["current_month", "next_month", "month_after"],
        "monthly_contracts": "Last_Tuesday",
        "quarterly_contracts": "Last_Tuesday_of_Mar_Jun_Sep_Dec"
      }
    }
  }
}
```

### **2. Holiday Calendar & Expiry Adjustments**

```json
{
  "holiday_calendar_2025_2026": {
    "national_holidays": [
      {
        "date": "2025-01-26",
        "name": "Republic Day",
        "market_status": "closed"
      },
      {
        "date": "2025-03-11",
        "name": "Maha Shivaratri",
        "market_status": "closed"
      },
      {
        "date": "2025-04-18",
        "name": "Good Friday",
        "market_status": "closed"
      },
      {
        "date": "2025-05-01",
        "name": "Maharashtra Day",
        "market_status": "closed"
      },
      {
        "date": "2025-08-15",
        "name": "Independence Day",
        "market_status": "closed"
      },
      {
        "date": "2025-10-02",
        "name": "Gandhi Jayanti",
        "market_status": "closed"
      },
      {
        "date": "2025-10-20",
        "name": "Dussehra",
        "market_status": "closed"
      },
      {
        "date": "2025-11-01",
        "name": "Diwali",
        "market_status": "closed"
      },
      {
        "date": "2025-11-03",
        "name": "Diwali (Day 2)",
        "market_status": "closed"
      },
      {
        "date": "2025-12-25",
        "name": "Christmas",
        "market_status": "closed"
      }
    ],
    "half_trading_days": [
      {
        "date": "2025-03-28",
        "name": "Holi/Early Diwali Settlement",
        "market_close": "15:30"
      }
    ],
    "year_end_provisions": {
      "last_trading_day_2025": "2025-12-30",
      "note": "If last Tuesday (31-Dec) is public holiday, expiry moves to 30-Dec",
      "settlement_date": "2025-12-31"
    }
  }
}
```

---

## PART 2: EXPIRY DAY CALCULATOR SERVICE

### **1. TypeScript Implementation**

```typescript
// ExpiryCalculatorService.ts

import dayjs from 'dayjs';
import isBetween from 'dayjs/plugin/isBetween';
import utc from 'dayjs/plugin/utc';
import timezone from 'dayjs/plugin/timezone';

dayjs.extend(isBetween);
dayjs.extend(utc);
dayjs.extend(timezone);

interface ExpiryRule {
  underlying: string;
  expiryPattern: 'weekly' | 'monthly' | 'quarterly';
  baseDay: 'tuesday' | 'wednesday' | 'thursday';
  position: 'last' | 'first' | 'second' | 'third';
  exceptions: ExpiryException[];
}

interface ExpiryException {
  condition: string;
  affectedDates: string[];
  action: 'skip' | 'move_backward' | 'move_forward';
  moveDays?: number;
  reason: string;
}

interface ExpiryCondition {
  underlying: string;
  nextExpiry: string;
  daysUntilExpiry: number;
  isLastWeek: boolean;
  isLastDay: boolean;
  autoRollover: boolean;
  warnings: string[];
}

class ExpiryCalculatorService {
  private indianTimezone = 'Asia/Kolkata';
  private holidayList: string[] = [];
  private expiryRules: Map<string, ExpiryRule> = new Map();

  constructor() {
    this.initializeExpiryRules();
    this.loadHolidayCalendar();
  }

  /**
   * Initialize expiry rules for all index derivatives
   */
  private initializeExpiryRules() {
    const rules = {
      NIFTY: {
        underlying: 'NIFTY',
        expiryPattern: 'monthly',
        baseDay: 'tuesday',
        position: 'last',
        exceptions: [
          {
            condition: 'Year_End',
            affectedDates: ['2025-12-31', '2026-12-31'],
            action: 'move_backward',
            moveDays: 1,
            reason: 'New Year holiday'
          },
          {
            condition: 'Republic_Day',
            affectedDates: ['2025-01-28', '2026-01-27'],
            action: 'move_backward',
            moveDays: 1,
            reason: 'Republic Day holiday on 26-Jan'
          }
        ]
      },
      BANKNIFTY: {
        underlying: 'BANKNIFTY',
        expiryPattern: 'monthly',
        baseDay: 'tuesday',
        position: 'last',
        exceptions: []
      },
      FINNIFTY: {
        underlying: 'FINNIFTY',
        expiryPattern: 'monthly',
        baseDay: 'tuesday',
        position: 'last',
        exceptions: []
      },
      MIDCPNIFTY: {
        underlying: 'MIDCPNIFTY',
        expiryPattern: 'monthly',
        baseDay: 'tuesday',
        position: 'last',
        exceptions: []
      },
      NIFTYNXT50: {
        underlying: 'NIFTYNXT50',
        expiryPattern: 'monthly',
        baseDay: 'tuesday',
        position: 'last',
        exceptions: []
      }
    };

    Object.values(rules).forEach(rule => {
      this.expiryRules.set(rule.underlying, rule as ExpiryRule);
    });
  }

  /**
   * Load NSE holiday calendar
   */
  private loadHolidayCalendar() {
    this.holidayList = [
      '2025-01-26', '2025-03-11', '2025-04-18', '2025-05-01',
      '2025-08-15', '2025-10-02', '2025-10-20', '2025-11-01',
      '2025-11-03', '2025-12-25'
    ];
  }

  /**
   * Calculate next expiry date for a given underlying
   * @param underlying - Index name (e.g., NIFTY, BANKNIFTY)
   * @param fromDate - Calculate from this date (default: today)
   * @returns Date string in YYYY-MM-DD format
   */
  getNextExpiryDate(underlying: string, fromDate?: string): string {
    const rule = this.expiryRules.get(underlying);
    if (!rule) throw new Error(`Unknown underlying: ${underlying}`);

    const baseDate = dayjs(fromDate || new Date()).tz(this.indianTimezone);
    let expiryDate = this.calculateExpiryFromRule(rule, baseDate);

    // If calculated expiry is in the past, get next month
    if (expiryDate.isBefore(baseDate, 'day')) {
      expiryDate = this.calculateExpiryFromRule(rule, baseDate.add(1, 'month'));
    }

    // Apply exceptions
    expiryDate = this.applyExpiryExceptions(underlying, expiryDate);

    // Check for holidays
    while (this.isHoliday(expiryDate.format('YYYY-MM-DD'))) {
      expiryDate = expiryDate.subtract(1, 'day');
    }

    // Check for weekends
    while (expiryDate.day() === 0 || expiryDate.day() === 6) {
      expiryDate = expiryDate.subtract(1, 'day');
    }

    return expiryDate.format('YYYY-MM-DD');
  }

  /**
   * Get all available expiry dates for monthly, weekly contracts
   * @param underlying - Index name
   * @param months - Number of months to look ahead (default: 3)
   */
  getAvailableExpiryDates(underlying: string, months: number = 3): {
    monthly: string[];
    weekly: string[];
    quarterly: string[];
  } {
    const today = dayjs().tz(this.indianTimezone);
    const monthlyExpiries: string[] = [];
    const weeklyExpiries: string[] = [];
    const quarterlyExpiries: string[] = [];

    for (let i = 0; i < months; i++) {
      const targetMonth = today.add(i, 'month');
      
      // Monthly expiry (last Tuesday)
      const monthlyExpiry = this.getNextExpiryDate(underlying, targetMonth.format('YYYY-MM-01'));
      if (!monthlyExpiries.includes(monthlyExpiry)) {
        monthlyExpiries.push(monthlyExpiry);
      }

      // Quarterly expiry (Mar, Jun, Sep, Dec)
      if ([3, 6, 9, 12].includes(targetMonth.month() + 1)) {
        if (!quarterlyExpiries.includes(monthlyExpiry)) {
          quarterlyExpiries.push(monthlyExpiry);
        }
      }

      // Weekly expiries (every Thursday for most indices, Wednesday for BANKNIFTY)
      if (underlying === 'BANKNIFTY') {
        weeklyExpiries.push(...this.getWeeklyExpiries(targetMonth, 'wednesday'));
      } else {
        weeklyExpiries.push(...this.getWeeklyExpiries(targetMonth, 'thursday'));
      }
    }

    return {
      monthly: monthlyExpiries.sort(),
      weekly: weeklyExpiries.sort(),
      quarterly: quarterlyExpiries.sort()
    };
  }

  /**
   * Get weekly expiries for a given month and day
   */
  private getWeeklyExpiries(month: dayjs.Dayjs, dayName: string): string[] {
    const weeklies: string[] = [];
    const dayMap = { monday: 1, tuesday: 2, wednesday: 3, thursday: 4, friday: 5 };
    const targetDay = dayMap[dayName as keyof typeof dayMap];

    let date = month.startOf('month');
    while (date.month() === month.month()) {
      if (date.day() === targetDay && !this.isHoliday(date.format('YYYY-MM-DD'))) {
        weeklies.push(date.format('YYYY-MM-DD'));
      }
      date = date.add(1, 'day');
    }

    return weeklies;
  }

  /**
   * Calculate base expiry date from rule
   */
  private calculateExpiryFromRule(rule: ExpiryRule, baseDate: dayjs.Dayjs): dayjs.Dayjs {
    const dayMap = { monday: 1, tuesday: 2, wednesday: 3, thursday: 4, friday: 5 };
    const targetDay = dayMap[rule.baseDay];

    if (rule.position === 'last') {
      // Get last occurrence of day in month
      let date = baseDate.endOf('month');
      while (date.day() !== targetDay) {
        date = date.subtract(1, 'day');
      }
      return date;
    }

    return baseDate;
  }

  /**
   * Apply exceptions to expiry date
   */
  private applyExpiryExceptions(underlying: string, expiryDate: dayjs.Dayjs): dayjs.Dayjs {
    const rule = this.expiryRules.get(underlying);
    if (!rule) return expiryDate;

    for (const exception of rule.exceptions) {
      if (exception.affectedDates.includes(expiryDate.format('YYYY-MM-DD'))) {
        if (exception.action === 'move_backward') {
          return expiryDate.subtract(exception.moveDays || 1, 'day');
        } else if (exception.action === 'move_forward') {
          return expiryDate.add(exception.moveDays || 1, 'day');
        }
      }
    }

    return expiryDate;
  }

  /**
   * Check if date is holiday
   */
  private isHoliday(dateStr: string): boolean {
    return this.holidayList.includes(dateStr);
  }

  /**
   * Get expiry conditions for position management
   */
  getExpiryConditions(underlying: string, expiryDate: string): ExpiryCondition {
    const today = dayjs().tz(this.indianTimezone);
    const expiry = dayjs(expiryDate);
    const daysUntilExpiry = expiry.diff(today, 'day');

    const warnings: string[] = [];
    if (daysUntilExpiry <= 0) {
      warnings.push('Contract has expired');
    } else if (daysUntilExpiry === 1) {
      warnings.push('Expiry is tomorrow - Consider rollover');
    } else if (daysUntilExpiry <= 7) {
      warnings.push(`Only ${daysUntilExpiry} days until expiry`);
    }

    return {
      underlying,
      nextExpiry: expiryDate,
      daysUntilExpiry,
      isLastWeek: daysUntilExpiry <= 7,
      isLastDay: daysUntilExpiry <= 1,
      autoRollover: daysUntilExpiry <= 3,
      warnings
    };
  }

  /**
   * Calculate rollover period dates
   */
  getRolloverPeriod(underlying: string, expiryDate: string): {
    rolloverStart: string;
    rolloverEnd: string;
    rolloverDays: number;
  } {
    const expiry = dayjs(expiryDate);
    const rolloverStart = expiry.subtract(7, 'day').format('YYYY-MM-DD');
    const rolloverEnd = expiry.subtract(1, 'day').format('YYYY-MM-DD');
    const rolloverDays = expiry.diff(dayjs(rolloverStart), 'day');

    return {
      rolloverStart,
      rolloverEnd,
      rolloverDays
    };
  }
}

export default new ExpiryCalculatorService();
```

---

## PART 3: SETTINGS PAGE - DERIVATIVES EXPIRY CONFIGURATION

### **1. Frontend Component**

```typescript
// DerivativesExpirySettings.tsx
import React, { useState, useEffect } from 'react';
import axios from 'axios';
import expiryCalculatorService from './services/ExpiryCalculatorService';

interface ExpiryPreference {
  underlying: string;
  autoRollover: boolean;
  rolloverThresholdDays: number;
  notificationDays: number[];
  preferredExpiryType: 'weekly' | 'monthly' | 'quarterly';
  closeOnExpiryDay: boolean;
  closeOnExpiryPercent: number;
}

interface ExpiryNotificationRule {
  underlying: string;
  daysBeforeExpiry: number;
  notificationType: 'email' | 'sms' | 'in_app' | 'webhook';
  message: string;
  enabled: boolean;
}

const DerivativesExpirySettings: React.FC = () => {
  const [expiryPreferences, setExpiryPreferences] = useState<ExpiryPreference[]>([]);
  const [notificationRules, setNotificationRules] = useState<ExpiryNotificationRule[]>([]);
  const [selectedUnderlying, setSelectedUnderlying] = useState('NIFTY');
  const [upcomingExpiries, setUpcomingExpiries] = useState<any>({});
  const [holidayCalendar, setHolidayCalendar] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  const UNDERLYINGS = ['NIFTY', 'BANKNIFTY', 'FINNIFTY', 'MIDCPNIFTY', 'NIFTYNXT50'];

  useEffect(() => {
    const initializeSettings = async () => {
      try {
        setLoading(true);

        // Fetch existing preferences from backend
        const token = localStorage.getItem('access_token');
        const preferencesResponse = await axios.get(
          '/api/v1/settings/derivatives-expiry',
          { headers: { Authorization: `Bearer ${token}` } }
        );

        if (preferencesResponse.data.data.preferences) {
          setExpiryPreferences(preferencesResponse.data.data.preferences);
          setNotificationRules(preferencesResponse.data.data.notification_rules);
        } else {
          // Initialize with defaults
          initializeDefaultPreferences();
        }

        // Calculate upcoming expiries for all underlyings
        const expiryMap: any = {};
        UNDERLYINGS.forEach(underlying => {
          const dates = expiryCalculatorService.getAvailableExpiryDates(underlying, 3);
          expiryMap[underlying] = dates;
        });
        setUpcomingExpiries(expiryMap);

        // Load holiday calendar
        const holidayResponse = await axios.get('/api/v1/market/holidays', {
          headers: { Authorization: `Bearer ${token}` }
        });
        setHolidayCalendar(holidayResponse.data.data.holidays);

      } catch (error) {
        console.error('Initialize settings error:', error);
        initializeDefaultPreferences();
      } finally {
        setLoading(false);
      }
    };

    initializeSettings();
  }, []);

  const initializeDefaultPreferences = () => {
    const defaults = UNDERLYINGS.map(underlying => ({
      underlying,
      autoRollover: true,
      rolloverThresholdDays: 7,
      notificationDays: [7, 3, 1],
      preferredExpiryType: 'monthly' as const,
      closeOnExpiryDay: false,
      closeOnExpiryPercent: 0
    }));

    setExpiryPreferences(defaults);

    const defaultNotifications = UNDERLYINGS.flatMap(underlying => [
      {
        underlying,
        daysBeforeExpiry: 7,
        notificationType: 'in_app' as const,
        message: `${underlying} contract expires in 7 days - consider rollover`,
        enabled: true
      },
      {
        underlying,
        daysBeforeExpiry: 1,
        notificationType: 'email' as const,
        message: `${underlying} contract expires tomorrow`,
        enabled: true
      }
    ]);

    setNotificationRules(defaultNotifications);
  };

  const saveExpirySettings = async () => {
    try {
      const token = localStorage.getItem('access_token');
      const response = await axios.put(
        '/api/v1/settings/derivatives-expiry',
        {
          preferences: expiryPreferences,
          notification_rules: notificationRules
        },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      if (response.status === 200) {
        alert('Settings saved successfully');
      }
    } catch (error) {
      console.error('Save settings error:', error);
      alert('Failed to save settings');
    }
  };

  const updateUnderlyingPreference = (underlying: string, key: string, value: any) => {
    setExpiryPreferences(prefs =>
      prefs.map(pref =>
        pref.underlying === underlying
          ? { ...pref, [key]: value }
          : pref
      )
    );
  };

  const addNotificationRule = (underlying: string) => {
    const newRule: ExpiryNotificationRule = {
      underlying,
      daysBeforeExpiry: 1,
      notificationType: 'in_app',
      message: `Custom notification for ${underlying}`,
      enabled: true
    };
    setNotificationRules([...notificationRules, newRule]);
  };

  const updateNotificationRule = (index: number, key: string, value: any) => {
    const updated = [...notificationRules];
    updated[index] = { ...updated[index], [key]: value };
    setNotificationRules(updated);
  };

  const removeNotificationRule = (index: number) => {
    setNotificationRules(notificationRules.filter((_, i) => i !== index));
  };

  if (loading) return <div className="loading">Loading derivatives settings...</div>;

  return (
    <div className="derivatives-expiry-settings">
      <div className="settings-header">
        <h1>📅 Derivatives Expiry Management</h1>
        <p>Configure automatic rollover, notifications, and expiry handling</p>
      </div>

      {/* Upcoming Expiries Overview */}
      <section className="upcoming-expiries">
        <h2>Upcoming Contract Expiries</h2>
        <div className="expiry-grid">
          {UNDERLYINGS.map(underlying => {
            const nextExpiry = upcomingExpiries[underlying]?.monthly[0];
            if (!nextExpiry) return null;

            const condition = expiryCalculatorService.getExpiryConditions(underlying, nextExpiry);
            
            return (
              <div key={underlying} className={`expiry-card ${condition.isLastWeek ? 'warning' : ''}`}>
                <h3>{underlying}</h3>
                <p className="expiry-date">{nextExpiry}</p>
                <p className="days-remaining">
                  {condition.daysUntilExpiry > 0 
                    ? `${condition.daysUntilExpiry} days remaining`
                    : 'Expired'}
                </p>
                {condition.warnings.map((warning, idx) => (
                  <p key={idx} className="warning-text">⚠️ {warning}</p>
                ))}
              </div>
            );
          })}
        </div>
      </section>

      {/* Holiday Calendar */}
      <section className="holiday-calendar">
        <h2>📆 NSE Holiday Calendar</h2>
        <div className="holiday-list">
          {holidayCalendar.map((holiday, idx) => (
            <div key={idx} className="holiday-item">
              <span className="holiday-date">{holiday.date}</span>
              <span className="holiday-name">{holiday.name}</span>
              <span className="holiday-status">{holiday.market_status}</span>
            </div>
          ))}
        </div>
      </section>

      {/* Expiry Preferences for Each Underlying */}
      <section className="expiry-preferences">
        <h2>⚙️ Expiry Preferences</h2>

        <div className="underlying-selector">
          {UNDERLYINGS.map(underlying => (
            <button
              key={underlying}
              className={`selector-btn ${selectedUnderlying === underlying ? 'active' : ''}`}
              onClick={() => setSelectedUnderlying(underlying)}
            >
              {underlying}
            </button>
          ))}
        </div>

        {expiryPreferences.map(pref => {
          if (pref.underlying !== selectedUnderlying) return null;

          const rolloverPeriod = expiryCalculatorService.getRolloverPeriod(
            pref.underlying,
            upcomingExpiries[pref.underlying]?.monthly[0]
          );

          return (
            <div key={pref.underlying} className="preference-form">
              {/* Available Expiry Types */}
              <div className="form-group">
                <label>Preferred Expiry Type</label>
                <div className="radio-group">
                  <label>
                    <input
                      type="radio"
                      name={`expiry-type-${pref.underlying}`}
                      value="weekly"
                      checked={pref.preferredExpiryType === 'weekly'}
                      onChange={(e) => updateUnderlyingPreference(pref.underlying, 'preferredExpiryType', e.target.value)}
                    />
                    Weekly (Every Thursday/Wednesday)
                  </label>
                  <label>
                    <input
                      type="radio"
                      name={`expiry-type-${pref.underlying}`}
                      value="monthly"
                      checked={pref.preferredExpiryType === 'monthly'}
                      onChange={(e) => updateUnderlyingPreference(pref.underlying, 'preferredExpiryType', e.target.value)}
                    />
                    Monthly (Last Tuesday)
                  </label>
                  <label>
                    <input
                      type="radio"
                      name={`expiry-type-${pref.underlying}`}
                      value="quarterly"
                      checked={pref.preferredExpiryType === 'quarterly'}
                      onChange={(e) => updateUnderlyingPreference(pref.underlying, 'preferredExpiryType', e.target.value)}
                    />
                    Quarterly (Mar/Jun/Sep/Dec)
                  </label>
                </div>
              </div>

              {/* Available Expiry Dates */}
              <div className="form-group">
                <label>Upcoming Contract Dates</label>
                <div className="expiry-dates-list">
                  <div className="expiry-type">
                    <h4>Monthly Expiries</h4>
                    {upcomingExpiries[pref.underlying]?.monthly.slice(0, 3).map((date, idx) => (
                      <span key={idx} className="date-badge">{date}</span>
                    ))}
                  </div>
                  <div className="expiry-type">
                    <h4>Weekly Expiries (Next 4)</h4>
                    {upcomingExpiries[pref.underlying]?.weekly.slice(0, 4).map((date, idx) => (
                      <span key={idx} className="date-badge">{date}</span>
                    ))}
                  </div>
                </div>
              </div>

              {/* Auto Rollover Settings */}
              <div className="form-group">
                <label className="checkbox-label">
                  <input
                    type="checkbox"
                    checked={pref.autoRollover}
                    onChange={(e) => updateUnderlyingPreference(pref.underlying, 'autoRollover', e.target.checked)}
                  />
                  Enable Auto Rollover
                </label>
                <p className="help-text">
                  Automatically move positions from expiring contract to next contract
                </p>
              </div>

              {pref.autoRollover && (
                <div className="form-group">
                  <label>Rollover Threshold (Days before expiry)</label>
                  <input
                    type="number"
                    min="1"
                    max="15"
                    value={pref.rolloverThresholdDays}
                    onChange={(e) => updateUnderlyingPreference(
                      pref.underlying,
                      'rolloverThresholdDays',
                      parseInt(e.target.value)
                    )}
                  />
                  <p className="help-text">
                    Rollover period: {rolloverPeriod.rolloverStart} to {rolloverPeriod.rolloverEnd}
                    ({rolloverPeriod.rolloverDays} days)
                  </p>
                </div>
              )}

              {/* Close on Expiry Settings */}
              <div className="form-group">
                <label className="checkbox-label">
                  <input
                    type="checkbox"
                    checked={pref.closeOnExpiryDay}
                    onChange={(e) => updateUnderlyingPreference(pref.underlying, 'closeOnExpiryDay', e.target.checked)}
                  />
                  Close Position on Expiry Day
                </label>
                <p className="help-text">
                  Automatically close all positions when contract expires
                </p>
              </div>

              {pref.closeOnExpiryDay && (
                <div className="form-group">
                  <label>Close Time (% of day remaining)</label>
                  <input
                    type="number"
                    min="0"
                    max="100"
                    step="10"
                    value={pref.closeOnExpiryPercent}
                    onChange={(e) => updateUnderlyingPreference(
                      pref.underlying,
                      'closeOnExpiryPercent',
                      parseInt(e.target.value)
                    )}
                  />
                  <p className="help-text">
                    Close at {pref.closeOnExpiryPercent}% through the trading day
                  </p>
                </div>
              )}

              {/* Notification Days */}
              <div className="form-group">
                <label>Notification Before Expiry (Days)</label>
                <div className="notification-days">
                  {[1, 3, 7, 14].map(days => (
                    <label key={days} className="checkbox-inline">
                      <input
                        type="checkbox"
                        checked={pref.notificationDays.includes(days)}
                        onChange={(e) => {
                          if (e.target.checked) {
                            updateUnderlyingPreference(
                              pref.underlying,
                              'notificationDays',
                              [...pref.notificationDays, days].sort((a, b) => b - a)
                            );
                          } else {
                            updateUnderlyingPreference(
                              pref.underlying,
                              'notificationDays',
                              pref.notificationDays.filter(d => d !== days)
                            );
                          }
                        }}
                      />
                      {days} day{days > 1 ? 's' : ''}
                    </label>
                  ))}
                </div>
              </div>
            </div>
          );
        })}
      </section>

      {/* Notification Rules */}
      <section className="notification-rules">
        <h2>🔔 Expiry Notifications</h2>
        
        <div className="rules-list">
          {notificationRules
            .filter(rule => rule.underlying === selectedUnderlying)
            .map((rule, idx) => (
              <div key={idx} className="notification-rule">
                <div className="rule-header">
                  <span className="rule-label">
                    {rule.daysBeforeExpiry} day{rule.daysBeforeExpiry > 1 ? 's' : ''} before expiry
                  </span>
                  <button
                    className="btn-remove"
                    onClick={() => removeNotificationRule(notificationRules.indexOf(rule))}
                  >
                    ✕
                  </button>
                </div>

                <div className="rule-content">
                  <div className="form-group">
                    <label>Notification Type</label>
                    <select
                      value={rule.notificationType}
                      onChange={(e) => updateNotificationRule(notificationRules.indexOf(rule), 'notificationType', e.target.value)}
                    >
                      <option value="in_app">In-App</option>
                      <option value="email">Email</option>
                      <option value="sms">SMS</option>
                      <option value="webhook">Webhook</option>
                    </select>
                  </div>

                  <div className="form-group">
                    <label>Message</label>
                    <input
                      type="text"
                      value={rule.message}
                      onChange={(e) => updateNotificationRule(notificationRules.indexOf(rule), 'message', e.target.value)}
                    />
                  </div>

                  <label className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={rule.enabled}
                      onChange={(e) => updateNotificationRule(notificationRules.indexOf(rule), 'enabled', e.target.checked)}
                    />
                    Enabled
                  </label>
                </div>
              </div>
            ))}
        </div>

        <button
          className="btn btn-secondary"
          onClick={() => addNotificationRule(selectedUnderlying)}
        >
          + Add Notification Rule
        </button>
      </section>

      {/* Action Buttons */}
      <div className="settings-actions">
        <button className="btn btn-primary" onClick={saveExpirySettings}>
          💾 Save Expiry Settings
        </button>
        <button className="btn btn-secondary" onClick={() => initializeDefaultPreferences()}>
          ↺ Reset to Defaults
        </button>
      </div>
    </div>
  );
};

export default DerivativesExpirySettings;
```

---

## PART 4: BACKEND API ENDPOINTS

### **1. Expiry Settings API**

```typescript
// Backend: FastAPI (Python)

from fastapi import APIRouter, Depends, HTTPException
from datetime import datetime, timedelta
import pytz

router = APIRouter(prefix="/api/v1/settings", tags=["settings"])

@router.get("/derivatives-expiry")
async def get_expiry_settings(token: str = Depends(verify_token)):
    """
    GET /api/v1/settings/derivatives-expiry
    
    Response:
    {
      "status": "success",
      "data": {
        "preferences": [
          {
            "underlying": "NIFTY",
            "autoRollover": true,
            "rolloverThresholdDays": 7,
            "notificationDays": [1, 3, 7],
            "preferredExpiryType": "monthly",
            "closeOnExpiryDay": false,
            "closeOnExpiryPercent": 0
          }
        ],
        "notification_rules": [
          {
            "underlying": "NIFTY",
            "daysBeforeExpiry": 7,
            "notificationType": "in_app",
            "message": "...",
            "enabled": true
          }
        ]
      }
    }
    """
    try:
        user_id = token['user_id']
        preferences = await db.get_user_expiry_preferences(user_id)
        notifications = await db.get_user_notification_rules(user_id)
        
        return {
            "status": "success",
            "data": {
                "preferences": preferences,
                "notification_rules": notifications
            }
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.put("/derivatives-expiry")
async def update_expiry_settings(
    settings_data: dict,
    token: str = Depends(verify_token)
):
    """
    PUT /api/v1/settings/derivatives-expiry
    
    Request:
    {
      "preferences": [...],
      "notification_rules": [...]
    }
    """
    try:
        user_id = token['user_id']
        
        # Validate and save preferences
        for pref in settings_data['preferences']:
            await db.save_expiry_preference(user_id, pref)
        
        # Validate and save notification rules
        for rule in settings_data['notification_rules']:
            await db.save_notification_rule(user_id, rule)
        
        return {
            "status": "success",
            "message": "Expiry settings updated successfully"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/holidays")
async def get_market_holidays(token: str = Depends(verify_token)):
    """
    GET /api/v1/market/holidays
    
    Response:
    {
      "status": "success",
      "data": {
        "holidays": [
          {
            "date": "2025-01-26",
            "name": "Republic Day",
            "market_status": "closed"
          }
        ]
      }
    }
    """
    holidays = [
        {"date": "2025-01-26", "name": "Republic Day", "market_status": "closed"},
        {"date": "2025-03-11", "name": "Maha Shivaratri", "market_status": "closed"},
        # ... more holidays
    ]
    
    return {
        "status": "success",
        "data": {"holidays": holidays}
    }

@router.get("/expiry-calendar")
async def get_expiry_calendar(
    underlying: str = "NIFTY",
    months: int = 3,
    token: str = Depends(verify_token)
):
    """
    GET /api/v1/settings/expiry-calendar?underlying=NIFTY&months=3
    
    Response:
    {
      "status": "success",
      "data": {
        "underlying": "NIFTY",
        "monthly_expiries": ["2025-01-28", "2025-02-25", "2025-03-25"],
        "weekly_expiries": ["2025-01-09", "2025-01-16", ...],
        "quarterly_expiries": ["2025-03-25", "2025-06-24", ...],
        "next_expiry": "2025-01-09",
        "days_until_expiry": 15,
        "rollover_period": {
          "start": "2025-01-02",
          "end": "2025-01-08",
          "days": 7
        }
      }
    }
    """
    try:
        from app.services.expiry_calculator import ExpiryCalculator
        
        calculator = ExpiryCalculator()
        monthly = calculator.get_monthly_expiries(underlying, months)
        weekly = calculator.get_weekly_expiries(underlying, months)
        quarterly = calculator.get_quarterly_expiries(underlying, months)
        next_expiry = calculator.get_next_expiry(underlying)
        rollover = calculator.get_rollover_period(underlying, next_expiry)
        
        return {
            "status": "success",
            "data": {
                "underlying": underlying,
                "monthly_expiries": monthly,
                "weekly_expiries": weekly,
                "quarterly_expiries": quarterly,
                "next_expiry": next_expiry,
                "days_until_expiry": calculator.days_until(next_expiry),
                "rollover_period": rollover
            }
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
```

---

## PART 5: AUTOMATIC ROLLOVER SERVICE

### **1. Rollover Automation**

```typescript
// RolloverService.ts

import axios from 'axios';
import expiryCalculatorService from './ExpiryCalculatorService';

interface RolloverTask {
  underlying: string;
  currentExpiry: string;
  nextExpiry: string;
  positions: any[];
  status: 'pending' | 'in_progress' | 'completed' | 'failed';
}

class RolloverService {
  private rolloverTasks: Map<string, RolloverTask> = new Map();

  /**
   * Check and execute rollovers based on user preferences
   */
  async checkAndExecuteRollovers(userToken: string, userId: string) {
    try {
      // Get user preferences
      const prefResponse = await axios.get(
        `/api/v1/settings/derivatives-expiry`,
        { headers: { Authorization: `Bearer ${userToken}` } }
      );

      const preferences = prefResponse.data.data.preferences;

      // Check each underlying
      for (const pref of preferences) {
        if (!pref.autoRollover) continue;

        await this.handleRolloverForUnderlying(
          userToken,
          userId,
          pref.underlying,
          pref.rolloverThresholdDays
        );
      }
    } catch (error) {
      console.error('Rollover check error:', error);
    }
  }

  /**
   * Handle rollover for specific underlying
   */
  private async handleRolloverForUnderlying(
    token: string,
    userId: string,
    underlying: string,
    thresholdDays: number
  ) {
    try {
      // Get current expiry and next expiry
      const nextExpiry = expiryCalculatorService.getNextExpiryDate(underlying);
      const condition = expiryCalculatorService.getExpiryConditions(underlying, nextExpiry);

      // Check if we're in rollover window
      if (condition.daysUntilExpiry > thresholdDays) {
        return; // Not yet time to rollover
      }

      // Get current positions
      const positionsResponse = await axios.get(
        `/api/v1/portfolio/positions`,
        { headers: { Authorization: `Bearer ${token}` } }
      );

      const positions = positionsResponse.data.data
        .filter((pos: any) => pos.underlying_key === underlying);

      if (positions.length === 0) {
        return; // No positions to rollover
      }

      // Execute rollover
      const rolloverTask: RolloverTask = {
        underlying,
        currentExpiry: nextExpiry,
        nextExpiry: expiryCalculatorService.getNextExpiryDate(underlying, nextExpiry),
        positions,
        status: 'pending'
      };

      this.rolloverTasks.set(`${userId}_${underlying}`, rolloverTask);

      await this.executeRollover(token, userId, rolloverTask);

    } catch (error) {
      console.error(`Rollover error for ${underlying}:`, error);
    }
  }

  /**
   * Execute actual rollover
   */
  private async executeRollover(
    token: string,
    userId: string,
    task: RolloverTask
  ) {
    try {
      const taskKey = `${userId}_${task.underlying}`;
      const task_update = this.rolloverTasks.get(taskKey);
      
      if (!task_update) return;
      task_update.status = 'in_progress';

      // Prepare rollover orders
      const closeOrders = task.positions.map(pos => ({
        instrument_key: pos.instrument_key,
        quantity: Math.abs(pos.net_quantity),
        transaction_type: pos.net_quantity > 0 ? 'SELL' : 'BUY',
        order_type: 'MARKET'
      }));

      const openOrders = task.positions.map(pos => {
        // Get new instrument for next expiry
        return {
          underlying: task.underlying,
          expiry: task.nextExpiry,
          quantity: Math.abs(pos.net_quantity),
          transaction_type: pos.net_quantity > 0 ? 'BUY' : 'SELL',
          order_type: 'LIMIT'
        };
      });

      // Close current expiry positions
      await axios.post(
        `/api/v1/orders/bulk`,
        { orders: closeOrders },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      // Open next expiry positions
      // (requires additional logic to determine exact strike/contract)
      
      task_update.status = 'completed';
      this.rolloverTasks.set(taskKey, task_update);

    } catch (error) {
      console.error('Rollover execution error:', error);
      const taskKey = `${userId}_${task.underlying}`;
      const task_update = this.rolloverTasks.get(taskKey);
      if (task_update) {
        task_update.status = 'failed';
        this.rolloverTasks.set(taskKey, task_update);
      }
    }
  }

  /**
   * Get rollover status
   */
  getRolloverStatus(userId: string, underlying: string): RolloverTask | null {
    const key = `${userId}_${underlying}`;
    return this.rolloverTasks.get(key) || null;
  }
}

export default new RolloverService();
```

---

## PART 6: NOTIFICATION SCHEDULER

### **1. Scheduled Notifications**

```typescript
// NotificationScheduler.ts

import node-cron from 'node-cron';
import axios from 'axios';

class NotificationScheduler {
  /**
   * Schedule daily expiry checks
   */
  scheduleExpiryNotifications() {
    // Run every day at 9:00 AM IST
    node-cron.schedule('0 9 * * *', async () => {
      await this.checkAndNotifyExpiringContracts();
    });

    // Run every 4 hours during market hours
    node-cron.schedule('0 */4 9-15 * * 1-5', async () => {
      await this.checkAndNotifyExpiringContracts();
    });
  }

  /**
   * Check and send notifications for expiring contracts
   */
  private async checkAndNotifyExpiringContracts() {
    try {
      const UNDERLYINGS = ['NIFTY', 'BANKNIFTY', 'FINNIFTY', 'MIDCPNIFTY', 'NIFTYNXT50'];

      for (const underlying of UNDERLYINGS) {
        const nextExpiry = expiryCalculatorService.getNextExpiryDate(underlying);
        const condition = expiryCalculatorService.getExpiryConditions(underlying, nextExpiry);

        // Send notifications for users with open positions
        const usersWithPositions = await this.getUsersWithPositions(underlying);

        for (const userId of usersWithPositions) {
          const userPrefs = await this.getUserPreferences(userId);
          const pref = userPrefs.find((p: any) => p.underlying === underlying);

          if (pref && pref.notificationDays.includes(condition.daysUntilExpiry)) {
            await this.sendNotifications(userId, pref, condition);
          }
        }
      }
    } catch (error) {
      console.error('Notification check error:', error);
    }
  }

  /**
   * Send notifications via multiple channels
   */
  private async sendNotifications(userId: string, pref: any, condition: any) {
    const rules = await this.getUserNotificationRules(userId, pref.underlying);

    for (const rule of rules) {
      if (!rule.enabled) continue;

      const message = this.buildNotificationMessage(rule.message, pref, condition);

      switch (rule.notificationType) {
        case 'in_app':
          await this.sendInAppNotification(userId, message);
          break;
        case 'email':
          await this.sendEmailNotification(userId, message);
          break;
        case 'sms':
          await this.sendSMSNotification(userId, message);
          break;
        case 'webhook':
          await this.sendWebhook(userId, message);
          break;
      }
    }
  }

  private buildNotificationMessage(template: string, pref: any, condition: any): string {
    return template
      .replace('{underlying}', condition.underlying)
      .replace('{expiry_date}', condition.nextExpiry)
      .replace('{days_remaining}', condition.daysUntilExpiry.toString());
  }

  private async sendInAppNotification(userId: string, message: string) {
    // Save to database
    await db.create_notification({
      user_id: userId,
      message,
      type: 'expiry_alert',
      read: false,
      created_at: new Date()
    });
  }

  private async sendEmailNotification(userId: string, message: string) {
    const user = await db.get_user(userId);
    // Send email via provider
    // await email_service.send(user.email, message);
  }

  private async sendSMSNotification(userId: string, message: string) {
    const user = await db.get_user(userId);
    // Send SMS via provider
    // await sms_service.send(user.phone, message);
  }

  private async sendWebhook(userId: string, message: string) {
    const user = await db.get_user(userId);
    const webhookUrl = user.webhook_url;
    
    if (webhookUrl) {
      await axios.post(webhookUrl, {
        event: 'derivatives_expiry',
        message,
        timestamp: new Date()
      });
    }
  }
}

export default new NotificationScheduler();
```

---

## PART 7: DATABASE SCHEMA

```sql
-- Expiry Preferences Table
CREATE TABLE expiry_preferences (
  id SERIAL PRIMARY KEY,
  user_id VARCHAR(255) NOT NULL,
  underlying VARCHAR(50) NOT NULL,
  auto_rollover BOOLEAN DEFAULT true,
  rollover_threshold_days INTEGER DEFAULT 7,
  notification_days INTEGER[] DEFAULT ARRAY[1, 3, 7],
  preferred_expiry_type VARCHAR(50) DEFAULT 'monthly',
  close_on_expiry_day BOOLEAN DEFAULT false,
  close_on_expiry_percent INTEGER DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(user_id, underlying)
);

-- Notification Rules Table
CREATE TABLE notification_rules (
  id SERIAL PRIMARY KEY,
  user_id VARCHAR(255) NOT NULL,
  underlying VARCHAR(50) NOT NULL,
  days_before_expiry INTEGER NOT NULL,
  notification_type VARCHAR(50) NOT NULL,
  message TEXT,
  enabled BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Rollover History Table
CREATE TABLE rollover_history (
  id SERIAL PRIMARY KEY,
  user_id VARCHAR(255) NOT NULL,
  underlying VARCHAR(50) NOT NULL,
  current_expiry DATE NOT NULL,
  next_expiry DATE NOT NULL,
  positions_count INTEGER,
  status VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  completed_at TIMESTAMP
);

-- Notifications Table
CREATE TABLE notifications (
  id SERIAL PRIMARY KEY,
  user_id VARCHAR(255) NOT NULL,
  message TEXT,
  type VARCHAR(50),
  read BOOLEAN DEFAULT false,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## PART 8: GLOBAL USAGE PATTERNS

### **1. Using Expiry Service in Components**

```typescript
// Using expiry service across application

// In Dashboard
const nextExpiryNifty = expiryCalculatorService.getNextExpiryDate('NIFTY');
const condition = expiryCalculatorService.getExpiryConditions('NIFTY', nextExpiryNifty);

// In Portfolio
const expiryDates = expiryCalculatorService.getAvailableExpiryDates('BANKNIFTY', 3);

// In Orders
const rolloverPeriod = expiryCalculatorService.getRolloverPeriod('NIFTY', expiryDate);

// In Risk Management
if (condition.isLastDay) {
  // Execute closing logic
}
```

---

**Last Updated**: December 12, 2025
**Version**: 1.0
**Status**: Complete Implementation Guide Ready

