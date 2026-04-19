package com.bookhub.catalog.application.error;

public class ExternalServiceUnavailableException extends ExternalProviderException {

    private final int retryAfterSeconds;

    public ExternalServiceUnavailableException(final String message, final int retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public ExternalServiceUnavailableException(final String message, final Throwable cause, final int retryAfterSeconds) {
        super(message, cause);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public int retryAfterSeconds() {
        return retryAfterSeconds;
    }
}
