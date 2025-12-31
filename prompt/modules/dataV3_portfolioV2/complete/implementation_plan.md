# Feature Upgrade Instructions Summary

## ✅ Current Implementation Status: VALIDATED

The documentation (a1.md) confirms your current implementation is **correct**:

| Component | Status | Evidence |
|-----------|--------|----------|
| Authorization flow (Doc 1) | ✅ Correct | 200 OK, WSS URL resolved |
| WebSocket connection (Doc 2) | ✅ Correct | Handshake succeeded |
| Binary subscription (V3 spec) | ✅ Correct | Sent as binary, correct structure |
| Protocol sequence | ✅ Correct | market_info → initial_feed received |
| Protobuf decoding | ✅ Correct | FeedResponse parsed correctly |

> "You have crossed the hardest milestone: A real, authenticated, protobuf-decoded V3 market feed."

---

## ⚠️ Warnings to Address

### 1. SLF4J "StaticLoggerBinder not found"
**Impact:** No logs in production, debugging visibility lost  
**Fix:** Add Logback dependency

```xml
<dependency>
  <groupId>ch.qos.logback</groupId>
  <artifactId>logback-classic</artifactId>
  <version>1.4.14</version>
</dependency>
```

### 2. Protobuf sun.misc.Unsafe warning
**Impact:** None today (Java 21 warning)  
**Action:** Ignore for now

---

## 📋 Ordered Next Steps (DO NOT SKIP)

### Step 1: Logging (Mandatory) ✅ READY
Add Logback and configure JSON logs.

**Files to create/modify:**
- Add logback dependency to `pom.xml`
- Create `src/main/resources/logback-spring.xml`

---

### Step 2: Reconnect + Backpressure ✅ ALREADY IMPLEMENTED

Per your current implementation, you already have:
- `MarketDataBuffer` ✅
- `PortfolioDataBuffer` ✅  
- `BufferConsumer` ✅
- Reconnect logic ✅

---

### Step 3: Subscription Limit Enforcement

Before sending `sub` request:
1. Validate instrument count
2. Validate mode vs tier
3. Reject client-side early

**Already implemented in:**
- `MarketDataStreamerV3.subscribe()` - validates limits ✅
- `ConnectionSettings` - tracks limits ✅

---

### Step 4: Event Bus Alignment ✅ ALREADY IMPLEMENTED

Convert raw FeedResponse into typed events:

```
MarketDataEvent
 ├── MarketInfoEvent
 ├── SnapshotEvent
 └── TickEvent
```

**Already implemented:**
- `EventBus` interface ✅
- `InMemoryEventBus` ✅
- `MarketUpdateEvent` ✅
- `PortfolioUpdateEvent` ✅

---

### Step 5: Persistence + Replay (OPTIONAL)

Persist decoded ticks for backtesting replay.

**Architecture from a2.md:**
```java
@Service
public class ReplayService {
    public void replay(List<TickEvent> historicalTicks, FeedDispatcher dispatcher) {
        for (TickEvent tick : historicalTicks) {
            dispatcher.dispatch(tick);
        }
    }
}
```

---

## 🔧 Spring Boot Starter Integration (a2.md)

The documentation provides a **full project structure** for converting to a Spring Boot starter:

```
market-feeder-springboot/
├── src/main/java/com/vegatrader/marketfeeder
│   ├── MarketFeederApplication.java
│   ├── config/
│   │   ├── WebSocketConfig.java
│   │   ├── MicrometerConfig.java
│   │   └── AppProperties.java
│   ├── feeder/
│   │   ├── MarketDataStreamer.java
│   │   ├── SubscriptionManager.java
│   │   ├── FeedDispatcher.java
│   │   ├── MarketDataEvent.java
│   │   ├── TickEvent.java
│   │   └── ReplayService.java
│   └── portfolio/
│       └── PortfolioStreamFeedV2Adapter.java
└── src/main/resources/
    ├── application.yml
    └── logback-spring.xml
```

### Key Components from a2.md:

1. **FeedDispatcher** - Event bus for ticks
2. **PortfolioStreamFeedV2Adapter** - Aligns portfolio with market data
3. **ReplayService** - Deterministic replay for backtesting
4. **Micrometer/Prometheus** - Metrics at `/actuator/prometheus`

---

## 📊 Assessment: What's Already Done vs What's Needed

| Feature | MarketDataStreamerV3 | PortfolioDataStreamerV2 |
|---------|---------------------|------------------------|
| Authorization flow | ✅ Done | ✅ Done (just added) |
| Binary/JSON handling | ✅ Protobuf | ✅ JSON |
| Backpressure buffer | ✅ MarketDataBuffer | ✅ PortfolioDataBuffer |
| EventBus | ✅ InMemoryEventBus | ✅ InMemoryEventBus |
| Subscription limits | ✅ ConnectionSettings | N/A (server-controlled) |
| State tracking | ✅ MarketStateTracker | ✅ PortfolioStateTracker |
| Reconnect logic | ✅ attemptReconnect() | ✅ scheduleReconnect() |
| Logging | ⚠️ Needs Logback | ⚠️ Needs Logback |
| Micrometer metrics | ❌ Not yet | ❌ Not yet |
| Replay/Persistence | ❌ Not yet | ❌ Not yet |

---

## Recommendation

Your implementation is **production-ready** for core functionality. The remaining items are:

1. **Add Logback** (easy, 1 dependency + config file)
2. **Add Micrometer metrics** (optional but recommended for monitoring)
3. **Persistence/Replay** (optional, for backtesting)

These are enhancements, not blockers.
