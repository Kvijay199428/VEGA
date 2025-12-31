# 📱 FRONTEND PAGE DESIGNS WITH BACKEND INTEGRATION

## Complete Frontend Implementation Guide for AI Trading Platform

---

## PART 1: DERIVATIVES CONTRACT SPECIFICATIONS & DATA MODELS

### **1. Index Derivatives Contracts**

```json
{
  "derivatives_contracts": {
    "NIFTY": {
      "name": "NIFTY 50",
      "symbol": "NIFTY",
      "instrument_type": "FUTIDX/OPTIDX",
      "underlying_type": "index",
      "lot_size": 50,
      "multiplier": 50,
      "min_contract_value": 1500000,
      "price_step": 0.05,
      "expiry_cycle": "3_month_nearest_mid_far",
      "expiry_day": "last_tuesday_of_month",
      "price_band": "10_percent",
      "trading_hours": "09:15_to_15:30",
      "margin": {
        "type": "span_margin",
        "initial_margin_percent": 8,
        "maintenance_margin_percent": 6
      },
      "options": {
        "expiry_types": ["weekly", "monthly", "quarterly", "half_yearly"],
        "strike_intervals": {
          "weekly_monthly": {
            "all_levels": {
              "interval": 50,
              "strikes": "35_itm_1_atm_35_otm"
            }
          },
          "quarterly_half_yearly": {
            "2000_to_4000": { "interval": 100, "strikes": "5_1_5" },
            "4000_to_5000": { "interval": 500, "strikes": "2_1_2" },
            "5000_to_6000": { "interval": 500, "strikes": "3_1_3" },
            "6000_to_7500": { "interval": 500, "strikes": "4_1_4" },
            "7500_to_15000": { "interval": 500, "strikes": "5_1_5" },
            "15000_to_25000": { "interval": 1000, "strikes": "5_1_5" },
            "above_25000": { "interval": 1500, "strikes": "5_1_5" }
          }
        }
      }
    },
    "BANKNIFTY": {
      "name": "NIFTY Bank",
      "symbol": "BANKNIFTY",
      "instrument_type": "FUTIDX/OPTIDX",
      "underlying_type": "index",
      "lot_size": 15,
      "multiplier": 15,
      "min_contract_value": 1500000,
      "price_step": 0.05,
      "expiry_cycle": "3_month_nearest_mid_far",
      "expiry_day": "last_tuesday_of_month",
      "options": {
        "strike_intervals": {
          "all_levels_100": { "interval": 100, "strikes": "50_1_50" },
          "all_levels_500": { "interval": 500, "strikes": "15_1_15" }
        }
      }
    },
    "FINNIFTY": {
      "name": "NIFTY Financial Services",
      "symbol": "FINNIFTY",
      "instrument_type": "FUTIDX/OPTIDX",
      "underlying_type": "index",
      "lot_size": 40,
      "multiplier": 40,
      "min_contract_value": 1500000
    },
    "MIDCPNIFTY": {
      "name": "NIFTY Midcap Select",
      "symbol": "MIDCPNIFTY",
      "instrument_type": "FUTIDX/OPTIDX",
      "underlying_type": "index",
      "lot_size": 75,
      "multiplier": 75,
      "min_contract_value": 1500000,
      "options": {
        "strike_intervals": {
          "25_interval": { "interval": 25, "strikes": "30_1_30" },
          "100_interval": { "interval": 100, "strikes": "15_1_15" }
        }
      }
    },
    "NIFTYNXT50": {
      "name": "NIFTY Next 50",
      "symbol": "NIFTYNXT50",
      "instrument_type": "FUTIDX/OPTIDX",
      "underlying_type": "index",
      "lot_size": 50,
      "multiplier": 50,
      "min_contract_value": 1500000
    }
  }
}
```

---

### **2. Upstox API Instrument Response Structure**

```json
{
  "instrument_response": {
    "status": "success",
    "data": [
      {
        "instrument_token": "3045",
        "exchange_token": "999900000",
        "tradingsymbol": "SBIN",
        "name": "State Bank of India",
        "last_price": 542.50,
        "expiry": null,
        "strike": null,
        "lot_size": 1,
        "instrument_type": "EQ",
        "segment": "NSE",
        "exchange": "NSE",
        "exchange_name": "National Stock Exchange of India Limited"
      },
      {
        "instrument_token": "3045_19DEC25_19500_CE",
        "exchange_token": "999900001",
        "tradingsymbol": "NIFTY1925C19500",
        "name": "NIFTY 50 Dec 25 19500 Call",
        "last_price": 85.50,
        "expiry": "2025-12-25",
        "strike": 19500,
        "lot_size": 50,
        "instrument_type": "OPTIDX",
        "segment": "NFO",
        "exchange": "NSE",
        "exchange_name": "National Stock Exchange of India Limited",
        "option_type": "CE",
        "underlying_key": "NIFTY"
      },
      {
        "instrument_token": "3045_19DEC25_NIFTY_FUT",
        "exchange_token": "999900002",
        "tradingsymbol": "NIFTY25DEC19500",
        "name": "NIFTY 50 Dec 25 Futures",
        "last_price": 19550.00,
        "expiry": "2025-12-25",
        "strike": null,
        "lot_size": 50,
        "instrument_type": "FUTIDX",
        "segment": "NFO",
        "exchange": "NSE",
        "exchange_name": "National Stock Exchange of India Limited",
        "underlying_key": "NIFTY"
      }
    ]
  }
}
```

---

### **3. Global Instrument Management System**

#### **A. Instrument Key Generation & Lookup Service**

