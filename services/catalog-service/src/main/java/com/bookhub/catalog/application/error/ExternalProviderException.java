package com.bookhub.catalog.application.error;

public class ExternalProviderException extends RuntimeException {

    public ExternalProviderException(final String message) {
        super(message);
    }

    public ExternalProviderException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
