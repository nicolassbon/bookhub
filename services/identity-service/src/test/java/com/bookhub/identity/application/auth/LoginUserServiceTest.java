package com.bookhub.identity.application.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookhub.identity.config.RefreshTokenProperties;
import com.bookhub.identity.domain.auth.RefreshToken;
import com.bookhub.identity.domain.auth.RefreshTokenRepository;
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
class LoginUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenIssuer tokenIssuer;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private RefreshTokenProperties refreshTokenProperties;

    @Mock
    private Clock clock;

    @Mock
    private AuthResultMapper authResultMapper;

    @InjectMocks
    private LoginUserService loginUserService;

    @Test
    @DisplayName("Should authenticate user and return login payload when credentials are valid")
    void shouldAuthenticateUserAndReturnLoginPayloadWhenCredentialsAreValid() {
        final LoginUserCommand command = LoginUserCommand.builder()
                .email(" NICO@Example.com ")
                .password("StrongPassword123!")
                .build();

        final User existingUser = User.rehydrate(
                UUID.fromString("6676f2d8-0f65-40ae-b102-66145e24f3fd"),
                "nico",
                "nico@example.com",
                "stored-hash",
                "Nicolas Bon",
                UserRole.USER);

        when(userRepository.findByEmail("nico@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("StrongPassword123!", "stored-hash")).thenReturn(true);
        when(refreshTokenProperties.expirationSeconds()).thenReturn(604800L);
        when(clock.instant()).thenReturn(Instant.parse("2026-04-12T12:00:00Z"));
        when(tokenIssuer.issueFor(existingUser)).thenReturn(TokenIssuer.IssuedTokenPair.builder()
                .accessToken("jwt-access-token")
                .expiresIn(3600)
                .build());
        when(authResultMapper.toLoginUserView(existingUser)).thenReturn(LoginUserResult.LoginUserView.builder()
                .userId("6676f2d8-0f65-40ae-b102-66145e24f3fd")
                .username("nico")
                .displayName("Nicolas Bon")
                .role("USER")
                .build());

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final LoginUserResult result = loginUserService.login(command);

        assertThat(result.accessToken()).isEqualTo("jwt-access-token");
        assertThat(result.expiresIn()).isEqualTo(3600);
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.refreshTokenExpiresIn()).isEqualTo(604800);
        assertThat(result.user().userId()).isEqualTo("6676f2d8-0f65-40ae-b102-66145e24f3fd");
        assertThat(result.user().username()).isEqualTo("nico");
        assertThat(result.user().displayName()).isEqualTo("Nicolas Bon");
        assertThat(result.user().role()).isEqualTo("USER");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should reject authentication when email does not exist")
    void shouldRejectAuthenticationWhenEmailDoesNotExist() {
        final LoginUserCommand command = LoginUserCommand.builder()
                .email("nico@example.com")
                .password("StrongPassword123!")
                .build();

        when(userRepository.findByEmail("nico@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUserService.login(command))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(tokenIssuer, never()).issueFor(any(User.class));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should reject authentication when password does not match")
    void shouldRejectAuthenticationWhenPasswordDoesNotMatch() {
        final LoginUserCommand command = LoginUserCommand.builder()
                .email("nico@example.com")
                .password("WrongPassword123!")
                .build();

        final User existingUser = User.rehydrate(
                UUID.fromString("6676f2d8-0f65-40ae-b102-66145e24f3fd"),
                "nico",
                "nico@example.com",
                "stored-hash",
                "Nicolas Bon",
                UserRole.USER);

        when(userRepository.findByEmail("nico@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("WrongPassword123!", "stored-hash")).thenReturn(false);

        assertThatThrownBy(() -> loginUserService.login(command))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(tokenIssuer, never()).issueFor(existingUser);
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }
}
