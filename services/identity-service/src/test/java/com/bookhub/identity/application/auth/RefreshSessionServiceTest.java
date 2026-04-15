package com.bookhub.identity.application.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookhub.identity.config.RefreshTokenProperties;
import com.bookhub.identity.domain.auth.RefreshToken;
import com.bookhub.identity.domain.auth.RefreshTokenRepository;
import com.bookhub.identity.domain.user.User;
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
class RefreshSessionServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenIssuer tokenIssuer;

    @Mock
    private RefreshTokenProperties refreshTokenProperties;

    @Mock
    private Clock clock;

    @Mock
    private AuthResultMapper authResultMapper;

    @InjectMocks
    private RefreshSessionService refreshSessionService;

    @Test
    @DisplayName("Should rotate refresh token and return a new access token")
    void shouldRotateRefreshTokenAndReturnANewAccessToken() {
        final User user = User.rehydrate(
                UUID.fromString("6676f2d8-0f65-40ae-b102-66145e24f3fd"),
                "nico",
                "nico@example.com",
                "hash",
                "Nicolas Bon",
                UserRole.USER);

        final UUID tokenValue = UUID.fromString("d7cc2a0f-ea1a-4f74-8e64-3f3f4a5ba723");
        final RefreshToken activeToken = RefreshToken.issue(
                tokenValue,
                user,
                Instant.parse("2026-04-20T13:00:00Z"));

        when(clock.instant()).thenReturn(Instant.parse("2026-04-12T13:00:00Z"));
        when(refreshTokenProperties.expirationSeconds()).thenReturn(604800L);
        when(refreshTokenRepository.findActiveByToken(tokenValue, Instant.parse("2026-04-12T13:00:00Z")))
                .thenReturn(Optional.of(activeToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenIssuer.issueFor(user)).thenReturn(TokenIssuer.IssuedTokenPair.builder()
                .accessToken("new-access-token")
                .expiresIn(3600)
                .build());
        when(authResultMapper.toLoginUserView(user)).thenReturn(LoginUserResult.LoginUserView.builder()
                .userId("6676f2d8-0f65-40ae-b102-66145e24f3fd")
                .username("nico")
                .displayName("Nicolas Bon")
                .role("USER")
                .build());

        final RefreshSessionResult result = refreshSessionService.refresh(tokenValue.toString());

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.expiresIn()).isEqualTo(3600);
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotEqualTo(tokenValue.toString());
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should reject refresh when token is invalid")
    void shouldRejectRefreshWhenTokenIsInvalid() {
        assertThatThrownBy(() -> refreshSessionService.refresh("not-a-uuid"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("Invalid refresh token");

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        verify(tokenIssuer, never()).issueFor(any(User.class));
    }
}
