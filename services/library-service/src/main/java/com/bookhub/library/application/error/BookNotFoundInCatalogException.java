package com.bookhub.library.application.error;

public class BookNotFoundInCatalogException extends RuntimeException {
  public BookNotFoundInCatalogException(final String message) {
    super(message);
  }
}
