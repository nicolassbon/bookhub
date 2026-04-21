package com.bookhub.identity.web.auth;

import com.bookhub.identity.domain.auth.RefreshToken;
import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserRole;
import com.bookhub.identity.infrastructure.security.HmacRefreshTokenHasher;
import java.time.Instant;
import java.util.UUID;

public final class AuthIntegrationFixture {

    private static final HmacRefreshTokenHasher REFRESH_TOKEN_HASHER =
            new HmacRefreshTokenHasher("test-refresh-token-secret");

    private AuthIntegrationFixture() {
    }

    public static User user(
            final String username,
            final String email,
            final String encodedPassword,
            final String displayName) {
        return User.create(username, email, encodedPassword, displayName, UserRole.USER);
    }

    public static RefreshToken refreshToken(
            final UUID token,
            final User user,
            final Instant expiresAt) {
        return RefreshToken.issue(REFRESH_TOKEN_HASHER.hash(token.toString()), user, expiresAt);
    }

    public static String refreshTokenHash(final UUID token) {
        return REFRESH_TOKEN_HASHER.hash(token.toString());
    }
}
