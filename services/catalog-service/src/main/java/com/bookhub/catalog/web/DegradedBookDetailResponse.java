package com.bookhub.catalog.web;

import lombok.Builder;

@Builder
public record DegradedBookDetailResponse(
        String id,
        String code,
        String message,
        boolean degraded,
        Integer retryAfterSeconds) {
}
