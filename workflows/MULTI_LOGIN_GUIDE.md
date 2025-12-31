# Live Multi-Login Execution Guide

## Prerequisites

To run the live multi-login automation, you need:

### 1. Upstox API Applications
Create 6 separate API applications in the [Upstox Developer Console](https://api.upstox.com):
- **API 0**: PRIMARY (general usage)
- **API 1-3**: WEBSOCKET1-3 (market data streaming)
- **API 4-5**: OPTIONCHAIN1-2 (option chain data)

For each API app, you'll receive:
- Client ID
- Client Secret  
- Configure redirect URI: `http://localhost:3000/callback`

### 2. Configuration File

Create a `.env` file in the `d:\projects\VEGA TRADER\backend\` directory:

```bash
# Copy the template
cp backend/.env.template backend/.env

# Edit with your actual credentials
notepad backend/.env
```

Fill in all the values in the `.env` file with your actual Upstox credentials.

### 3. Chrome Browser
The Selenium automation uses Chrome browser. Ensure Chrome is installed on your system.

---

## Running Multi-Login

### Option 1: Single API Login (PRIMARY only)

Test with just the PRIMARY API first:

```bash
cd "d:\projects\VEGA TRADER\backend\java\vega-trader"
mvn compile
java -cp "target/classes;target/test-classes" com.vegatrader.upstox.auth.selenium.integration.CompleteLoginTest
```

### Option 2: Full Multi-Login (All 6 APIs)

Run the complete multi-login for all 6 APIs:

```bash
cd "d:\projects\VEGA TRADER\backend\java\vega-trader"
mvn compile
java -cp "target/classes;target/test-classes" com.vegatrader.upstox.auth.selenium.integration.CompleteLoginTest multi
```

**Expected behavior:**
- Browser will open (in headless mode for multi-login)
- Automatic login sequence for each API:
  1. Navigate to Upstox OAuth  URL
  2. Enter mobile number
  3. Enter PIN
  4. Enter TOTP (auto-generated)
  5. Approve OAuth permissions
  6. Capture authorization code
  7. Exchange for access token
  8. Store in database
- 5-second delay between each API login (to avoid rate limiting)
- Progress displayed in console

**Duration:** Approximately 3-5 minutes for all 6 APIs

---

## Verifying Database Injection

After successful login, verify tokens are in the database:

```bash
cd "d:\projects\VEGA TRADER\backend\java\vega-trader"
mvn compile
java -cp "target/classes;target/test-classes" com.vegatrader.upstox.auth.util.DatabaseUtility
```

This will show:
- Database connection status
- Token count (should show 6 active tokens)
- List of all active tokens with API names
- Token metadata

### Manual Database Inspection

You can also query the database directly to see the injected tokens:

```sql
-- View all active tokens
SELECT id, api_name, is_primary, is_active, 
       substr(access_token, 1, 20) as token_preview,
       created_at, validity_at
FROM upstox_tokens 
WHERE is_active = 1 
ORDER BY api_name;

-- Count tokens by API name
SELECT api_name, COUNT(*) as count 
FROM upstox_tokens 
WHERE is_active = 1 
GROUP BY api_name;
```

---

## Troubleshooting

### Configuration Not Found
**Error**: "Configuration incomplete - check backend/.env"
**Solution**: Ensure `.env` file exists and all required fields are filled

### Browser Issues
**Error**: Browser doesn't open or crashes
**Solution**: 
- Ensure Chrome is installed
- Update ChromeDriver: `mvn clean compile` (WebDriverManager will auto-update)
- Try visible browser mode (edit `CompleteLoginTest.java` line 126: set `headless = false`)

### Login Failures
**Error**: Login automation fails
**Solution**:
- Verify mobile number, PIN, and TOTP secret are correct
- Check if TOTP is correctly configured (should be base32 secret, not the 6-digit code)
- Try single login first to debug

### Rate Limiting
**Error**: Too many login attempts
**Solution**: Wait 10-15 minutes before retrying. The system has a 5-second delay between logins to prevent this.

---

## Post-Login Usage

Once tokens are in the database, you can:

1. **Use tokens in your application**:
   ```java
   TokenStorageService service = new TokenStorageService();
   Optional<UpstoxTokenEntity> primary = service.getToken("PRIMARY");
   String authHeader = primary.get().getAuthorizationHeader();
   ```

2. **WebSocket connections**:
   - WEBSOCKET1-3 APIs can be used for concurrent market data streams
   - Each has its own access token with 86400-second (24-hour) expiry

3. **Token refresh**:
   - Tokens auto-refresh using the `TokenRefreshScheduler`
   - Manual refresh available via `TokenStorageService.updateToken()`

---

## Expected Database State

After successful multi-login, your `upstox_tokens` table should contain:

| API Name | Is Primary | Is Active | Purpose |
|----------|------------|-----------|---------|
| PRIMARY | true (1) | 1 | General API usage |
| WEBSOCKET1 | false (0) | 1 | Market data stream 1 |
| WEBSOCKET2 | false (0) | 1 | Market data stream 2 |
| WEBSOCKET3 | false (0) | 1 | Market data stream 3 |
| OPTIONCHAIN1 | false (0) | 1 | Option chain data 1 |
| OPTIONCHAIN2 | false (0) | 1 | Option chain data 2 |

All tokens should have:
- Valid access_token (encrypted string)
- Valid refresh_token
- token_type: "Bearer"
- expires_in: 86400 (24 hours)
- Complete metadata (client_id, client_secret, redirect_uri, etc.)
