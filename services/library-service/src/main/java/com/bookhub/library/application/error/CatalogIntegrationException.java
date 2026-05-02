package com.bookhub.library.application.error;

public class CatalogIntegrationException extends RuntimeException {

  public CatalogIntegrationException(final String message) {
    super(message);
  }

  public CatalogIntegrationException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