```typescript
// Global Instrument Service (TypeScript)

class InstrumentService {
  /**
   * Generate unique instrument key from symbol and details
   * Format: SEGMENT_UNDERLYING_EXPIRY_STRIKE_OPTTYPE
   * Examples:
   *   - EQ_SBIN = Equity SBIN
   *   - NFO_NIFTY_25DEC_19500_CE = Options
   *   - NFO_NIFTY_25DEC_FUT = Futures
   */
  generateInstrumentKey(instrument: any): string {
    const { segment, tradingsymbol, expiry, strike, option_type } = instrument;
    
    if (segment === 'NSE' || segment === 'BSE') {
      // Equity: EQ_SYMBOL
      return `EQ_${tradingsymbol}`;
    } else if (segment === 'NFO') {
      // Derivatives: NFO_UNDERLYING_EXPIRY_STRIKE_TYPE
      if (instrument.instrument_type === 'FUTIDX' || instrument.instrument_type === 'FUTSTK') {
        // Futures: NFO_NIFTY_25DEC_FUT
        const expiryStr = this.formatExpiry(expiry); // 25DEC format
        return `NFO_${instrument.underlying_key}_${expiryStr}_FUT`;
      } else if (instrument.instrument_type === 'OPTIDX' || instrument.instrument_type === 'OPTSTK') {
        // Options: NFO_NIFTY_25DEC_19500_CE
        const expiryStr = this.formatExpiry(expiry);
        return `NFO_${instrument.underlying_key}_${expiryStr}_${strike}_${option_type}`;
      }
    }
    return null;
  }

  /**
   * Parse instrument key to extract details
   * NFO_NIFTY_25DEC_19500_CE → { segment: NFO, underlying: NIFTY, expiry: 2025-12-25, strike: 19500, type: CE }
   */
  parseInstrumentKey(key: string): object {
    const parts = key.split('_');
    return {
      segment: parts[0],
      underlying: parts[1],
      expiry: this.parseExpiryFormat(parts[2]),
      strike: parts[3] || null,
      optionType: parts[4] || null,
      isFuture: parts[3] === 'FUT' || (parts.length === 3 && parts[2].includes('FUT'))
    };
  }

  /**
   * Format date to expiry string: 2025-12-25 → 25DEC
   */
  formatExpiry(date: string): string {
    const d = new Date(date);
    const day = String(d.getDate()).padStart(2, '0');
    const month = ['JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG', 'SEP', 'OCT', 'NOV', 'DEC'][d.getMonth()];
    return `${day}${month}`;
  }

  /**
   * Search instruments by symbol or keyword
   */
  searchInstruments(instruments: any[], query: string, filters?: object): any[] {
    return instruments.filter(inst => {
      // Basic text search
      const matches = inst.tradingsymbol.includes(query.toUpperCase()) || 
                     inst.name.toUpperCase().includes(query.toUpperCase());
      
      if (!matches) return false;
      
      // Apply filters if provided
      if (filters?.segment) {
        if (inst.segment !== filters.segment) return false;
      }
      if (filters?.instrumentType) {
        if (inst.instrument_type !== filters.instrumentType) return false;
      }
      if (filters?.underlyingKey) {
        if (inst.underlying_key !== filters.underlyingKey) return false;
      }
      
      return true;
    });
  }

  /**
   * Get contracts for underlying (e.g., all NIFTY contracts)
   */
  getContractsForUnderlying(instruments: any[], underlyingKey: string): object {
    const contracts = {
      futures: [],
      optionsCall: [],
      optionsPut: [],
      expiryDates: new Set()
    };

    instruments.forEach(inst => {
      if (inst.underlying_key === underlyingKey && inst.segment === 'NFO') {
        if (inst.instrument_type === 'FUTIDX' || inst.instrument_type === 'FUTSTK') {
          contracts.futures.push(inst);
        } else if (inst.instrument_type === 'OPTIDX' || inst.instrument_type === 'OPTSTK') {
          if (inst.option_type === 'CE') {
            contracts.optionsCall.push(inst);
          } else if (inst.option_type === 'PE') {
            contracts.optionsPut.push(inst);
          }
        }
        contracts.expiryDates.add(inst.expiry);
      }
    });

    return {
      ...contracts,
      expiryDates: Array.from(contracts.expiryDates).sort()
    };
  }

  /**
   * Get Greeks and metrics for option
   */
  getOptionMetrics(option: any): object {
    return {
      instrumentKey: this.generateInstrumentKey(option),
      symbol: option.tradingsymbol,
      strike: option.strike,
      optionType: option.option_type,
      lastPrice: option.last_price,
      bid: option.bid || 0,
      ask: option.ask || 0,
      bidQty: option.bid_qty || 0,
      askQty: option.ask_qty || 0,
      iv: option.iv || 0,
      delta: option.delta || 0,
      gamma: option.gamma || 0,
      theta: option.theta || 0,
      vega: option.vega || 0,
      openInterest: option.oi || 0,
      volume: option.volume || 0,
      impliedVolatility: option.iv || 0
    };
  }

  /**
   * Calculate lot value and margin
   */
  calculateContractValue(instrument: any, quantity: number): object {
    const lotSize = instrument.lot_size;
    const price = instrument.last_price;
    const totalQty = quantity * lotSize;
    const totalValue = totalQty * price;

    let margin = 0;
    if (instrument.instrument_type === 'FUTIDX' || instrument.instrument_type === 'FUTSTK') {
      // Futures: 8-10% SPAN margin
      margin = totalValue * 0.08;
    } else if (instrument.instrument_type === 'OPTIDX' || instrument.instrument_type === 'OPTSTK') {
      // Options: Premium + margin
      margin = (totalQty * price) * 0.5; // Simplified, actual = premium + span
    }

    return {
      lotSize: lotSize,
      price: price,
      totalQuantity: totalQty,
      totalValue: totalValue,
      estimatedMargin: margin,
      marginPercent: (margin / totalValue * 100).toFixed(2)
    };
  }
}

export default new InstrumentService();
```

