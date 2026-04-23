package com.bookhub.identity.infrastructure.persistence;

import com.bookhub.identity.domain.auth.RefreshToken;
import com.bookhub.identity.domain.auth.RefreshTokenRepository;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

  private final RefreshTokenJpaRepository refreshTokenJpaRepository;

  public RefreshTokenRepositoryAdapter(final RefreshTokenJpaRepository refreshTokenJpaRepository) {
    this.refreshTokenJpaRepository = refreshTokenJpaRepository;
  }

  @Override
  public Optional<RefreshToken> findByTokenHash(final String tokenHash) {
    return refreshTokenJpaRepository.findById(tokenHash);
  }

  @Override
  public Optional<RefreshToken> findActiveByTokenHashForUpdate(
      final String tokenHash, final Instant now) {
    return refreshTokenJpaRepository.findByTokenHashAndRevokedFalseAndExpiresAtAfter(
        tokenHash, now);
  }

  @Override
  public RefreshToken save(final RefreshToken refreshToken) {
    return refreshTokenJpaRepository.save(refreshToken);
  }

  @Override
  public void revokeByTokenHash(final String tokenHash) {
    findByTokenHash(tokenHash)
        .ifPresent(
            refreshToken -> {
              refreshToken.revoke(Instant.now());
              refreshTokenJpaRepository.save(refreshToken);
            });
  }
}
