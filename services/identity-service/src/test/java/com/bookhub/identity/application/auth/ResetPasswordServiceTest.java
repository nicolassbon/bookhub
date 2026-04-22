package com.bookhub.identity.application.auth;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ResetPasswordServiceTest {

        @Mock
        private PasswordResetTokenRepository passwordResetTokenRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private Clock clock;

        @Mock
        private PasswordResetTokenHasher passwordResetTokenHasher;

        @InjectMocks
        private ResetPasswordService resetPasswordService;

        @Test
        @DisplayName("Should reset password when token consumption is valid")
        void shouldResetPasswordWhenTokenConsumptionIsValid() {
                final UUID userId = UUID.fromString("b95f69ca-bf80-49f6-a4d6-8477f9127a87");
                final User user = User.rehydrate(
                                userId,
                                "nico",
                                "nico@example.com",
                                "old-password",
                                "Nico",
                                UserRole.USER);
                when(clock.instant()).thenReturn(Instant.parse("2026-04-12T20:00:00Z"));
                when(passwordResetTokenHasher.hash("raw-token"))
                                .thenReturn("hashed-token");
                when(passwordResetTokenRepository.consumeUserIdByTokenHash(
                                "hashed-token",
                                Instant.parse("2026-04-12T20:00:00Z")))
                                .thenReturn(java.util.Optional.of(userId));
                when(userRepository.findById(userId)).thenReturn(Optional.of(user));
                when(passwordEncoder.encode("NewStrongPassword123!"))
                                .thenReturn("encoded-new-password");

                resetPasswordService.reset("raw-token", "NewStrongPassword123!");

                verify(passwordEncoder).encode("NewStrongPassword123!");
                verify(userRepository).save(user);
                verify(passwordResetTokenHasher).hash("raw-token");
        }

        @Test
        @DisplayName("Should reject reset when token does not exist")
        void shouldRejectResetWhenTokenDoesNotExist() {
                when(passwordResetTokenHasher.hash("unknown-token"))
                                .thenReturn("unknown-token-hash");
                when(passwordResetTokenRepository.consumeUserIdByTokenHash(
                                "unknown-token-hash",
                                Instant.parse("2026-04-12T20:00:00Z")))
                                .thenReturn(java.util.Optional.empty());
                when(clock.instant()).thenReturn(Instant.parse("2026-04-12T20:00:00Z"));

                assertThatThrownBy(() -> resetPasswordService.reset("unknown-token",
                                "NewStrongPassword123!")).isInstanceOf(
                                                InvalidPasswordResetTokenException.class)
                                                .hasMessage("Invalid or expired password reset token");

                verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should reject reset when token consumption fails")
        void shouldRejectResetWhenTokenConsumptionFails() {
                when(clock.instant()).thenReturn(Instant.parse("2026-04-12T20:00:00Z"));
                when(passwordResetTokenHasher.hash("expired-token"))
                                .thenReturn("expired-token-hash");
                when(passwordResetTokenRepository.consumeUserIdByTokenHash(
                                "expired-token-hash",
                                Instant.parse("2026-04-12T20:00:00Z")))
                                .thenReturn(java.util.Optional.empty());

                assertThatThrownBy(() -> resetPasswordService.reset("expired-token",
                                "NewStrongPassword123!")).isInstanceOf(
                                                InvalidPasswordResetTokenException.class)
                                                .hasMessage("Invalid or expired password reset token");

                verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should reject reset when token user does not exist")
        void shouldRejectResetWhenTokenUserDoesNotExist() {
                final UUID userId = UUID.fromString("8bd381f7-5f26-4c56-bf62-51ca6b61a2cb");
                when(clock.instant()).thenReturn(Instant.parse("2026-04-12T20:00:00Z"));
                when(passwordResetTokenHasher.hash("valid-token"))
                                .thenReturn("valid-token-hash");
                when(passwordResetTokenRepository.consumeUserIdByTokenHash(
                                "valid-token-hash",
                                Instant.parse("2026-04-12T20:00:00Z")))
                                .thenReturn(java.util.Optional.of(userId));
                when(userRepository.findById(userId)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> resetPasswordService.reset("valid-token",
                                "NewStrongPassword123!")).isInstanceOf(
                                                InvalidPasswordResetTokenException.class)
                                                .hasMessage("Invalid or expired password reset token");

                verify(userRepository, never()).save(any(User.class));
        }
}
