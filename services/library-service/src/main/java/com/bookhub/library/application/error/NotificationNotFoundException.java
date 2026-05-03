package com.bookhub.library.application.error;

public class NotificationNotFoundException extends RuntimeException {

  public NotificationNotFoundException(final String message) {
    super(message);
  }
}
