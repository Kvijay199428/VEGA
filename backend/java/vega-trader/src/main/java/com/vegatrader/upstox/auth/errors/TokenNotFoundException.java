package com.vegatrader.upstox.auth.errors;

/**
 * Exception thrown when token is not found in database.
 *
 * @since 2.0.0
 */
public class TokenNotFoundException extends AuthenticationException {

    private final String apiName;

    public TokenNotFoundException(String apiName) {
        super(String.format("Token not found for API name: %s", apiName),
                "token_not_found");
        this.apiName = apiName;
    }

    public String getApiName() {
        return apiName;
    }
}
