# Error Handling

The application provides a comprehensive error handling framework mapping Upstox error codes to actionable resolutions.

## Core Components
- **Error Codes**: `com.vegatrader.upstox.api.errors.UpstoxErrorCode`
- **HTTP Status**: `com.vegatrader.upstox.api.errors.UpstoxHttpStatus`
- **Handler**: `com.vegatrader.upstox.api.errors.handlers.BaseErrorHandler`

## Standard Error Response
All errors follow a standard JSON structure:
```json
{
  "errorCode": "UDAPI100050",
  "message": "Invalid Instrument Key",
  "data": null,
  "status": "error"
}
```

## Common Error Codes

| Code | Status | Description | Resolution |
|------|--------|-------------|------------|
| `UDAPI100050` | 400 | Invalid Instrument Key | check format (e.g., NSE_EQ\|INE...) |
| `UDAPI100001` | 401 | Invalid/Expired Token | Re-authenticate flow |
| `UDAPI100060` | 429 | Rate Limit Exceeded | Slow down requests |
| `UDAPI100069` | 404 | Order Not Found | Verify Order ID |

## Feature-Specific Handlers
- **OrderErrorHandler**: Custom logic for order rejections (funds, risk limits).
- **MarketDataErrorHandler**: Handles missing subscription or key errors.
- **WebSocketErrorHandler**: Manages disconnection and connection refusal scenarios.
