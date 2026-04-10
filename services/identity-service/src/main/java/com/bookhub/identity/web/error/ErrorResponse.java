package com.bookhub.identity.web.error;

import java.time.Instant;
import lombok.Builder;

@Builder
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path) {
}