---

## PART 2: PAGE-BY-PAGE FRONTEND DESIGNS WITH BACKEND INTEGRATION

### **PAGE 1: LOGIN PAGE** 

#### **Frontend Component Structure**

```typescript
// LoginPage.tsx
import React, { useState, useEffect } from 'react';
import axios from 'axios';

interface LoginPageProps {
  onLoginSuccess: (userToken: string) => void;
  onError: (error: string) => void;
}

const LoginPage: React.FC<LoginPageProps> = ({ onLoginSuccess, onError }) => {
  const [sessionInfo, setSessionInfo] = useState({
    validUntil: null,
    hoursRemaining: 23,
    minutesRemaining: 59,
    sessionStatus: 'valid' // valid, expiring_soon, expired
  });
  
  const [tokenInput, setTokenInput] = useState('');
  const [loading, setLoading] = useState(false);

  // Backend API calls
  const handleOAuthLogin = async () => {
    try {
      setLoading(true);
      const response = await axios.post('/api/v1/auth/login');
      window.location.href = response.data.oauth_url;
    } catch (error) {
      onError('OAuth login failed');
    }
  };

  const handleManualTokenLogin = async () => {
    try {
      setLoading(true);
      const response = await axios.post('/api/v1/auth/manual-token-generation', {
        access_token: tokenInput
      });
      
      if (response.status === 200) {
        // Store token in secure storage
        localStorage.setItem('access_token', response.data.access_token);
        onLoginSuccess(response.data.access_token);
      }
    } catch (error) {
      onError('Invalid token. Please refresh and try again');
    } finally {
      setLoading(false);
    }
  };

  const checkSessionValidity = async (token: string) => {
    try {
      const response = await axios.get('/api/v1/auth/session-status', {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      setSessionInfo({
        validUntil: response.data.valid_until,
        hoursRemaining: response.data.hours_remaining,
        minutesRemaining: response.data.minutes_remaining,
        sessionStatus: response.data.hours_remaining > 4 ? 'valid' : 'expiring_soon'
      });
    } catch (error) {
      setSessionInfo({ ...sessionInfo, sessionStatus: 'expired' });
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <h1>Trading Platform</h1>
        
        {/* OAuth Login */}
        <button 
          onClick={handleOAuthLogin}
          disabled={loading}
          className="btn btn-primary btn-large"
        >
          🔐 Login with Upstox OAuth
        </button>
        
        <div className="divider">OR</div>
        
        {/* Manual Token Login */}
        <input 
          type="password"
          value={tokenInput}
          onChange={(e) => setTokenInput(e.target.value)}
          placeholder="Paste your access token"
          className="form-control"
        />
        
        <div className="session-validity">
          <p>Session Validity Window</p>
          <p className={`status-${sessionInfo.sessionStatus}`}>
            Valid: 3:15 AM - 2:30 AM IST ({sessionInfo.hoursRemaining}h {sessionInfo.minutesRemaining}m)
          </p>
        </div>
        
        <button 
          onClick={handleManualTokenLogin}
          disabled={loading || !tokenInput}
          className="btn btn-secondary"
        >
          Login
        </button>
      </div>
    </div>
  );
};

export default LoginPage;
```

#### **Backend API Response**

```json
{
  "auth/session-status": {
    "status": "success",
    "data": {
      "user_id": "USER123",
      "session_status": "active",
      "valid_until": "2025-12-13T02:30:00Z",
      "hours_remaining": 23,
      "minutes_remaining": 59,
      "session_window": {
        "start": "03:15 AM IST",
        "end": "02:30 AM IST (next day)",
        "window_length_hours": 23
      }
    }
  }
}
```

---

### **PAGE 2: DASHBOARD**

#### **Frontend Component**

