package com.bookhub.library.application.error;

public class BookNotReadException extends RuntimeException {

  public BookNotReadException(final String message) {
    super(message);
  }
}
