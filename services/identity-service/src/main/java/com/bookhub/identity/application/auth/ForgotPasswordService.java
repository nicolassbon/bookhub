package com.bookhub.identity.application.auth;

import com.bookhub.identity.config.PasswordResetProperties;
import com.bookhub.identity.domain.auth.MailSenderPort;
import com.bookhub.identity.domain.auth.PasswordResetToken;
import com.bookhub.identity.domain.auth.PasswordResetTokenRepository;
import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ForgotPasswordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForgotPasswordService.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MailSenderPort mailSenderPort;
    private final PasswordResetProperties passwordResetProperties;
    private final Clock clock;

    public ForgotPasswordService(final UserRepository userRepository,
            final PasswordResetTokenRepository passwordResetTokenRepository,
            final MailSenderPort mailSenderPort,
            final PasswordResetProperties passwordResetProperties, final Clock clock) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.mailSenderPort = mailSenderPort;
        this.passwordResetProperties = passwordResetProperties;
        this.clock = clock;
    }

    @Transactional
    public void request(final String email) {
        final String normalizedEmail = normalize(email);
        final Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);
        if (userOptional.isEmpty()) {
            return;
        }

        final User user = userOptional.get();
        final String token = UUID.randomUUID().toString();
        final Instant now = Instant.now(clock);

        passwordResetTokenRepository.deleteByUserId(user.getId());
        final PasswordResetToken passwordResetToken = PasswordResetToken.issue(
                token,
                user.getId(),
                now.plusSeconds(passwordResetProperties.expirationSeconds()));
        passwordResetTokenRepository.save(passwordResetToken);

        try {
            mailSenderPort.sendPasswordResetEmail(user.getEmail(), token);
        } catch (RuntimeException exception) {
            LOGGER.warn("Password reset mail delivery failed for {}", normalizedEmail, exception);
        }
    }

    private String normalize(final String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
