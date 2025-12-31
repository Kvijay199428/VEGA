# API Reference

## 1. REST Endpoints

### 1.1 Token Status
```http
GET /api/auth/upstox/tokens/status
```

**Response:**
```json
{
  "summary": {
    "valid": 4,
    "invalid": 1,
    "missing": 1,
    "total": 6
  },
  "tokens": [
    {
      "apiName": "PRIMARY",
      "status": "VALID",
      "validUntil": "2025-12-30T03:30:00",
      "userId": "7EAHBJ"
    },
    {
      "apiName": "WEBSOCKET1",
      "status": "EXPIRED",
      "validUntil": "2025-12-29T03:30:00"
    }
  ]
}
```

---

### 1.2 Token Generation
```http
POST /api/auth/upstox/tokens/generate
Content-Type: application/json
```

**Request Body:**
```json
{
  "mode": "INVALID_ONLY",
  "apiNames": []
}
```

**Mode Options:**
| Mode | Description |
|------|-------------|
| `ALL` | Regenerate all 6 tokens |
| `INVALID_ONLY` | Only invalid/missing tokens |
| `PARTIAL` | Specific APIs (use `apiNames`) |

**Response:**
```json
{
  "success": true,
  "generated": 2,
  "failed": 0,
  "results": [
    { "apiName": "WEBSOCKET1", "success": true },
    { "apiName": "OPTIONCHAIN2", "success": true }
  ]
}
```

---

### 1.3 Login Success
```http
GET /api/auth/upstox/login-success/{apiName}
```

**Response:**
```json
{
  "tokenInserted": true,
  "apiName": "PRIMARY",
  "tokenExpiry": "2025-12-30T03:30:00+05:30",
  "generatedAt": "2025-12-29 21:10:15",
  "message": "Access token generated and stored successfully",
  "profile": {
    "userId": "7EAHBJ",
    "userName": "vijay kumar sharma",
    "email": "v***@gmail.com",
    "broker": "UPSTOX",
    "userType": "individual",
    "exchanges": ["NSE", "NFO", "BSE", "CDS", "BFO", "BCD"],
    "products": ["D", "CO", "I"],
    "orderTypes": ["MARKET", "LIMIT", "SL", "SL-M"],
    "poa": false,
    "ddpi": false,
    "isActive": true
  }
}
```

---

### 1.4 All Login Success
```http
GET /api/auth/upstox/login-success
```

Returns array of login success responses for all stored tokens.

---

## 2. Internal APIs (Upstox)

### 2.1 Authorization URL
```
GET https://api.upstox.com/v2/login/authorization/dialog
  ?client_id={client_id}
  &redirect_uri={redirect_uri}
  &response_type=code
```

### 2.2 Token Exchange
```http
POST https://api.upstox.com/v2/login/authorization/token
Content-Type: application/x-www-form-urlencoded

code={auth_code}
&client_id={client_id}
&client_secret={client_secret}
&redirect_uri={redirect_uri}
&grant_type=authorization_code
```

**Response:**
```json
{
  "email": "v***@gmail.com",
  "exchanges": ["NSE", "NFO", "BSE", "CDS", "BFO", "BCD"],
  "products": ["D", "CO", "I"],
  "broker": "UPSTOX",
  "user_id": "7EAHBJ",
  "user_name": "vijay kumar sharma",
  "order_types": ["MARKET", "LIMIT", "SL", "SL-M"],
  "user_type": "individual",
  "poa": false,
  "is_active": true,
  "access_token": "eyJ0eXAiOi...",
  "extended_token": null,
  "expires_in": 86400
}
```

### 2.3 Profile Verification
```http
GET https://api.upstox.com/v2/user/profile
Authorization: Bearer {access_token}
Accept: application/json
```

**Response (200 OK = valid):**
```json
{
  "status": "success",
  "data": {
    "email": "v***@gmail.com",
    "user_id": "7EAHBJ",
    "user_name": "vijay kumar sharma",
    "broker": "UPSTOX"
  }
}
```

---

## 3. Error Responses

### 3.1 Token Not Found
```json
{
  "tokenInserted": false,
  "apiName": "WEBSOCKET3",
  "message": "Token not found for: WEBSOCKET3"
}
```

### 3.2 Invalid API Name
```json
{
  "error": "Invalid API name: UNKNOWN",
  "validNames": ["PRIMARY", "WEBSOCKET1", "WEBSOCKET2", "WEBSOCKET3", "OPTIONCHAIN1", "OPTIONCHAIN2"]
}
```

### 3.3 Generation Failed
```json
{
  "success": false,
  "generated": 1,
  "failed": 1,
  "results": [
    { "apiName": "WEBSOCKET1", "success": true },
    { "apiName": "WEBSOCKET2", "success": false, "error": "PIN timeout" }
  ]
}
```

---

## 4. HTTP Status Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 400 | Invalid request (bad API name) |
| 401 | Token invalid/expired |
| 404 | Token not found |
| 500 | Internal error |
| 503 | Upstox unavailable |

---

**Document Status:** Final  
**Last Updated:** 2025-12-29
