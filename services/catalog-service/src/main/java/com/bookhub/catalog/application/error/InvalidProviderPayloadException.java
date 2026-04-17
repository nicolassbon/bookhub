package com.bookhub.catalog.application.error;

public class InvalidProviderPayloadException extends ExternalProviderException {

    public InvalidProviderPayloadException(final String message) {
        super(message);
    }
}
