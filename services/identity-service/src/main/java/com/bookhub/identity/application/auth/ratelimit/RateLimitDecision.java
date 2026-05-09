package com.bookhub.identity.application.auth.ratelimit;

import java.time.Duration;

public record RateLimitDecision(boolean allowed, int remainingAttempts, Duration ttl) {

  public static RateLimitDecision allowed(final int remainingAttempts, final Duration ttl) {
    return new RateLimitDecision(true, Math.max(remainingAttempts, 0), ttl);
  }

  public static RateLimitDecision blocked(final Duration ttl) {
    return new RateLimitDecision(false, 0, ttl);
  }
}
