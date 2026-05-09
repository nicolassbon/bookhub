package com.bookhub.identity.application.auth.ratelimit;

public class AuthRateLimitStoreUnavailableException extends RuntimeException {

  public AuthRateLimitStoreUnavailableException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
