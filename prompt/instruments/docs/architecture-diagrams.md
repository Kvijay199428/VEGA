# Instruments Module - Architecture Diagrams

## Complete System Architecture

```mermaid
flowchart TB
    subgraph DataSources["📥 Data Sources"]
        UPSTOX_API["Upstox API"]
        NSE_MASTER["NSE Master Files"]
        BSE_MASTER["BSE Master Files"]
        REGULATORY["Regulatory Lists<br/>PCA/ASM/GSM"]
    end

    subgraph Database["💾 SQLite Database"]
        direction TB
        INST_MASTER[("instrument_master<br/>V10")]
        OVERLAYS[("instrument_overlays<br/>V11")]
        RISK_PROFILE[("product_risk_profile<br/>V12")]
        SEC_TYPE[("equity_security_type<br/>V13")]
        EX_SERIES[("exchange_series<br/>V14-V16")]
        WATCHLIST[("regulatory_watchlist<br/>V17")]
        IPO_CAL[("ipo_calendar<br/>V18")]
        MARGIN[("intraday_margin<br/>V19")]
        QTY_CAP[("quantity_caps<br/>V20")]
        PRICE_BAND[("price_band<br/>V21")]
        FO_LIFE[("fo_lifecycle<br/>V22")]
        CLIENT_LIMITS[("client_limits<br/>V23-V24")]
        BROKER_REG[("broker_registry<br/>V25-V26")]
    end

    subgraph Services["⚙️ Services Layer"]
        direction TB
        LOADER["InstrumentLoader"]
        ELIG_CACHE["EligibilityCache<br/>Caffeine 100K TTL:60s"]
        ELIG_RESOLVER["EligibilityResolver"]
        RMS_SERVICE["RmsValidationService"]
        CLIENT_RISK["ClientRiskEvaluator"]
        BROKER_ENGINE["MultiBrokerEngine"]
    end

    subgraph Adapters["🔌 Broker Adapters"]
        UPSTOX_ADAPTER["UpstoxAdapter"]
        FYERS_ADAPTER["FyersAdapter"]
        ZERODHA_ADAPTER["ZerodhaAdapter"]
    end

    subgraph Frontend["🖥️ Frontend"]
        REST_API["REST API<br/>:28020"]
        AUTOCOMPLETE["Autocomplete"]
        ORDER_FORM["Order Form"]
        PORTFOLIO["Portfolio View"]
    end

    %% Data Flow
    UPSTOX_API --> LOADER
    NSE_MASTER --> LOADER
    BSE_MASTER --> LOADER
    REGULATORY --> WATCHLIST

    LOADER --> INST_MASTER
    LOADER --> OVERLAYS
    
    INST_MASTER --> ELIG_CACHE
    SEC_TYPE --> ELIG_RESOLVER
    EX_SERIES --> ELIG_RESOLVER
    WATCHLIST --> ELIG_RESOLVER
    IPO_CAL --> ELIG_RESOLVER
    
    MARGIN --> RMS_SERVICE
    QTY_CAP --> RMS_SERVICE
    PRICE_BAND --> RMS_SERVICE
    FO_LIFE --> RMS_SERVICE
    
    CLIENT_LIMITS --> CLIENT_RISK
    BROKER_REG --> BROKER_ENGINE

    ELIG_CACHE --> ELIG_RESOLVER
    ELIG_RESOLVER --> RMS_SERVICE
    RMS_SERVICE --> CLIENT_RISK
    CLIENT_RISK --> BROKER_ENGINE
    
    BROKER_ENGINE --> UPSTOX_ADAPTER
    BROKER_ENGINE --> FYERS_ADAPTER
    BROKER_ENGINE --> ZERODHA_ADAPTER

    REST_API --> ELIG_CACHE
    REST_API --> RMS_SERVICE
    REST_API --> BROKER_ENGINE
    
    AUTOCOMPLETE --> REST_API
    ORDER_FORM --> REST_API
    PORTFOLIO --> REST_API
```

---

## Order Validation Flow

