package com.vegatrader.upstox.auth.errors;

/**
 * Exception thrown when authorization code is invalid or expired.
 *
 * @since 2.0.0
 */
public class InvalidAuthCodeException extends AuthenticationException {

    public InvalidAuthCodeException(String message) {
        super(message, "invalid_grant");
    }

    public InvalidAuthCodeException(String message, Throwable cause) {
        super(message, "invalid_grant", cause);
    }
}
