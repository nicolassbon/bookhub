package com.bookhub.identity.application.auth;

import com.bookhub.identity.config.RefreshTokenProperties;
import com.bookhub.identity.domain.auth.RefreshToken;
import com.bookhub.identity.domain.auth.RefreshTokenRepository;
import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LoginUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenProperties refreshTokenProperties;
    private final Clock clock;

    public LoginUserService(
            final UserRepository userRepository,
            final PasswordEncoder passwordEncoder,
            final TokenIssuer tokenIssuer,
            final RefreshTokenRepository refreshTokenRepository,
            final RefreshTokenProperties refreshTokenProperties,
            final Clock clock) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenIssuer = tokenIssuer;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenProperties = refreshTokenProperties;
        this.clock = clock;
    }

    @Transactional
    public LoginUserResult login(final LoginUserCommand command) {
        final String normalizedEmail = normalize(command.email());

        final User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        final boolean passwordMatches = passwordEncoder.matches(command.password(), user.getPasswordHash());
        if (!passwordMatches) {
            throw new InvalidCredentialsException();
        }

        final String refreshTokenValue = issueRefreshToken(user);
        final TokenIssuer.IssuedTokenPair issuedTokens = tokenIssuer.issueFor(user);
        return LoginUserResult.builder()
                .accessToken(issuedTokens.accessToken())
                .expiresIn(issuedTokens.expiresIn())
                .refreshToken(refreshTokenValue)
                .refreshTokenExpiresIn(refreshTokenProperties.expirationSeconds())
                .user(LoginUserResult.LoginUserView.builder()
                        .userId(user.getId().toString())
                        .username(user.getUsername())
                        .displayName(user.getDisplayName())
                        .role(user.getRole().name())
                        .build())
                .build();
    }

    private String normalize(final String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private String issueRefreshToken(final User user) {
        final UUID tokenValue = UUID.randomUUID();
        final Instant now = Instant.now(clock);
        final RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiresAt(now.plusSeconds(refreshTokenProperties.expirationSeconds()))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return tokenValue.toString();
    }
}
