package com.bookhub.catalog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "catalog.providers.openlibrary")
public record OpenLibraryProperties(
    boolean enabled, String url, int timeoutMs, CircuitBreaker circuitBreaker) {

  public OpenLibraryProperties {
    circuitBreaker =
        circuitBreaker == null ? CircuitBreaker.defaults() : circuitBreaker.withDefaults();
  }

  public record CircuitBreaker(
      String name,
      int slidingWindowSize,
      int minimumNumberOfCalls,
      float failureRateThreshold,
      long waitDurationOpenStateMs,
      int permittedNumberOfCallsInHalfOpenState) {

    private static final String DEFAULT_NAME = "openLibrary";
    private static final int DEFAULT_SLIDING_WINDOW_SIZE = 10;
    private static final int DEFAULT_MINIMUM_NUMBER_OF_CALLS = 5;
    private static final float DEFAULT_FAILURE_RATE_THRESHOLD = 50f;
    private static final long DEFAULT_WAIT_DURATION_OPEN_STATE_MS = 30_000L;
    private static final int DEFAULT_HALF_OPEN_CALLS = 2;

    public static CircuitBreaker defaults() {
      return new CircuitBreaker(
          DEFAULT_NAME,
          DEFAULT_SLIDING_WINDOW_SIZE,
          DEFAULT_MINIMUM_NUMBER_OF_CALLS,
          DEFAULT_FAILURE_RATE_THRESHOLD,
          DEFAULT_WAIT_DURATION_OPEN_STATE_MS,
          DEFAULT_HALF_OPEN_CALLS);
    }

    public CircuitBreaker withDefaults() {
      return new CircuitBreaker(
          (name == null || name.isBlank()) ? DEFAULT_NAME : name,
          slidingWindowSize > 0 ? slidingWindowSize : DEFAULT_SLIDING_WINDOW_SIZE,
          minimumNumberOfCalls > 0 ? minimumNumberOfCalls : DEFAULT_MINIMUM_NUMBER_OF_CALLS,
          failureRateThreshold > 0 ? failureRateThreshold : DEFAULT_FAILURE_RATE_THRESHOLD,
          waitDurationOpenStateMs > 0
              ? waitDurationOpenStateMs
              : DEFAULT_WAIT_DURATION_OPEN_STATE_MS,
          permittedNumberOfCallsInHalfOpenState > 0
              ? permittedNumberOfCallsInHalfOpenState
              : DEFAULT_HALF_OPEN_CALLS);
    }
  }
}
