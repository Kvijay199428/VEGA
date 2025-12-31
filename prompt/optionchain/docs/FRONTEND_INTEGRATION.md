# Option Chain - Frontend Integration Guide

**Version:** 1.0  
**Last Updated:** 2025-12-30

---

## 1. API Endpoint

```
GET /api/v1/option-chain?symbol={symbol}&expiry={expiry}
```

### Parameters

| Parameter | Required | Example |
|-----------|----------|---------|
| `symbol` | Yes | `NSE_INDEX|Nifty 50` |
| `expiry` | Yes | `2025-01-30` |

### Response

```json
{
  "status": "success",
  "data": [
    {
      "strike_price": 24000,
      "expiry": "2025-01-30",
      "pcr": 0.85,
      "underlying_spot_price": 24150.25,
      "call_options": {
        "market_data": { "ltp": 150.5, "oi": 5000000, "volume": 100000 },
        "option_greeks": { "iv": 15.2, "delta": 0.55, "theta": -5.2 }
      },
      "put_options": {
        "market_data": { "ltp": 80.25, "oi": 4200000, "volume": 85000 },
        "option_greeks": { "iv": 14.8, "delta": -0.45, "theta": -4.8 }
      }
    }
  ]
}
```

---

## 2. React/Vite Integration

### OptionChainTable Component

```jsx
import { useState, useEffect } from "react";
import axios from "axios";

export default function OptionChainTable({ symbol, expiry }) {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function fetchOptionChain() {
      setLoading(true);
      try {
        const res = await axios.get(`/api/v1/option-chain`, {
          params: { symbol, expiry }
        });
        setData(res.data.data);
        setError(null);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    }
    fetchOptionChain();
  }, [symbol, expiry]);

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <table className="option-chain-table">
      <thead>
        <tr>
          <th>Call OI</th>
          <th>Call IV</th>
          <th>Call LTP</th>
          <th>Strike</th>
          <th>Put LTP</th>
          <th>Put IV</th>
          <th>Put OI</th>
          <th>PCR</th>
        </tr>
      </thead>
      <tbody>
        {data.map((row) => (
          <tr key={row.strike_price}>
            <td>{row.call_options.market_data.oi.toLocaleString()}</td>
            <td>{row.call_options.option_greeks.iv.toFixed(2)}%</td>
            <td>{row.call_options.market_data.ltp.toFixed(2)}</td>
            <td className="strike">{row.strike_price}</td>
            <td>{row.put_options.market_data.ltp.toFixed(2)}</td>
            <td>{row.put_options.option_greeks.iv.toFixed(2)}%</td>
            <td>{row.put_options.market_data.oi.toLocaleString()}</td>
            <td>{row.pcr.toFixed(2)}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
```

### CSS Styling

```css
.option-chain-table {
  width: 100%;
  border-collapse: collapse;
  font-family: 'Inter', sans-serif;
}

.option-chain-table th {
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  color: #fff;
  padding: 12px;
  text-align: center;
}

.option-chain-table td {
  padding: 10px;
  border-bottom: 1px solid #eee;
  text-align: center;
}

.option-chain-table .strike {
  background: #f0f0f0;
  font-weight: bold;
}

/* ITM/OTM coloring */
.option-chain-table tr.itm-call td:nth-child(-n+3) { background: #e8f5e9; }
.option-chain-table tr.itm-put td:nth-last-child(-n+3) { background: #ffebee; }
```

---

## 3. CLI Usage (Node.js)

```javascript
import axios from "axios";

const symbol = "NSE_INDEX|Nifty 50";
const expiry = "2025-01-30";

async function fetchOptionChain() {
  try {
    const res = await axios.get("http://localhost:28020/api/v1/option-chain", {
      params: { symbol, expiry }
    });
    
    console.table(res.data.data.map(d => ({
      strike: d.strike_price,
      callLTP: d.call_options.market_data.ltp,
      callOI: d.call_options.market_data.oi,
      callIV: d.call_options.option_greeks.iv,
      putLTP: d.put_options.market_data.ltp,
      putOI: d.put_options.market_data.oi,
      putIV: d.put_options.option_greeks.iv,
      pcr: d.pcr
    })));
  } catch (err) {
    console.error("Error:", err.message);
  }
}

fetchOptionChain();
```

---

## 4. Column Mapping

| UI Column | Response Path |
|-----------|---------------|
| Strike Price | `strike_price` |
| Expiry | `expiry` |
| Call LTP | `call_options.market_data.ltp` |
| Call OI | `call_options.market_data.oi` |
| Call IV | `call_options.option_greeks.iv` |
| Call Delta | `call_options.option_greeks.delta` |
| Put LTP | `put_options.market_data.ltp` |
| Put OI | `put_options.market_data.oi` |
| Put IV | `put_options.option_greeks.iv` |
| Put Delta | `put_options.option_greeks.delta` |
| PCR | `pcr` |
| Spot Price | `underlying_spot_price` |

---

## 5. Error Handling

| Status Code | Meaning | Frontend Action |
|-------------|---------|-----------------|
| 200 | Success | Display data |
| 400 | Invalid params | Show validation error |
| 404 | No data found | Show "No option chain available" |
| 429 | Rate limited | Show "Too many requests, retry later" |
| 500 | Server error | Show generic error, enable retry |

---

## 6. Features to Implement

- [ ] ITM/OTM strike highlighting
- [ ] Sorting by strike/OI/IV
- [ ] Filter by strike range
- [ ] Auto-refresh (configurable)
- [ ] Export to CSV/Excel
- [ ] Greeks display toggle

---

*Document Status: FINALIZED*