```typescript
// Dashboard.tsx
import React, { useState, useEffect } from 'react';
import axios from 'axios';
import instrumentService from './services/InstrumentService';

const Dashboard: React.FC = () => {
  const [portfolioData, setPortfolioData] = useState({
    netWorth: 0,
    dailyPnL: 0,
    dailyPnLPercent: 0,
    marginUsed: 0,
    marginTotal: 0,
    buyingPower: 0
  });

  const [holdings, setHoldings] = useState([]);
  const [positions, setPositions] = useState([]);
  const [aiRecommendations, setAiRecommendations] = useState([]);
  const [recentOrders, setRecentOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const token = localStorage.getItem('access_token');
        
        // Parallel API calls
        const [profile, margins, holdings, positions, portfolio, aiRecs] = await Promise.all([
          axios.get('/api/v1/user/profile', { headers: { Authorization: `Bearer ${token}` } }),
          axios.get('/api/v1/user/margins', { headers: { Authorization: `Bearer ${token}` } }),
          axios.get('/api/v1/portfolio/holdings', { headers: { Authorization: `Bearer ${token}` } }),
          axios.get('/api/v1/portfolio/positions', { headers: { Authorization: `Bearer ${token}` } }),
          axios.get('/api/v1/portfolio/net-worth', { headers: { Authorization: `Bearer ${token}` } }),
          axios.post('/api/v1/ai/analyze-contract', { 
            instruments: ['NIFTY', 'BANKNIFTY'],
            analyze_type: 'top_opportunities'
          }, { headers: { Authorization: `Bearer ${token}` } })
        ]);

        // Process portfolio data
        const marginData = margins.data.data.equity;
        setPortfolioData({
          netWorth: portfolio.data.data.net_worth,
          dailyPnL: portfolio.data.data.pnl_day,
          dailyPnLPercent: (portfolio.data.data.pnl_day / portfolio.data.data.opening_balance) * 100,
          marginUsed: marginData.used,
          marginTotal: marginData.opening_balance,
          buyingPower: marginData.available
        });

        // Format holdings with instrument keys
        const formattedHoldings = holdings.data.data.map((h: any) => ({
          ...h,
          instrumentKey: instrumentService.generateInstrumentKey({
            segment: 'NSE',
            tradingsymbol: h.trading_symbol
          }),
          dayPnL: (h.last_price - h.average_price) * h.quantity,
          dayPnLPercent: ((h.last_price - h.average_price) / h.average_price) * 100
        }));

        setHoldings(formattedHoldings);
        setPositions(positions.data.data);
        setAiRecommendations(aiRecs.data.data.opportunities || []);

      } catch (error) {
        console.error('Dashboard fetch error:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  if (loading) return <div>Loading dashboard...</div>;

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1>Trading Platform</h1>
        <div className="session-timer">
          Session expires at 2:30 AM IST
        </div>
      </header>

      {/* Portfolio Summary Cards */}
      <section className="portfolio-summary">
        <div className="card metric-card">
          <h3>Net Worth</h3>
          <p className="metric-value">₹{portfolioData.netWorth.toLocaleString('en-IN')}</p>
          <p className={`metric-change ${portfolioData.dailyPnL >= 0 ? 'positive' : 'negative'}`}>
            {portfolioData.dailyPnL >= 0 ? '↑' : '↓'} ₹{Math.abs(portfolioData.dailyPnL).toLocaleString('en-IN')} 
            ({portfolioData.dailyPnLPercent.toFixed(2)}%)
          </p>
        </div>

        <div className="card metric-card">
          <h3>Daily P&L</h3>
          <p className={`metric-value ${portfolioData.dailyPnL >= 0 ? 'positive' : 'negative'}`}>
            {portfolioData.dailyPnL >= 0 ? '+' : ''}₹{portfolioData.dailyPnL.toLocaleString('en-IN')}
          </p>
          <div className="progress-bar">
            <div className="progress-fill" style={{width: `${(portfolioData.dailyPnLPercent + 5) * 10}%`}}></div>
          </div>
        </div>

        <div className="card metric-card">
          <h3>Margin Used</h3>
          <p className="metric-value">
            {((portfolioData.marginUsed / portfolioData.marginTotal) * 100).toFixed(1)}%
          </p>
          <p className="metric-subtitle">
            ₹{portfolioData.marginUsed.toLocaleString('en-IN')} / ₹{portfolioData.marginTotal.toLocaleString('en-IN')}
          </p>
        </div>
      </section>

      {/* Quick Actions */}
      <section className="quick-actions">
        <button className="btn btn-primary">➕ New Order</button>
        <button className="btn btn-primary">✨ AI Strategy</button>
        <button className="btn btn-secondary">📊 Analytics</button>
        <button className="btn btn-secondary">⚡ Alerts</button>
      </section>

      {/* Holdings Table */}
      <section className="holdings-section">
        <h2>Top Holdings</h2>
        <table className="holdings-table">
          <thead>
            <tr>
              <th>Symbol</th>
              <th>Qty</th>
              <th>Avg Price</th>
              <th>Current</th>
              <th>Day P&L</th>
              <th>%Change</th>
            </tr>
          </thead>
          <tbody>
            {holdings.slice(0, 5).map((holding, idx) => (
              <tr key={idx} className={holding.dayPnL >= 0 ? 'profit-row' : 'loss-row'}>
                <td><strong>{holding.trading_symbol}</strong></td>
                <td>{holding.quantity}</td>
                <td>₹{holding.average_price.toFixed(2)}</td>
                <td>₹{holding.last_price.toFixed(2)}</td>
                <td className={holding.dayPnL >= 0 ? 'positive' : 'negative'}>
                  {holding.dayPnL >= 0 ? '+' : ''}₹{holding.dayPnL.toFixed(2)}
                </td>
                <td>{holding.dayPnLPercent.toFixed(2)}%</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      {/* AI Recommendations */}
      <section className="ai-recommendations">
        <div className="card ai-card">
          <h3>✨ AI Generated Opportunity</h3>
          {aiRecommendations[0] && (
            <div>
              <p className="strategy-name">{aiRecommendations[0].strategy_name}</p>
              <p className="strategy-desc">{aiRecommendations[0].description}</p>
              <div className="metrics">
                <span>Est. P&L: ₹{aiRecommendations[0].estimated_pnl.toLocaleString('en-IN')}</span>
                <span>Confidence: {aiRecommendations[0].confidence_percent}%</span>
              </div>
              <button className="btn btn-primary">Execute Now</button>
            </div>
          )}
        </div>
      </section>
    </div>
  );
};

export default Dashboard;
```

#### **Backend API Response**

```json
{
  "portfolio/net-worth": {
    "status": "success",
    "data": {
      "net_worth": 15450000,
      "pnl_day": 12500,
      "pnl_total": 245000,
      "opening_balance": 15000000,
      "equity_value": 14500000,
      "derivatives_value": 950000
    }
  },
  "ai/analyze-contract": {
    "status": "success",
    "data": {
      "opportunities": [
        {
          "strategy_name": "NIFTY 50 Call Spread",
          "description": "Sell 19500 Call, Buy 19600 Call",
          "estimated_pnl": 2500,
          "confidence_percent": 78,
          "win_rate": 0.72,
          "max_profit": 2500,
          "max_loss": 1000,
          "breakeven": 19498.5
        }
      ]
    }
  }
}
```

