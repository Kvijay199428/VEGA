# Frontend Integration Guide

## 1. Overview

The auth system provides REST endpoints for frontend integration.  
All endpoints return JSON. No authentication required for auth management endpoints.

---

## 2. Endpoints for Frontend

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/auth/upstox/tokens/status` | GET | Get all token statuses |
| `/api/auth/upstox/tokens/generate` | POST | Trigger token generation |
| `/api/auth/upstox/login-success` | GET | Get all login success info |
| `/api/auth/upstox/login-success/{api}` | GET | Get specific API success |

---

## 3. React/Vite Integration

### 3.1 Token Status Component

```tsx
// TokenStatus.tsx
import { useEffect, useState } from 'react';

interface TokenSummary {
  valid: number;
  invalid: number;
  missing: number;
  total: number;
}

interface TokenInfo {
  apiName: string;
  status: 'VALID' | 'EXPIRED' | 'INVALID' | 'MISSING';
  validUntil?: string;
  userId?: string;
}

export function TokenStatus() {
  const [summary, setSummary] = useState<TokenSummary | null>(null);
  const [tokens, setTokens] = useState<TokenInfo[]>([]);

  useEffect(() => {
    fetch('/api/auth/upstox/tokens/status')
      .then(res => res.json())
      .then(data => {
        setSummary(data.summary);
        setTokens(data.tokens);
      });
  }, []);

  return (
    <div className="token-status">
      <h2>Token Status</h2>
      
      {summary && (
        <div className="summary">
          <span className="valid">✓ {summary.valid} Valid</span>
          <span className="invalid">✗ {summary.invalid} Invalid</span>
          <span className="missing">○ {summary.missing} Missing</span>
        </div>
      )}
      
      <table>
        <thead>
          <tr>
            <th>API</th>
            <th>Status</th>
            <th>Valid Until</th>
          </tr>
        </thead>
        <tbody>
          {tokens.map(token => (
            <tr key={token.apiName}>
              <td>{token.apiName}</td>
              <td className={token.status.toLowerCase()}>{token.status}</td>
              <td>{token.validUntil || '-'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
```

---

### 3.2 Login Success Page

```tsx
// LoginSuccess.tsx
import { useEffect, useState } from 'react';

interface ProfileView {
  userId: string;
  userName: string;
  email: string;
  broker: string;
  exchanges: string[];
  products: string[];
  orderTypes: string[];
  poa: boolean;
  ddpi: boolean;
  isActive: boolean;
}

interface LoginSuccessResponse {
  tokenInserted: boolean;
  apiName: string;
  tokenExpiry: string;
  message: string;
  profile: ProfileView;
}

export function LoginSuccess({ apiName }: { apiName: string }) {
  const [data, setData] = useState<LoginSuccessResponse | null>(null);

  useEffect(() => {
    fetch(`/api/auth/upstox/login-success/${apiName}`)
      .then(res => res.json())
      .then(setData);
  }, [apiName]);

  if (!data) return <div>Loading...</div>;
  if (!data.tokenInserted) return <div>Token not found</div>;

  return (
    <div className="login-success">
      {/* Success Banner */}
      <div className="success-banner">
        <span className="icon">✔</span>
        <span>Access Token Generated & Stored Successfully</span>
      </div>

      {/* Token Info */}
      <div className="token-info">
        <h3>Token Information</h3>
        <p><strong>API:</strong> {data.apiName}</p>
        <p><strong>Expires:</strong> {data.tokenExpiry}</p>
        <p><strong>Status:</strong> <span className="badge">ACTIVE</span></p>
      </div>

      {/* Profile Info */}
      {data.profile && (
        <div className="profile-info">
          <h3>User Profile</h3>
          <p><strong>User ID:</strong> {data.profile.userId}</p>
          <p><strong>Name:</strong> {data.profile.userName}</p>
          <p><strong>Broker:</strong> {data.profile.broker}</p>
          <p><strong>Active:</strong> {data.profile.isActive ? '✅' : '❌'}</p>

          <h4>Exchanges</h4>
          <div className="chips">
            {data.profile.exchanges.map(ex => (
              <span key={ex} className="chip">{ex}</span>
            ))}
          </div>

          <h4>Products</h4>
          <div className="chips">
            {data.profile.products.map(p => (
              <span key={p} className="chip">{p}</span>
            ))}
          </div>

          <h4>Order Types</h4>
          <div className="chips">
            {data.profile.orderTypes.map(ot => (
              <span key={ot} className="chip">{ot}</span>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
```

---

### 3.3 CSS Styles

```css
/* auth.css */
.token-status .summary {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
}

.token-status .valid { color: #22c55e; }
.token-status .invalid { color: #ef4444; }
.token-status .missing { color: #f59e0b; }
.token-status .expired { color: #f59e0b; }

.login-success .success-banner {
  background: linear-gradient(135deg, #22c55e, #16a34a);
  color: white;
  padding: 1rem;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.login-success .badge {
  background: #22c55e;
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
}

.login-success .chip {
  background: #e0e7ff;
  color: #4f46e5;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.875rem;
  display: inline-block;
  margin: 0.25rem;
}

.login-success .chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}
```

---

## 4. API Fetch Wrapper

```typescript
// api/auth.ts
const API_BASE = '/api/auth/upstox';

export const authApi = {
  getTokenStatus: () =>
    fetch(`${API_BASE}/tokens/status`).then(r => r.json()),

  generateTokens: (mode: 'ALL' | 'INVALID_ONLY' | 'PARTIAL', apiNames?: string[]) =>
    fetch(`${API_BASE}/tokens/generate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ mode, apiNames: apiNames || [] })
    }).then(r => r.json()),

  getLoginSuccess: (apiName: string) =>
    fetch(`${API_BASE}/login-success/${apiName}`).then(r => r.json()),

  getAllLoginSuccess: () =>
    fetch(`${API_BASE}/login-success`).then(r => r.json())
};
```

---

## 5. CORS Configuration

If frontend runs on different port:

```java
// Spring Boot CORS
@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class TokenStatusController { ... }
```

---

**Document Status:** Final  
**Last Updated:** 2025-12-29
