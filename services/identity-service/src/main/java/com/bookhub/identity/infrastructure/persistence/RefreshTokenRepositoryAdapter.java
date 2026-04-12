package com.bookhub.identity.infrastructure.persistence;

import com.bookhub.identity.domain.auth.RefreshToken;
import com.bookhub.identity.domain.auth.RefreshTokenRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    public RefreshTokenRepositoryAdapter(final RefreshTokenJpaRepository refreshTokenJpaRepository) {
        this.refreshTokenJpaRepository = refreshTokenJpaRepository;
    }

    @Override
    public Optional<RefreshToken> findByToken(final UUID token) {
        return refreshTokenJpaRepository.findById(token);
    }

    @Override
    public Optional<RefreshToken> findActiveByToken(final UUID token, final Instant now) {
        return refreshTokenJpaRepository.findByTokenAndRevokedFalseAndExpiresAtAfter(token, now);
    }

    @Override
    public RefreshToken save(final RefreshToken refreshToken) {
        return refreshTokenJpaRepository.save(refreshToken);
    }

    @Override
    public void revokeByToken(final UUID token) {
        findByToken(token).ifPresent(refreshToken -> {
            refreshToken.revoke(Instant.now());
            refreshTokenJpaRepository.save(refreshToken);
        });
    }
}