---

### **PAGE 3: DERIVATIVES STRATEGY BUILDER**

#### **Frontend Component**

```typescript
// DerivativesStrategyBuilder.tsx
import React, { useState, useEffect } from 'react';
import axios from 'axios';
import instrumentService from './services/InstrumentService';

interface StrategyLeg {
  action: 'BUY' | 'SELL';
  instrumentKey: string;
  strike: number;
  optionType: 'CE' | 'PE';
  quantity: number;
  premium: number;
}

const DerivativesStrategyBuilder: React.FC = () => {
  const [selectedUnderlying, setSelectedUnderlying] = useState('NIFTY');
  const [selectedExpiry, setSelectedExpiry] = useState('');
  const [currentPrice, setCurrentPrice] = useState(19500);
  const [availableStrikes, setAvailableStrikes] = useState<number[]>([]);
  const [availableExpiryDates, setAvailableExpiryDates] = useState<string[]>([]);
  const [strategyLegs, setStrategyLegs] = useState<StrategyLeg[]>([]);
  const [greeksData, setGreeksData] = useState({});
  const [allInstruments, setAllInstruments] = useState([]);

  useEffect(() => {
    const fetchInstruments = async () => {
      try {
        const token = localStorage.getItem('access_token');
        const response = await axios.get('/api/v1/instruments/all', {
          headers: { Authorization: `Bearer ${token}` },
          params: { exchange: 'NFO' }
        });

        setAllInstruments(response.data.data);

        // Get contracts for selected underlying
        const contracts = instrumentService.getContractsForUnderlying(
          response.data.data,
          selectedUnderlying
        );

        // Extract expiry dates
        setAvailableExpiryDates(contracts.expiryDates);

        // Get strikes for first available expiry
        if (contracts.expiryDates.length > 0) {
          const firstExpiry = contracts.expiryDates[0];
          setSelectedExpiry(firstExpiry);
          fetchStrikesForExpiry(firstExpiry);
        }
      } catch (error) {
        console.error('Fetch instruments error:', error);
      }
    };

    fetchInstruments();
  }, [selectedUnderlying]);

  const fetchStrikesForExpiry = async (expiry: string) => {
    try {
      const token = localStorage.getItem('access_token');
      const response = await axios.get('/api/v1/instruments/options-chain', {
        headers: { Authorization: `Bearer ${token}` },
        params: {
          underlying: selectedUnderlying,
          expiry_date: expiry
        }
      });

      const uniqueStrikes = [...new Set(response.data.data.map((opt: any) => opt.strike))];
      setAvailableStrikes(uniqueStrikes.sort((a, b) => a - b));
    } catch (error) {
      console.error('Fetch strikes error:', error);
    }
  };

  const addLegToStrategy = (strike: number, optionType: 'CE' | 'PE', action: 'BUY' | 'SELL') => {
    const instrument = allInstruments.find(
      inst => inst.strike === strike && 
               inst.option_type === optionType &&
               inst.expiry === selectedExpiry &&
               inst.underlying_key === selectedUnderlying
    );

    if (!instrument) return;

    const newLeg: StrategyLeg = {
      action,
      instrumentKey: instrumentService.generateInstrumentKey(instrument),
      strike,
      optionType,
      quantity: 1,
      premium: instrument.last_price
    };

    setStrategyLegs([...strategyLegs, newLeg]);
  };

  const calculateStrategyMetrics = async () => {
    try {
      const token = localStorage.getItem('access_token');
      const legDetails = strategyLegs.map(leg => ({
        instrument_key: leg.instrumentKey,
        quantity: leg.quantity,
        action: leg.action
      }));

      const response = await axios.post('/api/v1/ai/analyze-contract', {
        legs: legDetails,
        underlying: selectedUnderlying
      }, { headers: { Authorization: `Bearer ${token}` } });

      setGreeksData(response.data.data.greeks);
    } catch (error) {
      console.error('Calculate metrics error:', error);
    }
  };

  const generateAIStrategy = async () => {
    try {
      const token = localStorage.getItem('access_token');
      const response = await axios.post('/api/v1/ai/generate-strategy', {
        underlying: selectedUnderlying,
        expiry: selectedExpiry,
        current_price: currentPrice,
        strategy_type: 'bullish' // Could be bullish, bearish, neutral
      }, { headers: { Authorization: `Bearer ${token}` } });

      // Auto-populate legs with AI suggestions
      const aiLegs = response.data.data.suggested_legs;
      setStrategyLegs(aiLegs.map((leg: any) => ({
        ...leg,
        instrumentKey: instrumentService.generateInstrumentKey({
          underlying_key: selectedUnderlying,
          expiry: selectedExpiry,
          strike: leg.strike,
          option_type: leg.option_type,
          segment: 'NFO'
        })
      })));
    } catch (error) {
      console.error('AI strategy generation error:', error);
    }
  };

  const placeStrategyOrder = async () => {
    try {
      const token = localStorage.getItem('access_token');
      const orders = strategyLegs.map(leg => ({
        instrument_key: leg.instrumentKey,
        quantity: leg.quantity,
        transaction_type: leg.action,
        order_type: 'REGULAR',
        product: 'MIS'
      }));

      const response = await axios.post('/api/v1/orders/bulk', {
        orders: orders
      }, { headers: { Authorization: `Bearer ${token}` } });

      console.log('Orders placed:', response.data);
      // Show success message
    } catch (error) {
      console.error('Order placement error:', error);
    }
  };

  return (
    <div className="derivatives-strategy-builder">
      <div className="left-panel">
        {/* Chart Area */}
        <div className="price-ticker">
          <h2>{selectedUnderlying}</h2>
          <p className="price">₹{currentPrice.toFixed(2)}</p>
          <p className="change">↑ 150 points (+0.77%)</p>
        </div>

        {/* Chart will be rendered here with TradingView Lightweight Charts */}
        <div id="chart-container" style={{ height: '400px' }}></div>

        {/* Technical Indicators */}
        <div className="indicators">
          <div className="indicator">RSI: 65 (Overbought)</div>
          <div className="indicator">MACD: Positive</div>
          <div className="indicator">BB: Within bands</div>
        </div>

        {/* Greeks Display */}
        <div className="greeks-display">
          <div>Δ: {greeksData?.delta?.toFixed(3) || '0.00'}</div>
          <div>Γ: {greeksData?.gamma?.toFixed(4) || '0.00'}</div>
          <div>Θ: {greeksData?.theta?.toFixed(4) || '0.00'}</div>
          <div>Ν: {greeksData?.vega?.toFixed(4) || '0.00'}</div>
        </div>
      </div>

      <div className="right-panel">
        {/* Strike Selector */}
        <div className="strike-ladder">
          <h3>Strike Selection</h3>
          {availableStrikes.map(strike => (
            <div key={strike} className={`strike-row ${strike === currentPrice ? 'atm' : ''}`}>
              <span>{strike}</span>
              <button onClick={() => addLegToStrategy(strike, 'CE', 'SELL')}>SELL CALL</button>
              <button onClick={() => addLegToStrategy(strike, 'CE', 'BUY')}>BUY CALL</button>
            </div>
          ))}
        </div>

        {/* Strategy Summary */}
        <div className="strategy-summary">
          <h3>Strategy Legs</h3>
          {strategyLegs.map((leg, idx) => (
            <div key={idx} className="leg">
              <span className={leg.action === 'BUY' ? 'buy' : 'sell'}>
                {leg.action} {leg.strike} {leg.optionType}
              </span>
              <button onClick={() => setStrategyLegs(strategyLegs.filter((_, i) => i !== idx))}>×</button>
            </div>
          ))}
        </div>

        {/* P&L Projection */}
        <div className="pnl-projection">
          <p>Max Profit: ₹{greeksData?.max_profit || '0'}</p>
          <p>Max Loss: ₹{greeksData?.max_loss || '0'}</p>
          <p>Breakeven: {greeksData?.breakeven || '0'}</p>
        </div>

        {/* AI Generate Button */}
        <button onClick={generateAIStrategy} className="btn btn-gold">
          ✨ AI Generate Strategy
        </button>

        {/* Order Ticket */}
        <div className="order-ticket">
          <input type="number" placeholder="Quantity" defaultValue="1" />
          <input type="number" placeholder="Price" defaultValue={currentPrice} />
          <select>
            <option>DAY</option>
            <option>IOC</option>
            <option>FOK</option>
          </select>
          <select>
            <option>MIS</option>
            <option>NRML</option>
          </select>
          <p>Est. Margin: ₹5,000</p>
          <p>Est. P&L: +₹2,500</p>
          <button onClick={placeStrategyOrder} className="btn btn-primary">
            Place Order
          </button>
        </div>
      </div>
    </div>
  );
};

export default DerivativesStrategyBuilder;
```

