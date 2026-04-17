package com.bookhub.identity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.rate-limit")
public record AuthRateLimitProperties(
        EndpointRule login,
        EndpointRule register,
        EndpointRule forgotPassword) {

    public record EndpointRule(int maxAttempts, long windowSeconds) {
    }
}
