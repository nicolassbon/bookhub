package com.bookhub.library.application.error;

public class NotificationOwnershipException extends RuntimeException {

  public NotificationOwnershipException(final String message) {
    super(message);
  }
}