#### **Backend API Response**

```json
{
  "instruments/options-chain": {
    "status": "success",
    "data": [
      {
        "instrument_token": "3045_19DEC25_19200_CE",
        "tradingsymbol": "NIFTY1925C19200",
        "strike": 19200,
        "option_type": "CE",
        "expiry": "2025-12-25",
        "last_price": 320.50,
        "bid": 320.00,
        "ask": 321.00,
        "bid_qty": 5000,
        "ask_qty": 5500,
        "iv": 17.2,
        "delta": 0.80,
        "gamma": 0.01,
        "theta": 0.05,
        "vega": 0.12,
        "oi": 5000000,
        "volume": 1200000,
        "underlying_key": "NIFTY"
      }
    ]
  },
  "ai/generate-strategy": {
    "status": "success",
    "data": {
      "strategy_name": "NIFTY Call Spread",
      "strategy_type": "bullish",
      "suggested_legs": [
        {
          "action": "SELL",
          "strike": 19500,
          "option_type": "CE",
          "quantity": 1
        },
        {
          "action": "BUY",
          "strike": 19600,
          "option_type": "CE",
          "quantity": 1
        }
      ],
      "greeks": {
        "delta": 0.45,
        "gamma": 0.02,
        "theta": 0.15,
        "vega": 0.35,
        "max_profit": 2500,
        "max_loss": 1000,
        "breakeven": 19498.5
      },
      "confidence_percent": 78
    }
  }
}
```

---

### **PAGE 4: STOCK ANALYSIS**

#### **Frontend Component**

