package com.bookhub.identity.domain.auth;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {

    Optional<RefreshToken> findByToken(UUID token);

    Optional<RefreshToken> findActiveByToken(UUID token, Instant now);

    RefreshToken save(RefreshToken refreshToken);

    void revokeByToken(UUID token);
}
