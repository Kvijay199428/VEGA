package com.vegatrader.upstox.auth.selenium.v2.exception;

/**
 * Exception thrown when Selenium DOM elements are not found.
 * May indicate Upstox UI change requiring code update.
 *
 * @since 2.2.0
 */
public class SeleniumDomException extends AuthException {

    private final String elementIdentifier;

    public SeleniumDomException(String message) {
        super(message, TokenFailureReason.SELENIUM_DOM_CHANGE);
        this.elementIdentifier = null;
    }

    public SeleniumDomException(String message, String elementIdentifier) {
        super(message, TokenFailureReason.SELENIUM_DOM_CHANGE);
        this.elementIdentifier = elementIdentifier;
    }

    public SeleniumDomException(String message, Throwable cause) {
        super(message, TokenFailureReason.SELENIUM_DOM_CHANGE, cause);
        this.elementIdentifier = null;
    }

    public String getElementIdentifier() {
        return elementIdentifier;
    }

    @Override
    public boolean isRetryable() {
        return false; // DOM changes need code fix
    }
}
