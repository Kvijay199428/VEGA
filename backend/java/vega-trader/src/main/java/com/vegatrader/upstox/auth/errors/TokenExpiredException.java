package com.vegatrader.upstox.auth.errors;

/**
 * Exception thrown when token has expired.
 *
 * @since 2.0.0
 */
public class TokenExpiredException extends AuthenticationException {

    private final String apiName;
    private final String validityAt;

    public TokenExpiredException(String apiName, String validityAt) {
        super(String.format("Token expired for %s at %s", apiName, validityAt),
                "token_expired");
        this.apiName = apiName;
        this.validityAt = validityAt;
    }

    public String getApiName() {
        return apiName;
    }

    public String getValidityAt() {
        return validityAt;
    }
}
