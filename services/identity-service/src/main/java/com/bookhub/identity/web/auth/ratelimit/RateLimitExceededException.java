package com.bookhub.identity.web.auth.ratelimit;

public class RateLimitExceededException extends RuntimeException {

  public RateLimitExceededException(final String message) {
    super(message);
  }
}
