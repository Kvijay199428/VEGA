# Enrollment Submodule

## Package: `com.vegatrader.upstox.api.instrument.enrollment`

The enrollment submodule handles subscription eligibility validation, ensuring that instrument subscription requests do not exceed Upstox API limits.

---

## Classes

### SubscriptionEligibilityValidator

**Path**: `instrument/enrollment/SubscriptionEligibilityValidator.java`  
**Since**: 3.1.0

A hard guardrail validator that prevents subscription requests from exceeding Upstox API limits.

#### Purpose

- Validates subscription counts against mode-specific limits
- Prevents WebSocket rejection at runtime due to limit violations
- Provides both exception-throwing and safe validation methods

#### Subscription Limits by Mode

| Mode | Limit | Description |
|------|-------|-------------|
| `LTPC` | 5,000 | Last Traded Price/Quantity/Time + Close Price + Volume |
| `OPTION_GREEKS` | 2,000 | LTPC + Option Greeks (Delta, Gamma, Vega, Theta, IV) |
| `FULL` | 2,000 | LTPC + 5-depth bid/ask + OHLC + metadata |
| `FULL_D30` | 1,000 | LTPC + 30-depth bid/ask + OHLC + metadata |

---

## API Reference

### validate(int count, Mode mode)

Validates that the subscription count does not exceed the mode limit.

```java
public void validate(int count, Mode mode)
```

**Parameters:**
- `count` - Number of instruments to subscribe
- `mode` - Subscription mode with individual limit

**Throws:**
- `IllegalStateException` - If count exceeds limit

**Example:**
```java
SubscriptionEligibilityValidator validator = new SubscriptionEligibilityValidator();

// This will pass
validator.validate(1000, Mode.LTPC);

// This will throw IllegalStateException
validator.validate(6000, Mode.LTPC); // Exceeds 5000 limit
```

---

### isValid(int count, Mode mode)

Checks if subscription is valid without throwing an exception.

```java
public boolean isValid(int count, Mode mode)
```

**Parameters:**
- `count` - Number of instruments
- `mode` - Subscription mode

**Returns:**
- `true` if valid, `false` if exceeds limit

**Example:**
```java
SubscriptionEligibilityValidator validator = new SubscriptionEligibilityValidator();

if (validator.isValid(instrumentKeys.size(), Mode.FULL)) {
    // Proceed with subscription
    streamer.subscribe(instrumentKeys, Mode.FULL);
} else {
    // Handle limit exceeded
    log.warn("Subscription limit exceeded, reducing set");
}
```

---

## Integration with InstrumentEnrollmentService

The `InstrumentEnrollmentService` uses `SubscriptionEligibilityValidator` internally:

```java
// In InstrumentEnrollmentService
public Set<String> enroll(Set<String> requestedKeys, Mode mode) {
    validator.validate(requestedKeys.size(), mode);
    logger.info("Enrolled {} instruments for mode {}", requestedKeys.size(), mode);
    return requestedKeys;
}
```

---

## Best Practices

1. **Always validate before subscribing** - Call `isValid()` or `validate()` before making subscription requests
2. **Handle limit exceeded gracefully** - Implement fallback logic to reduce subscription set
3. **Consider mode selection** - Choose appropriate mode based on required data and instrument count
4. **Batch subscriptions** - If you need more instruments than a mode allows, consider using multiple connections

---

## Error Handling

When validation fails, the error message includes:
- The mode that was exceeded
- The attempted count
- The maximum allowed limit

```
Subscription limit exceeded for mode FULL_D30: 1500 > 1000
```

---

*Part 2 of 8 - [Back to Overview](./01-overview.md)*
