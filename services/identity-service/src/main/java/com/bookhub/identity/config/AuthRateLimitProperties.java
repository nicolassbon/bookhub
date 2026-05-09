package com.bookhub.identity.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.rate-limit")
public record AuthRateLimitProperties(
    EndpointRule login,
    EndpointRule register,
    EndpointRule forgotPassword,
    EndpointRule refresh,
    EndpointRule serviceToken,
    boolean trustForwardedHeaders,
    List<String> trustedProxyCidrs,
    Redis redis) {

  public record EndpointRule(int maxAttempts, long windowSeconds) {}

  public record Redis(String keyPrefix, FailureMode failureMode) {}

  public enum FailureMode {
    FAIL_CLOSED,
    FAIL_OPEN
  }
}
