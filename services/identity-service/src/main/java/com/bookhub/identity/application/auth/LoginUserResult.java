package com.bookhub.identity.application.auth;

import lombok.Builder;

@Builder
public record LoginUserResult(
        String accessToken,
        long expiresIn,
        LoginUserView user) {

    @Builder
    public record LoginUserView(
            String userId,
            String username,
            String displayName,
            String role) {
    }
}
