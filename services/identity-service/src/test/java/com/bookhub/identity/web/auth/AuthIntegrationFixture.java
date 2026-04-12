package com.bookhub.identity.web.auth;

import com.bookhub.identity.domain.auth.RefreshToken;
import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserRole;
import java.time.Instant;
import java.util.UUID;

public final class AuthIntegrationFixture {

    private AuthIntegrationFixture() {
    }

    public static User user(
            final String username,
            final String email,
            final String encodedPassword,
            final String displayName) {
        return User.builder()
                .username(username)
                .email(email)
                .passwordHash(encodedPassword)
                .displayName(displayName)
                .role(UserRole.USER)
                .build();
    }

    public static RefreshToken refreshToken(
            final UUID token,
            final User user,
            final Instant expiresAt) {
        return RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();
    }
}
