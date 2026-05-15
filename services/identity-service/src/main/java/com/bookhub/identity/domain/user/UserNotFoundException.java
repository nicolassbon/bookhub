package com.bookhub.identity.domain.user;

public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(final String userId) {
    super("User not found: " + userId);
  }
}