```typescript
// StockAnalysis.tsx
import React, { useState, useEffect } from 'react';
import axios from 'axios';
import instrumentService from './services/InstrumentService';

const StockAnalysis: React.FC = () => {
  const [selectedStock, setSelectedStock] = useState('SBIN');
  const [stockData, setStockData] = useState<any>(null);
  const [chartData, setChartData] = useState([]);
  const [technicalAnalysis, setTechnicalAnalysis] = useState<any>(null);
  const [aiInsights, setAiInsights] = useState<any>(null);
  const [orderQuantity, setOrderQuantity] = useState(1);
  const [orderPrice, setOrderPrice] = useState(0);

  useEffect(() => {
    const fetchStockData = async () => {
      try {
        const token = localStorage.getItem('access_token');
        
        // Get stock quote
        const quoteResponse = await axios.get('/api/v1/market/quote/' + selectedStock, {
          headers: { Authorization: `Bearer ${token}` },
          params: { mode: 'FULL' }
        });
        
        setStockData(quoteResponse.data.data);
        setOrderPrice(quoteResponse.data.data.last_price);

        // Get historical data for chart
        const historyResponse = await axios.get('/api/v1/market/historical', {
          headers: { Authorization: `Bearer ${token}` },
          params: {
            symbol: selectedStock,
            interval: '15minute',
            from_date: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            to_date: new Date().toISOString().split('T')[0]
          }
        });

        setChartData(historyResponse.data.data);

        // Get technical indicators
        const technicalResponse = await axios.post('/api/v1/indicators/calculate', {
          symbol: selectedStock,
          indicators: ['RSI', 'MACD', 'BB'],
          period: 14
        }, { headers: { Authorization: `Bearer ${token}` } });

        setTechnicalAnalysis(technicalResponse.data.data);

        // Get AI insights
        const aiResponse = await axios.post('/api/v1/ai/analyze-contract', {
          symbol: selectedStock,
          analyze_type: 'stock_analysis'
        }, { headers: { Authorization: `Bearer ${token}` } });

        setAiInsights(aiResponse.data.data);

      } catch (error) {
        console.error('Fetch stock data error:', error);
      }
    };

    fetchStockData();
  }, [selectedStock]);

  const handleBuyOrder = async () => {
    try {
      const token = localStorage.getItem('access_token');
      const response = await axios.post('/api/v1/orders/place', {
        symbol: selectedStock,
        quantity: orderQuantity,
        price: orderPrice,
        transaction_type: 'BUY',
        order_type: 'LIMIT',
        product: 'CNC', // Delivery
        validity: 'DAY'
      }, { headers: { Authorization: `Bearer ${token}` } });

      console.log('Order placed:', response.data);
    } catch (error) {
      console.error('Order placement error:', error);
    }
  };

  if (!stockData) return <div>Loading stock data...</div>;

  return (
    <div className="stock-analysis">
      {/* Header */}
      <div className="stock-header">
        <select value={selectedStock} onChange={(e) => setSelectedStock(e.target.value)}>
          <option>SBIN</option>
          <option>TCS</option>
          <option>INFY</option>
          <option>HDFC</option>
        </select>
      </div>

      {/* Stock Info Card */}
      <div className="stock-info-card">
        <h2>{stockData.name}</h2>
        <p className="stock-symbol">{selectedStock} - NSE</p>
        <p className={`stock-price ${stockData.change >= 0 ? 'positive' : 'negative'}`}>
          ₹{stockData.last_price.toFixed(2)}
        </p>
        <p className="stock-change">
          {stockData.change >= 0 ? '↑' : '↓'} ₹{Math.abs(stockData.change).toFixed(2)} 
          ({stockData.change_percent.toFixed(2)}%)
        </p>
      </div>

      {/* Chart */}
      <div className="chart-container">
        {/* TradingView Lightweight Charts will render here */}
        <div id="stock-chart" style={{ height: '400px' }}></div>
      </div>

      {/* Key Metrics */}
      <div className="key-metrics">
        <div className="metric">
          <label>Open</label>
          <value>{stockData.ohlc.open.toFixed(2)}</value>
        </div>
        <div className="metric">
          <label>High</label>
          <value>{stockData.ohlc.high.toFixed(2)}</value>
        </div>
        <div className="metric">
          <label>Low</label>
          <value>{stockData.ohlc.low.toFixed(2)}</value>
        </div>
        <div className="metric">
          <label>Volume</label>
          <value>{(stockData.volume / 1000000).toFixed(1)}M</value>
        </div>
      </div>

      {/* Technical Analysis */}
      <div className="technical-analysis">
        <h3>Technical Analysis</h3>
        {technicalAnalysis && (
          <div>
            <p>RSI: {technicalAnalysis.rsi.toFixed(2)} ({technicalAnalysis.rsi > 70 ? 'Overbought' : 'Normal'})</p>
            <p>MACD: {technicalAnalysis.macd > 0 ? 'Positive' : 'Negative'}</p>
            <p>Bollinger Bands: {technicalAnalysis.bb_signal}</p>
          </div>
        )}
        <p className="recommendation">
          Recommendation: {aiInsights?.recommendation || 'HOLD'} ⭐⭐⭐
        </p>
      </div>

      {/* AI Insights */}
      <div className="ai-insights">
        <h3>✨ AI Insights</h3>
        {aiInsights && (
          <div>
            <p>{aiInsights.analysis_text}</p>
            <p>Support: ₹{aiInsights.support_level.toFixed(2)}</p>
            <p>Resistance: ₹{aiInsights.resistance_level.toFixed(2)}</p>
          </div>
        )}
      </div>

      {/* Order Placement */}
      <div className="order-section">
        <h3>Order Placement</h3>
        <label>
          <input type="radio" name="order-type" defaultChecked /> Delivery
          <input type="radio" name="order-type" /> Intraday
        </label>
        <input 
          type="number" 
          placeholder="Quantity" 
          value={orderQuantity}
          onChange={(e) => setOrderQuantity(parseInt(e.target.value))}
          min="1"
          max="100"
        />
        <input 
          type="number" 
          placeholder="Price" 
          value={orderPrice}
          onChange={(e) => setOrderPrice(parseFloat(e.target.value))}
        />
        <button onClick={handleBuyOrder} className="btn btn-buy">
          Buy @ ₹{orderPrice.toFixed(2)}
        </button>
        <button className="btn btn-sell">Sell</button>
      </div>
    </div>
  );
};

export default StockAnalysis;
```

---

## PART 3: INSTRUMENT KEY MANAGEMENT BEST PRACTICES

### **1. Naming Conventions**

