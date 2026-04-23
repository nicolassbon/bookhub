package com.bookhub.identity.application.auth;

import com.bookhub.identity.domain.auth.RefreshTokenRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LogoutUserService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final RefreshTokenHasher refreshTokenHasher;

  public LogoutUserService(
      final RefreshTokenRepository refreshTokenRepository,
      final RefreshTokenHasher refreshTokenHasher) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.refreshTokenHasher = refreshTokenHasher;
  }

  public void logout(final String refreshTokenValue) {
    final String token = parseToken(refreshTokenValue);
    if (token == null) {
      return;
    }

    refreshTokenRepository.revokeByTokenHash(refreshTokenHasher.hash(token));
  }

  private String parseToken(final String refreshTokenValue) {
    if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
      return null;
    }

    try {
      return UUID.fromString(refreshTokenValue).toString();
    } catch (IllegalArgumentException exception) {
      return null;
    }
  }
}
