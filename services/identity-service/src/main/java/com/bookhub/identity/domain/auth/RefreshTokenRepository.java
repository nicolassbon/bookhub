package com.bookhub.identity.domain.auth;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    Optional<RefreshToken> findActiveByTokenHashForUpdate(String tokenHash, Instant now);

    RefreshToken save(RefreshToken refreshToken);

    void revokeByTokenHash(String tokenHash);
}