```mermaid
sequenceDiagram
    autonumber
    participant User
    participant API as REST API
    participant Cache as Eligibility Cache
    participant Resolver as Eligibility Resolver
    participant RMS as RMS Validation
    participant Client as Client Risk
    participant Engine as Multi-Broker
    participant Broker as Upstox

    User->>API: POST /orders
    
    rect rgb(240, 248, 255)
        Note over API,Cache: Step 1: Eligibility Check
        API->>Cache: get(instrumentKey)
        alt Cache Hit
            Cache-->>API: ProductEligibility
        else Cache Miss
            Cache->>Resolver: resolve(instrumentKey)
            Resolver->>Resolver: Check PCA/T2T/IPO/SME
            Resolver-->>Cache: ProductEligibility
            Cache-->>API: ProductEligibility
        end
    end

    rect rgb(255, 248, 240)
        Note over API,RMS: Step 2: RMS Validation
        API->>RMS: validate(instrument, product, qty, price)
        RMS->>RMS: Check price band
        RMS->>RMS: Check quantity cap
        RMS->>RMS: Check FO expiry
        RMS-->>API: RmsValidationResult
    end

    rect rgb(240, 255, 240)
        Note over API,Client: Step 3: Client Risk
        API->>Client: validate(limit, state, projections)
        Client->>Client: Check kill-switch
        Client->>Client: Check exposure limits
        Client->>Client: Check max loss
        Client-->>API: Pass/Reject
    end

    rect rgb(255, 240, 255)
        Note over API,Broker: Step 4: Order Execution
        API->>Engine: routeOrder(UPSTOX, request)
        Engine->>Broker: placeOrder(request)
        Broker-->>Engine: BrokerOrderResponse
        Engine-->>API: OrderResult
    end

    API-->>User: Order Confirmation
```

---

## Database Entity Relationships

```mermaid
erDiagram
    INSTRUMENT_MASTER {
        string instrument_key PK
        string trading_symbol
        string segment
        string exchange
        string instrument_type
        string equity_security_type FK
        string exchange_series FK
    }

    EQUITY_SECURITY_TYPE {
        string code PK
        string description
        boolean mis_allowed
        boolean mtf_allowed
        boolean cnc_allowed
    }

    EXCHANGE_SERIES {
        string exchange PK
        string series_code PK
        boolean rolling_settlement
        boolean trade_for_trade
        boolean mis_allowed
    }

    REGULATORY_WATCHLIST {
        string symbol PK
        string exchange PK
        string watch_type
        date effective_date
        date expiry_date
    }

    IPO_CALENDAR {
        string symbol PK
        string exchange PK
        date listing_date
        string restriction_level
    }

    INTRADAY_MARGIN {
        string exchange PK
        string series PK
        decimal margin_pct
        decimal leverage
    }

    QUANTITY_CAP {
        string instrument_key PK
        integer max_qty
        decimal max_value
        date effective_date
    }

    PRICE_BAND {
        string instrument_key PK
        date trade_date PK
        decimal lower_price
        decimal upper_price
    }

    FO_CONTRACT_LIFECYCLE {
        string instrument_key PK
        date expiry_date
        string status
        string instrument_type
    }

    CLIENT_RISK_LIMIT {
        string client_id PK
        decimal max_gross_exposure
        decimal max_order_value
        decimal max_intraday_loss
        boolean trading_enabled
    }

    CLIENT_RISK_STATE {
        string client_id PK
        decimal gross_exposure
        decimal current_mtm
        integer open_positions
    }

    BROKER {
        string broker_id PK
        string name
        string api_type
        boolean enabled
    }

    BROKER_SYMBOL_MAPPING {
        string broker_id PK
        string instrument_key PK
        string broker_symbol
    }

    INSTRUMENT_MASTER ||--o| EQUITY_SECURITY_TYPE : "has"
    INSTRUMENT_MASTER ||--o| EXCHANGE_SERIES : "belongs to"
    INSTRUMENT_MASTER ||--o{ REGULATORY_WATCHLIST : "may have"
    INSTRUMENT_MASTER ||--o| IPO_CALENDAR : "may have"
    INSTRUMENT_MASTER ||--o| QUANTITY_CAP : "may have"
    INSTRUMENT_MASTER ||--o{ PRICE_BAND : "has daily"
    INSTRUMENT_MASTER ||--o| FO_CONTRACT_LIFECYCLE : "has (F&O)"
    EXCHANGE_SERIES ||--o{ INTRADAY_MARGIN : "defines"
    CLIENT_RISK_LIMIT ||--|| CLIENT_RISK_STATE : "has"
    BROKER ||--o{ BROKER_SYMBOL_MAPPING : "maps"
```

