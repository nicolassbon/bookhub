package com.bookhub.identity.domain.auth;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository {

    PasswordResetToken save(PasswordResetToken passwordResetToken);

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    void replaceForUser(UUID userId, String tokenHash, Instant expiresAt);

    Optional<UUID> consumeUserIdByTokenHash(String tokenHash, Instant now);

    void delete(PasswordResetToken passwordResetToken);

    void deleteByUserId(UUID userId);
}
