package com.bookhub.identity.application.auth;

import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserRepository;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LoginUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;

    public LoginUserService(
            final UserRepository userRepository,
            final PasswordEncoder passwordEncoder,
            final TokenIssuer tokenIssuer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenIssuer = tokenIssuer;
    }

    public LoginUserResult login(final LoginUserCommand command) {
        final String normalizedEmail = normalize(command.email());

        final User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        final boolean passwordMatches = passwordEncoder.matches(command.password(), user.getPasswordHash());
        if (!passwordMatches) {
            throw new InvalidCredentialsException();
        }

        final TokenIssuer.IssuedTokenPair issuedTokens = tokenIssuer.issueFor(user);
        return LoginUserResult.builder()
                .accessToken(issuedTokens.accessToken())
                .expiresIn(issuedTokens.expiresIn())
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
}