---

## Client Risk Evaluation

```mermaid
flowchart TD
    START([Order Request]) --> CHECK_ENABLED{Trading<br/>Enabled?}
    
    CHECK_ENABLED -->|No| REJECT_DISABLED[❌ CLIENT_DISABLED]
    CHECK_ENABLED -->|Yes| CHECK_ORDER_VALUE{Order Value<br/>≤ Limit?}
    
    CHECK_ORDER_VALUE -->|No| REJECT_ORDER[❌ ORDER_VALUE_LIMIT]
    CHECK_ORDER_VALUE -->|Yes| CHECK_GROSS{Gross Exposure<br/>≤ Limit?}
    
    CHECK_GROSS -->|No| REJECT_GROSS[❌ GROSS_EXPOSURE_LIMIT]
    CHECK_GROSS -->|Yes| CHECK_NET{Net Exposure<br/>≤ Limit?}
    
    CHECK_NET -->|No| REJECT_NET[❌ NET_EXPOSURE_LIMIT]
    CHECK_NET -->|Yes| CHECK_TURNOVER{Turnover<br/>≤ Limit?}
    
    CHECK_TURNOVER -->|No| REJECT_TURNOVER[❌ TURNOVER_LIMIT]
    CHECK_TURNOVER -->|Yes| CHECK_POSITIONS{Positions<br/>≤ Limit?}
    
    CHECK_POSITIONS -->|No| REJECT_POS[❌ POSITION_COUNT_LIMIT]
    CHECK_POSITIONS -->|Yes| CHECK_LOSS{Intraday Loss<br/>≤ Limit?}
    
    CHECK_LOSS -->|No| REJECT_LOSS[❌ MAX_LOSS_HIT]
    CHECK_LOSS -->|Yes| APPROVE([✅ Order Approved])

    style REJECT_DISABLED fill:#ffcccc
    style REJECT_ORDER fill:#ffcccc
    style REJECT_GROSS fill:#ffcccc
    style REJECT_NET fill:#ffcccc
    style REJECT_TURNOVER fill:#ffcccc
    style REJECT_POS fill:#ffcccc
    style REJECT_LOSS fill:#ffcccc
    style APPROVE fill:#ccffcc
```

---

## Product Eligibility Decision Tree

```mermaid
flowchart TD
    START([Check Instrument]) --> EXISTS{Instrument<br/>Exists?}
    
    EXISTS -->|No| BLOCKED_NOTFOUND[🚫 blocked: NOT_FOUND]
    EXISTS -->|Yes| CHECK_PCA{On PCA<br/>Watchlist?}
    
    CHECK_PCA -->|Yes| CNC_PCA[📋 cncOnly: PCA]
    CHECK_PCA -->|No| CHECK_ASM{On ASM/GSM<br/>Watchlist?}
    
    CHECK_ASM -->|Yes| CNC_SURVEILLANCE[📋 cncOnly: SURVEILLANCE]
    CHECK_ASM -->|No| CHECK_T2T{Trade-for-Trade<br/>Series?}
    
    CHECK_T2T -->|Yes| CNC_T2T[📋 cncOnly: T2T]
    CHECK_T2T -->|No| CHECK_IPO{IPO<br/>Listing Day?}
    
    CHECK_IPO -->|Yes| CNC_IPO[📋 cncOnly: IPO_DAY0]
    CHECK_IPO -->|No| CHECK_SME{SME<br/>Security?}
    
    CHECK_SME -->|Yes| CNC_SME[📋 cncOnly: SME]
    CHECK_SME -->|No| NORMAL[✅ normal: ALL_ALLOWED]

    style BLOCKED_NOTFOUND fill:#ff9999
    style CNC_PCA fill:#ffffcc
    style CNC_SURVEILLANCE fill:#ffffcc
    style CNC_T2T fill:#ffffcc
    style CNC_IPO fill:#ffffcc
    style CNC_SME fill:#ffffcc
    style NORMAL fill:#99ff99
```
