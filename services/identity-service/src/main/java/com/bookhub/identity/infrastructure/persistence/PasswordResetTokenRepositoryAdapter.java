package com.bookhub.identity.infrastructure.persistence;

import com.bookhub.identity.domain.auth.PasswordResetToken;
import com.bookhub.identity.domain.auth.PasswordResetTokenRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepository {

    private final PasswordResetTokenJpaRepository passwordResetTokenJpaRepository;

    public PasswordResetTokenRepositoryAdapter(final PasswordResetTokenJpaRepository passwordResetTokenJpaRepository) {
        this.passwordResetTokenJpaRepository = passwordResetTokenJpaRepository;
    }

    @Override
    public PasswordResetToken save(final PasswordResetToken passwordResetToken) {
        return passwordResetTokenJpaRepository.save(passwordResetToken);
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(final String tokenHash) {
        return passwordResetTokenJpaRepository.findByTokenHash(tokenHash);
    }

    @Override
    public void delete(final PasswordResetToken passwordResetToken) {
        passwordResetTokenJpaRepository.delete(passwordResetToken);
    }

    @Override
    public void deleteByUserId(final UUID userId) {
        passwordResetTokenJpaRepository.deleteByUserId(userId);
    }
}
