package com.bookhub.identity.support;

import com.bookhub.identity.infrastructure.persistence.PasswordResetTokenJpaRepository;
import com.bookhub.identity.infrastructure.persistence.RefreshTokenJpaRepository;
import com.bookhub.identity.infrastructure.persistence.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdentityDatabaseCleaner {

  private final RefreshTokenJpaRepository refreshTokenJpaRepository;
  private final PasswordResetTokenJpaRepository passwordResetTokenJpaRepository;
  private final UserJpaRepository userJpaRepository;

  public void clean() {
    refreshTokenJpaRepository.deleteAll();
    passwordResetTokenJpaRepository.deleteAll();
    userJpaRepository.deleteAll();
  }
}
