package com.bookhub.identity.application.auth;

import com.bookhub.identity.config.RefreshTokenProperties;
import com.bookhub.identity.domain.auth.RefreshToken;
import com.bookhub.identity.domain.auth.RefreshTokenRepository;
import com.bookhub.identity.domain.user.User;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RefreshSessionService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenIssuer tokenIssuer;
    private final RefreshTokenProperties refreshTokenProperties;
    private final Clock clock;
    private final AuthResultMapper authResultMapper;

    public RefreshSessionService(
            final RefreshTokenRepository refreshTokenRepository,
            final TokenIssuer tokenIssuer,
            final RefreshTokenProperties refreshTokenProperties,
            final Clock clock,
            final AuthResultMapper authResultMapper) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenIssuer = tokenIssuer;
        this.refreshTokenProperties = refreshTokenProperties;
        this.clock = clock;
        this.authResultMapper = authResultMapper;
    }

    public RefreshSessionResult refresh(final String refreshTokenValue) {
        final UUID token = parseToken(refreshTokenValue);
        final Instant now = Instant.now(clock);
        final RefreshToken existingToken = refreshTokenRepository.findActiveByToken(token, now)
                .orElseThrow(InvalidRefreshTokenException::new);

        existingToken.revoke(now);
        refreshTokenRepository.save(existingToken);

        final User user = existingToken.getUser();
        final UUID newTokenValue = UUID.randomUUID();
        final RefreshToken newToken = RefreshToken.issue(
                newTokenValue,
                user,
                now.plusSeconds(refreshTokenProperties.expirationSeconds()));
        refreshTokenRepository.save(newToken);

        final TokenIssuer.IssuedTokenPair accessToken = tokenIssuer.issueFor(user);
        return RefreshSessionResult.builder()
                .accessToken(accessToken.accessToken())
                .expiresIn(accessToken.expiresIn())
                .refreshToken(newTokenValue.toString())
                .refreshTokenExpiresIn(refreshTokenProperties.expirationSeconds())
                .user(authResultMapper.toLoginUserView(user))
                .build();
    }

    private UUID parseToken(final String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new InvalidRefreshTokenException();
        }

        try {
            return UUID.fromString(refreshTokenValue);
        } catch (IllegalArgumentException exception) {
            throw new InvalidRefreshTokenException();
        }
    }
}
