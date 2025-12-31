# Authentication Module

The `com.upstox.auth` package contains classes responsible for handling authentication with the Upstox API.

## Classes

### 1. [OAuth.java](../src/main/java/com/upstox/auth/OAuth.java)
**Implements**: `Authentication`
- **Purpose**: Handles OAuth2 Bearer Token authentication.
- **Key Methods**:
  - `setAccessToken(String accessToken)`: Sets the bearer token.
  - `applyToParams(...)`: Injects `Authorization: Bearer <token>` into request headers.

### 2. [ApiKeyAuth.java](../src/main/java/com/upstox/auth/ApiKeyAuth.java)
**Implements**: `Authentication`
- **Purpose**: Handles API Key based authentication (if supported by specific endpoints).
- **Locations**: Supports passing key in `header` or `query` params.

### 3. [HttpBasicAuth.java](../src/main/java/com/upstox/auth/HttpBasicAuth.java)
**Implements**: `Authentication`
- **Purpose**: Standard HTTP Basic Auth (Username/Password).
- **Usage**: Encodes credentials in Base64 for the `Authorization` header.

### 4. [OAuthFlow.java](../src/main/java/com/upstox/auth/OAuthFlow.java)
**Enum**: `accessCode`, `implicit`, `password`, `application`
- **Purpose**: Defines supported OAuth2 flows.

### 5. [Authentication.java](../src/main/java/com/upstox/auth/Authentication.java)
**Interface**
- **Purpose**: Common interface for all authentication schemes.
- **Method**: `applyToParams(List<Pair> queryParams, Map<String, String> headerParams)` - applies auth credentials to the request.
