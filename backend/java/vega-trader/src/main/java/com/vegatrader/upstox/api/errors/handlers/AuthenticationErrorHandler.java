package com.vegatrader.upstox.api.errors.handlers;

import com.vegatrader.upstox.api.response.common.ErrorResponse;

/**
 * Error handler for authentication specific errors.
 *
 * @since 2.0.0
 */
public class AuthenticationErrorHandler extends BaseErrorHandler {

    public AuthenticationErrorHandler() {
        registerResolutionHint("invalid_token",
                "Token expired or invalid. Please re-authenticate");

        registerResolutionHint("unauthorized",
                "Access token missing or invalid. Include 'Authorization: Bearer <token>' header");

        registerResolutionHint("token_expired",
                "Access token has expired. Refresh token or re-authenticate");

        registerResolutionHint("invalid_client",
                "Client ID or secret is incorrect");

        registerResolutionHint("invalid_grant",
                "Authorization code is invalid or expired");

        registerResolutionHint("invalid_redirect_uri",
                "Redirect URI does not match registered URI");
    }

    @Override
    public ErrorResponse handleError(int httpStatus, String errorCode) {
        ErrorResponse error = super.handleError(httpStatus, errorCode);
        error.addDetail("category", "AUTHENTICATION_ERROR");

        if ("token_expired".equals(errorCode)) {
            error.addDetail("action_required", "REFRESH_TOKEN");
        } else if ("unauthorized".equals(errorCode) || "invalid_token".equals(errorCode)) {
            error.addDetail("action_required", "RE_AUTHENTICATE");
        }

        return error;
    }
}
