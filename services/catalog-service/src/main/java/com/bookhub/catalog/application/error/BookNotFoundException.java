package com.bookhub.catalog.application.error;

public class BookNotFoundException extends RuntimeException {

  public BookNotFoundException(final String message) {
    super(message);
  }
}
