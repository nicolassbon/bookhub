package com.bookhub.identity.application.auth.ratelimit;

import java.time.Duration;

public interface AuthRateLimitStore {

  RateLimitDecision consume(String bucketKey, int maxAttempts, Duration window);
}
