package com.bookhub.library.application.error;

public class ReviewAlreadyExistsException extends RuntimeException {

  public ReviewAlreadyExistsException(final String message) {
    super(message);
  }
}