```typescript
// Global Instrument Key Standards

const INSTRUMENT_KEY_PATTERNS = {
  // Equity Stocks: EQ_SYMBOL
  // Examples: EQ_SBIN, EQ_TCS, EQ_INFY
  EQUITY: /^EQ_[A-Z]+$/,
  
  // Index Futures: NFO_UNDERLYING_EXPIRY_FUT
  // Examples: NFO_NIFTY_25DEC_FUT, NFO_BANKNIFTY_25DEC_FUT
  FUTURES_INDEX: /^NFO_[A-Z]+_\d{2}[A-Z]{3}_FUT$/,
  
  // Stock Futures: NFO_SYMBOL_EXPIRY_FUT
  // Examples: NFO_SBIN_25DEC_FUT
  FUTURES_STOCK: /^NFO_[A-Z]+_\d{2}[A-Z]{3}_FUT$/,
  
  // Index Options: NFO_UNDERLYING_EXPIRY_STRIKE_TYPE
  // Examples: NFO_NIFTY_25DEC_19500_CE, NFO_NIFTY_25DEC_19500_PE
  OPTIONS_INDEX: /^NFO_[A-Z]+_\d{2}[A-Z]{3}_\d+_[CP]E$/,
  
  // Stock Options: NFO_SYMBOL_EXPIRY_STRIKE_TYPE
  // Examples: NFO_SBIN_25DEC_525_CE
  OPTIONS_STOCK: /^NFO_[A-Z]+_\d{2}[A-Z]{3}_\d+_[CP]E$/
};
```

### **2. Instrument Key Usage Examples**

```typescript
// Dashboard: Display holdings
const holdingsByKey: Map<string, Holding> = new Map();
holdings.forEach(h => {
  const key = `EQ_${h.trading_symbol}`;
  holdingsByKey.set(key, h);
});

// Search: Find instrument by key
function findInstrumentByKey(key: string, instruments: any[]): any {
  return instruments.find(inst => 
    instrumentService.generateInstrumentKey(inst) === key
  );
}

// Caching: Use key as cache identifier
const instrumentCache: Map<string, any> = new Map();
function getCachedQuote(key: string, TTL = 60000): any {
  const cached = instrumentCache.get(key);
  if (cached && Date.now() - cached.timestamp < TTL) {
    return cached.data;
  }
  return null;
}

// Sorting: Order strategies by instrument key
const strategies = [
  { key: 'NFO_NIFTY_25DEC_19500_CE', pnl: 2500 },
  { key: 'NFO_NIFTY_25DEC_19500_PE', pnl: -1000 },
  { key: 'NFO_BANKNIFTY_25DEC_46000_CE', pnl: 1500 }
];
strategies.sort((a, b) => a.key.localeCompare(b.key));
```

---

## PART 4: API INTEGRATION PATTERNS

### **1. Error Handling & Retry Logic**

```typescript
async function callWithRetry(
  apiCall: () => Promise<any>,
  maxRetries = 3,
  backoffMs = 1000
): Promise<any> {
  let lastError;
  
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await apiCall();
    } catch (error) {
      lastError = error;
      
      if (error.response?.status === 429) {
        // Rate limit: wait longer
        await new Promise(resolve => setTimeout(resolve, backoffMs * Math.pow(2, i)));
      } else if (error.response?.status >= 500) {
        // Server error: retry with backoff
        await new Promise(resolve => setTimeout(resolve, backoffMs * Math.pow(2, i)));
      } else {
        // Client error: don't retry
        throw error;
      }
    }
  }
  
  throw lastError;
}
```

### **2. WebSocket Real-time Data**

```typescript
class MarketDataService {
  private ws: WebSocket;
  private subscriptions: Map<string, (data: any) => void> = new Map();

  connect(token: string) {
    this.ws = new WebSocket('wss://ws.upstox.com/feed');
    
    this.ws.onopen = () => {
      this.ws.send(JSON.stringify({
        guid: `client-${Date.now()}`,
        method: 'sub',
        data: {
          mode: 'quote',
          instrumentTokens: Array.from(this.subscriptions.keys())
        }
      }));
    };

    this.ws.onmessage = (event) => {
      const data = JSON.parse(event.data);
      const callback = this.subscriptions.get(data.data.instrument_token);
      if (callback) callback(data.data);
    };
  }

  subscribe(instrumentKey: string, callback: (data: any) => void) {
    this.subscriptions.set(instrumentKey, callback);
  }

  unsubscribe(instrumentKey: string) {
    this.subscriptions.delete(instrumentKey);
  }
}
```

---

## PART 5: STATE MANAGEMENT

```typescript
// Redux Store Example
interface TradingState {
  user: {
    profile: any;
    margins: any;
    sessionStatus: 'active' | 'expiring' | 'expired';
  };
  instruments: {
    allInstruments: any[];
    selectedUnderlying: string;
    selectedExpiry: string;
    priceCache: Map<string, any>;
  };
  portfolio: {
    holdings: any[];
    positions: any[];
    orders: any[];
    strategies: any[];
  };
  ui: {
    loading: boolean;
    error: string | null;
    selectedPage: string;
  };
}

const initialState: TradingState = {
  user: {
    profile: null,
    margins: null,
    sessionStatus: 'active'
  },
  instruments: {
    allInstruments: [],
    selectedUnderlying: 'NIFTY',
    selectedExpiry: '',
    priceCache: new Map()
  },
  portfolio: {
    holdings: [],
    positions: [],
    orders: [],
    strategies: []
  },
  ui: {
    loading: false,
    error: null,
    selectedPage: 'dashboard'
  }
};
```

---

**Last Updated**: December 12, 2025
**Status**: Complete Frontend Design with Backend Integration Ready

