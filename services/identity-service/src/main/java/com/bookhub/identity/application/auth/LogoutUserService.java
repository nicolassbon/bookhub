package com.bookhub.identity.application.auth;

import com.bookhub.identity.domain.auth.RefreshTokenRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LogoutUserService {

    private final RefreshTokenRepository refreshTokenRepository;

    public LogoutUserService(final RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public void logout(final String refreshTokenValue) {
        final UUID token = parseToken(refreshTokenValue);
        if (token == null) {
            return;
        }

        refreshTokenRepository.revokeByToken(token);
    }

    private UUID parseToken(final String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            return null;
        }

        try {
            return UUID.fromString(refreshTokenValue);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
