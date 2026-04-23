package com.bookhub.catalog.application.error;

public class InvalidBookIdException extends RuntimeException {

  public InvalidBookIdException(final String message) {
    super(message);
  }
}
