package com.bookhub.identity.web.auth;

import lombok.Builder;

@Builder
public record LoginResponse(
        String accessToken,
        long expiresIn,
        LoginUserResponse user) {

    @Builder
    public record LoginUserResponse(
            String userId,
            String username,
            String displayName,
            String role) {
    }
}
