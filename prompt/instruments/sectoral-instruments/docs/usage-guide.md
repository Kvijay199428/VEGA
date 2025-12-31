# Sectoral Indexing & User Settings - Usage Guide

**Version:** 4.3.0  
**Last Updated:** 2025-12-30

---

## Quick Start

### Run Tests
```bash
mvn test -Dtest="SectoralTest,UserSettingsTest"
```

### Load Index Constituents
```bash
# Via CLI (when implemented)
java -jar vega-trader.jar load-indices --all
java -jar vega-trader.jar load-indices --index NIFTY_IT
```

---

## Sectoral Indexing

### Available Sectors (20)

| Category | Sectors |
|----------|---------|
| **SECTORAL** | IT, Banking, Finance, Pharma, Healthcare, Auto, FMCG, Metal, Energy, Realty, Infra, Media |
| **THEMATIC** | PSU, MNC, Consumption, Commodities, CPSE |
| **BROAD** | Nifty50, Nifty100, Nifty500, Midcap, Smallcap |

### Available Indices (24)

| Index Code | Sector | Source URL |
|------------|--------|------------|
| NIFTY_IT | IT | niftyindices.com |
| NIFTY_BANK | Banking | niftyindices.com |
| NIFTY_FIN_SERVICE | Finance | niftyindices.com |
| NIFTY_PHARMA | Pharma | niftyindices.com |
| NIFTY_50 | NIFTY50 | niftyindices.com |
| ... | ... | ... |

### REST API

```bash
# Get all sectors
curl http://localhost:28020/api/sectors

# Get indices by sector
curl http://localhost:28020/api/indices?sector=IT

# Get index constituents
curl http://localhost:28020/api/indices/NIFTY_IT/constituents

# Search by sector
curl "http://localhost:28020/api/instruments/search?q=bank&sector=BANKING"
```

### Service Usage

```java
@Autowired SectorService sectorService;

// Get all sectors
List<SectorMasterEntity> sectors = sectorService.getAllSectors();

// Get instruments by sector
List<String> itStocks = sectorService.getInstrumentKeysBySector("IT");

// Check if sector is blocked
boolean blocked = sectorService.isSectorBlocked("REALTY");
```

---

## User Settings

### Setting Categories

| Category | Settings |
|----------|----------|
| **INSTRUMENT** | exchange.enabled, segment.enabled, search.mode, refresh.interval |
| **ORDER** | confirm.required, max.qty.per.symbol, max.notional, price.deviation.pct |
| **RISK** | show.span_breakdown, show.var_metrics, refresh.interval |
| **BROKER** | primary, fallback.enabled, auto.failover |
| **LOGGING** | level.ui, show.raw_payload, order.trace |

### REST API

```bash
# Get all settings
curl http://localhost:28020/api/settings \
  -H "Authorization: Bearer $TOKEN"

# Update setting
curl -X PUT http://localhost:28020/api/settings/order.confirm.required \
  -H "Content-Type: application/json" \
  -d '{"value": "true"}'

# Get setting by category
curl http://localhost:28020/api/settings?category=ORDER
```

### CLI Usage

```bash
vega settings list
vega settings get order.max.qty.per.symbol
vega settings set order.confirm.required true
```

### Service Usage

```java
@Autowired UserSettingsService settingsService;

// Get all settings for user
Map<String, String> settings = settingsService.getAllSettings("user1");

// Get specific setting
String mode = settingsService.getSetting("user1", "instrument.search.mode");

// Get typed values
boolean confirm = settingsService.getBoolean("user1", "order.confirm.required", true);
int maxQty = settingsService.getInt("user1", "order.max.qty.per.symbol", 1800);

// Update setting (with audit)
settingsService.updateSetting("user1", "order.confirm.required", "true", "user1", "UI");

// Reset to default
settingsService.resetToDefault("user1", "order.confirm.required");
```

---

## Role Matrix

| Role | Instrument | Order | Broker | System |
|------|:----------:|:-----:|:------:|:------:|
| **VIEWER** | Read | Read | Read | ❌ |
| **TRADER** | ✅ | ✅ | Read | ❌ |
| **ADMIN** | ✅ | ✅ | ✅ | ❌ |
| **SYSTEM** | ❌ | ❌ | ❌ | ✅ |

---

## Database Tables

### Sectoral (V27-V30)
```
sector_master          → 20 sectors
index_master           → 24 indices
index_constituent      → Instrument mappings
sector_risk_limit      → Exposure caps
instrument_sector_view → Derived view
```

### Settings (V31-V32)
```
user_settings          → User preferences
settings_audit_log     → Change history
settings_metadata      → Schema definitions
```

---

## Testing

### Test Classes
| Class | Tests | Coverage |
|-------|-------|----------|
| SectoralTest | 14 | Entity logic |
| UserSettingsTest | 15 | Settings validation |

### Run All Extended Tests
```bash
mvn test -Dtest="SectoralTest,UserSettingsTest,RmsEntityTest,RmsValidationTest,MultiBrokerTest"
```

---

*Documentation generated: 2025-12-30*
