# Expired Instruments - Flow & Sequence Diagrams

**Based on:** c1.md specification

---

## 1. High-Level Architecture

```mermaid
flowchart TB
    subgraph Clients
        A[Frontend / Vite+React]
        B[CLI]
        C[REST API / curl]
    end
    
    subgraph Backend
        D[UserSettingsService]
        E[SectorService]
        F[ExpiredInstrumentFetcher]
        G[ExpiredInstrumentServiceImpl]
        H[HistoricalMarketDataServiceImpl]
    end
    
    subgraph Storage
        I[(Cache Layer)]
        J[(Database)]
    end
    
    A --> D
    B --> D
    C --> D
    
    D --> E
    E --> F
    F --> G
    F --> H
    G --> I
    H --> J
```

---

## 2. Single Instrument Request Flow

```mermaid
sequenceDiagram
    participant User
    participant REST as REST API
    participant Settings as UserSettingsService
    participant Fetcher as ExpiredInstrumentFetcher
    participant Expired as ExpiredInstrumentServiceImpl
    participant Market as HistoricalMarketDataServiceImpl
    participant Cache
    
    User->>REST: GET /expired/data?underlying=NSE_INDEX|Nifty50
    REST->>Settings: getSettings(userId)
    Settings-->>REST: ExpiredFetchSettings
    
    REST->>Fetcher: fetchWithUserSettings(userId, underlyingKey)
    Fetcher->>Expired: fetchExpiries(underlyingKey)
    
    alt Cache Hit
        Expired->>Cache: get(underlyingKey)
        Cache-->>Expired: cachedExpiries
    else Cache Miss
        Expired->>Expired: API call (pending)
        Expired->>Cache: cache(expiries, 24h TTL)
    end
    
    Expired-->>Fetcher: List<LocalDate> expiries
    Fetcher->>Fetcher: filter by maxHistoricalDays
    Fetcher->>Fetcher: apply "latest" or "all"
    
    loop Each Expiry
        Fetcher->>Expired: fetchExpiredOptions(key, expiry)
        Fetcher->>Expired: fetchExpiredFutures(key, expiry)
    end
    
    Fetcher->>Market: fetchExpiredHistoricalCandles()
    Market-->>Fetcher: List<Candle>
    
    Fetcher-->>REST: List<Candle>
    REST-->>User: JSON Response
```

---

## 3. Multi-Sector Batch Request

```mermaid
sequenceDiagram
    participant User
    participant REST as REST API
    participant Settings as UserSettingsService
    participant Sector as SectorService
    participant Fetcher as ExpiredInstrumentFetcher
    participant Cache
    
    User->>REST: GET /expired/data?sectors=IT,BANKING
    REST->>Settings: getSettings(userId)
    Settings-->>REST: ExpiredFetchSettings + sectorFilter
    
    REST->>Sector: getInstrumentKeysBySector("IT")
    Sector-->>REST: List<String> itInstruments
    
    REST->>Sector: getInstrumentKeysBySector("BANKING")
    Sector-->>REST: List<String> bankInstruments
    
    loop Each Instrument
        REST->>Fetcher: fetchWithUserSettings(userId, instrumentKey)
        Fetcher-->>REST: List<Candle>
    end
    
    REST-->>User: Aggregated JSON Response
```

---

## 4. REST API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/expired/expiries` | GET | Get expiry dates for underlying |
| `/api/v1/expired/options` | GET | Get expired option contracts |
| `/api/v1/expired/futures` | GET | Get expired future contracts |
| `/api/v1/expired/candles` | GET | Get historical candles |
| `/api/v1/expired/data` | GET | Full fetch with user settings |
| `/api/v1/expired/intervals` | GET | Get supported intervals |
| `/api/v1/expired/validate` | GET | Validate key format |

---

## 5. User Settings Integration

| Setting | Effect |
|---------|--------|
| `expired.default_expiry_fetch` | "latest" = most recent, "all" = full history |
| `expired.default_instrument_type` | "options", "futures", or "both" |
| `expired.default_interval` | Candle interval (1min, 5min, day) |
| `expired.max_historical_days` | Limit history depth |
| `expired.show_weekly_options` | Include/exclude weekly expiries |
| `expired.auto_cache_expiries` | Enable 24h expiry caching |

---

## 6. Caching Strategy

```mermaid
flowchart LR
    A[API Request] --> B{Cache Check}
    B -->|Hit| C[Return Cached]
    B -->|Miss| D[Fetch from Upstox]
    D --> E[Store in Cache]
    E --> F[Return Fresh Data]
    
    subgraph Cache TTL
        G[Expiries: 24h]
        H[Contracts: 12h]
        I[Candles: Optional]
    end
```

---

*Generated: 2025-12-30*
