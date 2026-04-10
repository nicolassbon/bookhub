package com.bookhub.identity.web.auth;

import lombok.Builder;

@Builder
public record RegisterResponse(
        String userId,
        String username,
        String email,
        String displayName,
        String role) {
}
