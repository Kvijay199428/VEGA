# Multi-Login Automation - Production Commands

## Configuration Hygiene Fixes Applied ✅

### 1. .env Path Resolution
- **Fixed**: `EnvConfigLoader` now uses `System.getProperty("user.dir")` for robust path resolution
- **Enhancement**: Added `VEGA_ENV_FILE` environment variable override for production flexibility

### 2. SLF4J Multiple Bindings
- **Fixed**: Excluded `slf4j-simple` from `selenium-java` dependency in `pom.xml`
- **Result**: No more SLF4J warnings; Logback is the sole binding

### 3. PowerShell Maven Commands
- **Rule**: ALL `-D` properties must be quoted AND come before the goal

---

## Correct Production Commands

### Run Multi-Login (All 6 APIs)

```powershell
cd "d:\projects\VEGA TRADER\backend\java\vega-trader"

# Compile first
mvn clean compile test-compile

# Run multi-login
mvn -q exec:java@default-cli `
  "-Dexec.mainClass=com.vegatrader.upstox.auth.selenium.integration.CompleteLoginTest" `
  "-Dexec.args=multi" `
  "-Dexec.classpathScope=test"
```

### Run Single Login (PRIMARY API Only)

```powershell
mvn -q exec:java@default-cli `
  "-Dexec.mainClass=com.vegatrader.upstox.auth.selenium.integration.CompleteLoginTest" `
  "-Dexec.classpathScope=test"
```

### Verify Database Tokens

```powershell
mvn -q exec:java@default-cli `
  "-Dexec.mainClass=com.vegatrader.upstox.auth.util.DatabaseUtility" `
  "-Dexec.classpathScope=test"
```

### Capture Output to File

```powershell
# Use *> for all streams (not 2>&1)
mvn -q exec:java@default-cli `
  "-Dexec.mainClass=com.vegatrader.upstox.auth.util.DatabaseUtility" `
  "-Dexec.classpathScope=test" `
  *> database-status.txt
```

---

## Environment Variable Override (Production)

For production deployments, override `.env` location:

```powershell
$env:VEGA_ENV_FILE = "D:\production\config\.env"

mvn -q exec:java@default-cli `
  "-Dexec.mainClass=com.vegatrader.upstox.auth.selenium.integration.CompleteLoginTest" `
  "-Dexec.args=multi" `
  "-Dexec.classpathScope=test"
```

---

## Test Suite Execution

### All Token Tests (39 tests)

```powershell
mvn test `
  "-Dtest=DatabaseSchemaVerificationTest,TokenRepositoryTest,TokenStorageServiceTest,MultiLoginIntegrationTest"
```

### Individual Test Classes

```powershell
# Database schema verification (8 tests)
mvn test "-Dtest=DatabaseSchemaVerificationTest"

# Repository unit tests (11 tests)
mvn test "-Dtest=TokenRepositoryTest"

# Service layer tests (11 tests)
mvn test "-Dtest=TokenStorageServiceTest"

# Multi-login integration (9 tests)
mvn test "-Dtest=MultiLoginIntegrationTest"
```

---

## Maven Dependency Commands (Corrected PowerShell Syntax)

### Build Classpath

```powershell
# ✅ Correct
mvn "dependency:build-classpath" "-Dmdep.outputFile=classpath.txt"

# ❌ Wrong (PowerShell breaks it)
mvn dependency:build-classpath -Dmdep.outputFile=classpath.txt
```

### Analyze Dependencies

```powershell
mvn "dependency:tree" "-Dverbose"
```

---

## Expected Behavior After Fixes

### No SLF4J Warnings
```
✅ Clean output - No "multiple SLF4J providers" warning
```

### Correct .env Loading
```
✅ Loading .env from: D:\projects\VEGA TRADER\backend\java\vega-trader\.env
✅ Loaded 20 properties from .env
```

### Database Token Storage
After successful multi-login:
```
Total tokens: 6
Active tokens: 6
Inactive tokens: 0

Active API Names:
  - PRIMARY
  - WEBSOCKET1
  - WEBSOCKET2
  - WEBSOCKET3
  - OPTIONCHAIN1
  - OPTIONCHAIN2
```

---

## Files Modified

1. **[EnvConfigLoader.java](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/src/main/java/com/vegatrader/upstox/auth/selenium/config/EnvConfigLoader.java)**
   - Removed hardcoded path
   - Added `System.getProperty("user.dir")` resolution
   - Added `VEGA_ENV_FILE` environment variable override

2. **[pom.xml](file:///d:/projects/VEGA%20TRADER/backend/java/vega-trader/pom.xml)**
   - Excluded `slf4j-simple` from Selenium dependency
   - Resolves SLF4J multiple bindings warning

---

## Quick Reference Card

| Task | Command |
|------|---------|
| **Compile** | `mvn clean compile test-compile` |
| **Multi-Login** | `mvn -q exec:java@default-cli "-Dexec.mainClass=...CompleteLoginTest" "-Dexec.args=multi" "-Dexec.classpathScope=test"` |
| **Check DB** | `mvn -q exec:java@default-cli "-Dexec.mainClass=...DatabaseUtility" "-Dexec.classpathScope=test"` |
| **All Tests** | `mvn test "-Dtest=*Token*Test,*Database*Test,MultiLoginIntegrationTest"` |

---

✅ **System Status**: Production-ready with clean configuration
