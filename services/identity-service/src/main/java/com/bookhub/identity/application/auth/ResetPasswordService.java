package com.bookhub.identity.application.auth;

import com.bookhub.identity.domain.auth.PasswordResetToken;
import com.bookhub.identity.domain.auth.PasswordResetTokenRepository;
import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserRepository;
import java.time.Clock;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ResetPasswordService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public ResetPasswordService(
            final PasswordResetTokenRepository passwordResetTokenRepository,
            final UserRepository userRepository,
            final PasswordEncoder passwordEncoder,
            final Clock clock) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    public void reset(final String token, final String newPassword) {
        final Instant now = Instant.now(clock);
        final PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(InvalidPasswordResetTokenException::new);

        if (passwordResetToken.getExpiresAt().isBefore(now)) {
            throw new InvalidPasswordResetTokenException();
        }

        final User user = userRepository.findById(passwordResetToken.getUserId())
                .orElseThrow(InvalidPasswordResetTokenException::new);

        final String encodedPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(encodedPassword);
        userRepository.save(user);

        passwordResetTokenRepository.delete(passwordResetToken);
        passwordResetTokenRepository.deleteByUserId(user.getId());
    }
}
