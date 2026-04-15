package com.bookhub.identity.application.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookhub.identity.config.PasswordResetProperties;
import com.bookhub.identity.domain.auth.MailSenderPort;
import com.bookhub.identity.domain.auth.PasswordResetToken;
import com.bookhub.identity.domain.auth.PasswordResetTokenRepository;
import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserRepository;
import com.bookhub.identity.domain.user.UserRole;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private MailSenderPort mailSenderPort;

    @Mock
    private PasswordResetProperties passwordResetProperties;

    @Mock
    private Clock clock;

    @InjectMocks
    private ForgotPasswordService forgotPasswordService;

    @Test
    @DisplayName("Should create reset token and send email when user exists")
    void shouldCreateResetTokenAndSendEmailWhenUserExists() {
        final User existingUser = User.rehydrate(
                UUID.fromString("4a6b21df-9276-4fa9-92ea-f22748fc45aa"),
                "nico",
                "user@example.com",
                "encoded-password",
                "Nico",
                UserRole.USER);

        final Instant now = Instant.parse("2026-04-12T20:00:00Z");
        when(clock.instant()).thenReturn(now);
        when(passwordResetProperties.expirationSeconds()).thenReturn(900L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        forgotPasswordService.request(" user@example.com ");

        verify(passwordResetTokenRepository).deleteByUserId(existingUser.getId());
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(mailSenderPort).sendPasswordResetEmail(eq("user@example.com"), any(String.class));
    }

    @Test
    @DisplayName("Should not persist token or send email when user does not exist")
    void shouldNotPersistTokenOrSendEmailWhenUserDoesNotExist() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        forgotPasswordService.request("unknown@example.com");

        verify(passwordResetTokenRepository, never()).deleteByUserId(any(UUID.class));
        verify(passwordResetTokenRepository, never()).save(any(PasswordResetToken.class));
        verify(mailSenderPort, never()).sendPasswordResetEmail(any(String.class), any(String.class));
    }
}
