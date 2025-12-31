# Option Chain Module - Sequence Diagrams

**Version:** 1.0  
**Last Updated:** 2025-12-30

---

## 1. Option Chain Fetch Flow

```mermaid
sequenceDiagram
    actor User
    participant FE as Frontend (React/CLI)
    participant SVC as OptionChainService
    participant CACHE as Cache Layer
    participant API as Upstox API
    participant LOG as Audit Log DB

    User->>FE: Request option chain (symbol, expiry)
    FE->>SVC: GET /option-chain?symbol=X&expiry=Y
    
    SVC->>CACHE: Check cache(symbol, expiry)
    
    alt Cache Hit
        CACHE-->>SVC: Return cached data
        SVC->>LOG: Log CACHE_HIT event
    else Cache Miss
        SVC->>API: GET /v2/option/chain (Bearer token)
        API-->>SVC: Return option chain JSON
        SVC->>CACHE: Store in cache (TTL: 60min)
        SVC->>LOG: Log API_FETCH event
    end
    
    SVC-->>FE: Return OptionChainResponse
    FE-->>User: Render option chain table
```

---

## 2. Admin Fetch Flow

```mermaid
sequenceDiagram
    actor Admin
    participant UI as Admin UI
    participant SVC as OptionChainService
    participant BROKER as BrokerAdapter
    participant CACHE as Cache Layer
    participant AUDIT as Audit Log

    Admin->>UI: Request Option Chain
    UI->>SVC: fetchOptionChain(symbol, expiry, userId)
    
    SVC->>CACHE: get(cacheKey)
    
    alt Cache Hit
        CACHE-->>SVC: cachedResponse
        SVC->>AUDIT: log(CACHE_HIT)
    else Cache Miss
        SVC->>BROKER: fetch(symbol, expiry)
        BROKER-->>SVC: response
        SVC->>CACHE: put(cacheKey, response)
        SVC->>AUDIT: log(API_FETCH)
    end
    
    SVC-->>UI: return OptionChainResponse
    UI-->>Admin: display data
```

---

## 3. T-1 Pre-warm Scheduler

```mermaid
sequenceDiagram
    participant SCHED as Scheduler (6:30 PM)
    participant SVC as OptionChainService
    participant DB as Instrument DB
    participant API as Upstox API
    participant CACHE as Cache Layer

    SCHED->>DB: Get instruments with T+1 expiry
    DB-->>SCHED: List of (symbol, expiry)
    
    loop Each Instrument
        SCHED->>SVC: prewarmOptionChain(symbol, expiry)
        SVC->>API: GET /v2/option/chain
        API-->>SVC: Option chain data
        SVC->>CACHE: Store in cache
    end
    
    SCHED->>SCHED: Log prewarm summary
```

---

## 4. Token Rotation Flow

```mermaid
sequenceDiagram
    participant SVC as OptionChainService
    participant TM as TokenManager
    participant API as Upstox API

    SVC->>TM: getActiveToken()
    TM-->>SVC: OPTIONCHAIN1
    
    SVC->>API: Request with OPTIONCHAIN1
    
    alt Rate Limit Hit (429)
        API-->>SVC: 429 Too Many Requests
        SVC->>TM: rotateToken()
        TM-->>SVC: OPTIONCHAIN2
        SVC->>API: Retry with OPTIONCHAIN2
        API-->>SVC: Success
    else Success
        API-->>SVC: Option chain data
    end
```

---

## 5. Settings Resolution Flow

```mermaid
flowchart TB
    RR[Regulatory Rules] --> SR[Settings Resolver]
    ER[Exchange Rules] --> SR
    SD[System Defaults] --> SR
    UP[User Preferences] --> SR
    SO[Session Overrides] --> SR
    
    SR --> RMS[RMS Engine]
    RMS --> OCS[OptionChainService]
```

---

*Document Status: FINALIZED*
