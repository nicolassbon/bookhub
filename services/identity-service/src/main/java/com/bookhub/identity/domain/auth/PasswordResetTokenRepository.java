package com.bookhub.identity.domain.auth;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository {

    PasswordResetToken save(PasswordResetToken passwordResetToken);

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    void delete(PasswordResetToken passwordResetToken);

    void deleteByUserId(UUID userId);
}
