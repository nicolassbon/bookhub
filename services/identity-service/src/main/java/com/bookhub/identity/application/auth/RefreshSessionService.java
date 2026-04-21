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
    private final RefreshTokenHasher refreshTokenHasher;
    private final TokenIssuer tokenIssuer;
    private final RefreshTokenProperties refreshTokenProperties;
    private final Clock clock;
    private final AuthResultMapper authResultMapper;

    public RefreshSessionService(
            final RefreshTokenRepository refreshTokenRepository,
            final RefreshTokenHasher refreshTokenHasher,
            final TokenIssuer tokenIssuer,
            final RefreshTokenProperties refreshTokenProperties,
            final Clock clock,
            final AuthResultMapper authResultMapper) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenHasher = refreshTokenHasher;
        this.tokenIssuer = tokenIssuer;
        this.refreshTokenProperties = refreshTokenProperties;
        this.clock = clock;
        this.authResultMapper = authResultMapper;
    }

    public RefreshSessionResult refresh(final String refreshTokenValue) {
        final String token = parseToken(refreshTokenValue);
        final String tokenHash = refreshTokenHasher.hash(token);
        final Instant now = Instant.now(clock);
        final RefreshToken existingToken = refreshTokenRepository.findActiveByTokenHashForUpdate(tokenHash, now)
                .orElseThrow(InvalidRefreshTokenException::new);

        existingToken.revoke(now);
        refreshTokenRepository.save(existingToken);

        final User user = existingToken.getUser();
        final String newTokenValue = UUID.randomUUID().toString();
        final RefreshToken newToken = RefreshToken.issue(
                refreshTokenHasher.hash(newTokenValue),
                user,
                now.plusSeconds(refreshTokenProperties.expirationSeconds()));
        refreshTokenRepository.save(newToken);

        final TokenIssuer.IssuedTokenPair accessToken = tokenIssuer.issueFor(user);
        return RefreshSessionResult.builder()
                .accessToken(accessToken.accessToken())
                .expiresIn(accessToken.expiresIn())
                .refreshToken(newTokenValue)
                .refreshTokenExpiresIn(refreshTokenProperties.expirationSeconds())
                .user(authResultMapper.toLoginUserView(user))
                .build();
    }

    private String parseToken(final String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new InvalidRefreshTokenException();
        }

        try {
            return UUID.fromString(refreshTokenValue).toString();
        } catch (IllegalArgumentException exception) {
            throw new InvalidRefreshTokenException();
        }
    }
}
