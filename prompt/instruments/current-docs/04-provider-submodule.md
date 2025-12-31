# Provider Submodule

## Package: `com.vegatrader.upstox.api.instrument.provider`

The provider submodule provides an abstraction layer between instrument management and the MarketDataStreamerV3.

---

## Classes

### InstrumentKeyProvider (Interface)

**Path**: `instrument/provider/InstrumentKeyProvider.java`  
**Since**: 3.1.0

The mandatory contract that `MarketDataStreamerV3` depends on for obtaining subscription-ready instrument keys.

#### Design Philosophy

The interface enforces clean separation of concerns:

**MarketDataStreamerV3 SHOULD:**
- Depend only on `InstrumentKeyProvider`
- Focus on WebSocket streaming logic
- Trust that keys are already validated

**MarketDataStreamerV3 should NEVER:**
- Access instrument files directly
- Perform filtering logic
- Discover instruments
- Validate subscription limits

---

#### Interface Definition

```java
public interface InstrumentKeyProvider {
    
    /**
     * Returns subscription-ready instrument keys.
     * 
     * @return immutable set of instrument keys in format "EXCHANGE|SYMBOL"
     *         (e.g., "NSE_INDEX|Nifty 50", "NSE_FO|45450")
     */
    Set<String> getInstrumentKeys();
}
```

---

#### Contract Requirements

Implementations must ensure returned keys are:

| Requirement | Description |
|-------------|-------------|
| **Validated** | Keys conform to `EXCHANGE|IDENTIFIER` format |
| **De-duplicated** | No duplicate keys in the set |
| **Limit-safe** | Below subscription limits for the intended mode |
| **Ready** | Can be immediately used for WebSocket subscription |

---

### FileBackedInstrumentKeyProvider

**Path**: `instrument/provider/FileBackedInstrumentKeyProvider.java`  
**Since**: 3.1.0

File-backed implementation of `InstrumentKeyProvider` that delegates to `InstrumentEnrollmentService`.

#### Constructor

```java
public FileBackedInstrumentKeyProvider(InstrumentEnrollmentService enrollmentService)
```

**Parameters:**
- `enrollmentService` - The enrollment service that manages instrument data

---

#### Implementation

```java
@Override
public Set<String> getInstrumentKeys() {
    Set<String> keys = enrollmentService.enrollConfiguredInstruments();
    logger.debug("Providing {} instrument keys", keys.size());
    return keys;
}
```

---

## Usage with MarketDataStreamerV3

### Configuration Example

```java
@Configuration
public class StreamerConfig {
    
    @Bean
    public InstrumentKeyProvider instrumentKeyProvider(
            InstrumentEnrollmentService enrollmentService) {
        return new FileBackedInstrumentKeyProvider(enrollmentService);
    }
    
    @Bean
    public MarketDataStreamerV3 marketDataStreamer(
            InstrumentKeyProvider keyProvider,
            // ... other dependencies
    ) {
        MarketDataStreamerV3 streamer = new MarketDataStreamerV3();
        
        // Get keys from provider
        Set<String> instrumentKeys = keyProvider.getInstrumentKeys();
        
        // Subscribe
        streamer.subscribe(instrumentKeys, Mode.LTPC);
        
        return streamer;
    }
}
```

---

## Bounded Context Benefits

### Before (Tight Coupling)

```java
// BAD: MarketDataStreamerV3 directly accessing files and filtering
class MarketDataStreamerV3 {
    public void start() {
        // Direct file access - violation!
        List<Instrument> instruments = loadFromFile("NSE.json.gz");
        
        // Filtering logic - violation!
        List<String> keys = instruments.stream()
            .filter(i -> i.getSegment().equals("NSE_EQ"))
            .map(Instrument::getKey)
            .collect(toList());
        
        subscribe(keys);
    }
}
```

### After (Clean Separation)

```java
// GOOD: MarketDataStreamerV3 depends only on provider interface
class MarketDataStreamerV3 {
    private final InstrumentKeyProvider keyProvider;
    
    public void start() {
        // Clean delegation
        Set<String> keys = keyProvider.getInstrumentKeys();
        subscribe(keys);
    }
}
```

---

## Creating Custom Providers

You can implement custom providers for different use cases:

### Database-Backed Provider

```java
public class DatabaseInstrumentKeyProvider implements InstrumentKeyProvider {
    
    private final InstrumentRepository repository;
    
    @Override
    public Set<String> getInstrumentKeys() {
        return repository.findActiveInstrumentKeys()
            .stream()
            .collect(Collectors.toSet());
    }
}
```

### Static Configuration Provider

```java
public class StaticInstrumentKeyProvider implements InstrumentKeyProvider {
    
    private final Set<String> keys = Set.of(
        "NSE_INDEX|Nifty 50",
        "NSE_INDEX|Nifty Bank",
        "NSE_EQ|INE002A01018"  // Reliance
    );
    
    @Override
    public Set<String> getInstrumentKeys() {
        return keys;
    }
}
```

### User-Selection Provider

```java
public class UserSelectionKeyProvider implements InstrumentKeyProvider {
    
    private final WatchlistService watchlistService;
    private final String userId;
    
    @Override
    public Set<String> getInstrumentKeys() {
        return watchlistService.getUserWatchlist(userId)
            .stream()
            .map(WatchlistItem::getInstrumentKey)
            .collect(Collectors.toSet());
    }
}
```

---

*Part 4 of 8 - [Back to Overview](./01-overview.md)*
