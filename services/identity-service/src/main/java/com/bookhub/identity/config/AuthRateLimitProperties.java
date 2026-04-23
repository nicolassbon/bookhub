package com.bookhub.identity.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.rate-limit")
public record AuthRateLimitProperties(
    EndpointRule login,
    EndpointRule register,
    EndpointRule forgotPassword,
    EndpointRule refresh,
    boolean trustForwardedHeaders,
    List<String> trustedProxyCidrs,
    int maxTrackedKeys,
    long staleEntryTtlSeconds) {

  public record EndpointRule(int maxAttempts, long windowSeconds) {}
}
