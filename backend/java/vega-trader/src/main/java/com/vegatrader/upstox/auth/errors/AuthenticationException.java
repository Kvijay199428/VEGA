package com.vegatrader.upstox.auth.errors;

/**
 * Base exception for authentication-related errors.
 *
 * @since 2.0.0
 */
public class AuthenticationException extends RuntimeException {

    private final String errorCode;

    public AuthenticationException(String message) {
        super(message);
        this.errorCode = "authentication_error";
    }

    public AuthenticationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "authentication_error";
    }

    public AuthenticationException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return String.format("AuthenticationException{errorCode='%s', message='%s'}",
                errorCode, getMessage());
    }
}
