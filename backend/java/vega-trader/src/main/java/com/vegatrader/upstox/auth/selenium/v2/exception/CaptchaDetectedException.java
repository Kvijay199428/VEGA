package com.vegatrader.upstox.auth.selenium.v2.exception;

/**
 * Exception thrown when CAPTCHA is detected during login.
 * 
 * CRITICAL POLICY:
 * - Abort immediately
 * - Never retry
 * - Quarantine token
 * - Require manual intervention
 *
 * @since 2.2.0
 */
public class CaptchaDetectedException extends AuthException {

    private final String captchaType;

    public CaptchaDetectedException(String message) {
        super(message, TokenFailureReason.CAPTCHA);
        this.captchaType = "unknown";
    }

    public CaptchaDetectedException(String message, String captchaType) {
        super(message, TokenFailureReason.CAPTCHA);
        this.captchaType = captchaType;
    }

    public CaptchaDetectedException(String message, String captchaType, String apiName) {
        super(message, TokenFailureReason.CAPTCHA, apiName);
        this.captchaType = captchaType;
    }

    public String getCaptchaType() {
        return captchaType;
    }

    @Override
    public boolean requiresQuarantine() {
        return true; // Always quarantine on CAPTCHA
    }

    @Override
    public boolean isRetryable() {
        return false; // Never retry CAPTCHA
    }

    @Override
    public String toString() {
        return String.format("CaptchaDetectedException{type='%s', apiName='%s', message='%s'}",
                captchaType, getApiName(), getMessage());
    }
}
