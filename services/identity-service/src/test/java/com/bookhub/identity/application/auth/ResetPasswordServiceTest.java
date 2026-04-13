package com.bookhub.identity.application.auth;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        @InjectMocks
        private ResetPasswordService resetPasswordService;

        @Test
        @DisplayName("Should reset password and invalidate all user tokens when token is valid")
        void shouldResetPasswordAndInvalidateAllUserTokensWhenTokenIsValid() {
                final UUID userId = UUID.fromString("b95f69ca-bf80-49f6-a4d6-8477f9127a87");
                final User user = User.builder().id(userId).username("nico")
                                .email("nico@example.com").passwordHash("old-password")
                                .displayName("Nico").role(UserRole.USER).build();
                final PasswordResetToken resetToken = PasswordResetToken.builder()
                                .id(UUID.fromString("f0f4f780-edf1-4c06-b5a0-0464bf8282d7"))
                                .token("f26e5f56-b8fb-4bd5-b18a-95a7ef8d7ab3").userId(userId)
                                .expiresAt(Instant.parse("2026-04-12T21:00:00Z")).build();

                when(clock.instant()).thenReturn(Instant.parse("2026-04-12T20:00:00Z"));
                when(passwordResetTokenRepository.findByToken(resetToken.getToken()))
                                .thenReturn(Optional.of(resetToken));
                when(userRepository.findById(userId)).thenReturn(Optional.of(user));
                when(passwordEncoder.encode("NewStrongPassword123!"))
                                .thenReturn("encoded-new-password");

                resetPasswordService.reset(resetToken.getToken(), "NewStrongPassword123!");

                verify(passwordEncoder).encode("NewStrongPassword123!");
                verify(userRepository).save(user);
                verify(passwordResetTokenRepository).delete(resetToken);
                verify(passwordResetTokenRepository).deleteByUserId(userId);
        }

        @Test
        @DisplayName("Should reject reset when token does not exist")
        void shouldRejectResetWhenTokenDoesNotExist() {
                when(passwordResetTokenRepository.findByToken("unknown-token"))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> resetPasswordService.reset("unknown-token",
                                "NewStrongPassword123!")).isInstanceOf(
                                                InvalidPasswordResetTokenException.class)
                                                .hasMessage("Invalid or expired password reset token");

                verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should reject reset when token is expired")
        void shouldRejectResetWhenTokenIsExpired() {
                final PasswordResetToken expiredToken = PasswordResetToken.builder()
                                .id(UUID.fromString("85af7414-b79a-4e95-8f87-5eb0d7104d8e"))
                                .token("expired-token")
                                .userId(UUID.fromString("334dd578-518e-49bc-b9d3-a4f89e4c3c46"))
                                .expiresAt(Instant.parse("2026-04-12T19:59:00Z")).build();

                when(clock.instant()).thenReturn(Instant.parse("2026-04-12T20:00:00Z"));
                when(passwordResetTokenRepository.findByToken("expired-token"))
                                .thenReturn(Optional.of(expiredToken));

                assertThatThrownBy(() -> resetPasswordService.reset("expired-token",
                                "NewStrongPassword123!")).isInstanceOf(
                                                InvalidPasswordResetTokenException.class)
                                                .hasMessage("Invalid or expired password reset token");

                verify(userRepository, never()).save(any(User.class));
                verify(passwordResetTokenRepository, never()).delete(any(PasswordResetToken.class));
                verify(passwordResetTokenRepository, never()).deleteByUserId(any(UUID.class));
        }

        @Test
        @DisplayName("Should reject reset when token user does not exist")
        void shouldRejectResetWhenTokenUserDoesNotExist() {
                final UUID userId = UUID.fromString("8bd381f7-5f26-4c56-bf62-51ca6b61a2cb");
                final PasswordResetToken validToken = PasswordResetToken.builder()
                                .id(UUID.fromString("f2f8e967-b91b-48f8-aa5d-f251cd5963ad"))
                                .token("valid-token").userId(userId)
                                .expiresAt(Instant.parse("2026-04-12T21:00:00Z")).build();

                when(clock.instant()).thenReturn(Instant.parse("2026-04-12T20:00:00Z"));
                when(passwordResetTokenRepository.findByToken("valid-token"))
                                .thenReturn(Optional.of(validToken));
                when(userRepository.findById(userId)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> resetPasswordService.reset("valid-token",
                                "NewStrongPassword123!")).isInstanceOf(
                                                InvalidPasswordResetTokenException.class)
                                                .hasMessage("Invalid or expired password reset token");

                verify(userRepository, never()).save(any(User.class));
                verify(passwordResetTokenRepository, never()).delete(any(PasswordResetToken.class));
                verify(passwordResetTokenRepository, never()).deleteByUserId(any(UUID.class));
        }
}
